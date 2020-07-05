package com.sili.repository;

import com.sili.model.TokenTO;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class AuthStateRepository {

    private static final String TABLE_NAME = "ffs_ca.auth_state";

    private final io.vertx.mutiny.pgclient.PgPool client;

    private static TokenTO from(Row row) {
        return new TokenTO(
            row.getUUID("token"),
            row.getInteger("reps"),
            row.getInteger("succ_tries")
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

    public Uni<TokenTO> createAuthState(Tuple3<UUID, Long, Long> initValues) {
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
}