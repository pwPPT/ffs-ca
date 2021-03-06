package com.sili.repository;

import com.sili.model.AValueTO;
import com.sili.model.SessionResponseTO;
import com.sili.model.SuccessTO;
import com.sili.model.TokenResponseTO;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class AuthStateRepository {

    private static final String TABLE_NAME = "ffs_ca.auth_state";

    private final io.vertx.mutiny.pgclient.PgPool client;

    private static TokenResponseTO from(Row row) {
        return new TokenResponseTO(
            row.getUUID("token"),
            row.getInteger("reps"),
            row.getInteger("succ_tries")
        );
    }

    private static AValueTO aValueFrom(Row row) {
        return new AValueTO(
            row.getLong("curr_x"),
            Arrays.asList(row.getIntegerArray("curr_a")),
            row.getInteger("user_id")
        );
    }

    private static SuccessTO successToFrom(Row row) {
        return new SuccessTO(
            row.getInteger("succ_tries"),
            row.getInteger("reps")
        );
    }

    public Uni<Long> getUserId(UUID token) {
        return client.preparedQuery(
            "SELECT user_id FROM " + TABLE_NAME
                + " WHERE token = '" + token + "'")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? iterator.next().getLong("user_id")
                    : null
            );
    }

    public Uni<AValueTO> updateValues(UUID token, Long xValue, List<Long> aVector) {
        return client.preparedQuery(
            "UPDATE " + TABLE_NAME
                + " SET curr_x = " + xValue + ", "
                + " curr_a = ARRAY[" + aVector.stream().map(Object::toString).collect(Collectors.joining(",")) + "]"
                + " WHERE token = '" + token + "'"
                + " RETURNING curr_x, curr_a")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? aValueFrom(iterator.next())
                    : null
            );
    }

    public Uni<AValueTO> getYValueForToken(UUID token) {
        return client.preparedQuery(
            "SELECT * FROM " + TABLE_NAME
                + " WHERE token = '" + token + "'")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? aValueFrom(iterator.next())
                    : null
            );
    }

    public Uni<Long> checkAuthState(Long userId) {
        return client.preparedQuery(
            "SELECT * FROM " + TABLE_NAME
                + " WHERE user_id = " + userId)
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? iterator.next().getLong("user_id")
                    : null
            );
    }

    public Uni<TokenResponseTO> createAuthState(Tuple3<UUID, Long, Long> initValues) {
        return client.preparedQuery(
            "INSERT INTO " + TABLE_NAME
                + " (token, user_id, reps) VALUES"
                + " ('" + initValues.getItem1() + "',"
                + " " + initValues.getItem2() + ","
                + " " + initValues.getItem3()
                + ")  RETURNING *")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? from(iterator.next())
                    : null
            );
    }

    public Uni<SuccessTO> incrementSuccessTries(UUID token) {
        return client.preparedQuery(
            "UPDATE " + TABLE_NAME
                + " SET succ_tries = succ_tries + 1 "
                + " WHERE token = '" + token + "'"
                + " RETURNING *")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? successToFrom(iterator.next())
                    : null
            );
    }

    public Uni<Boolean> removeToken(SessionResponseTO session, UUID token) {
       if (session.getIs_authenticated()) {
            System.out.println("TRYING TO REMOVE");
            return client.preparedQuery(
                "DELETE FROM " + TABLE_NAME
                    + " WHERE token = '" + token + "'")
                .execute()
                .onItem().apply(pgRowSet -> pgRowSet.rowCount() == 1);
        }
        return Uni.createFrom().item(false);
    }
}
