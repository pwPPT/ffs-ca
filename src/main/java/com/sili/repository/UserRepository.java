package com.sili.repository;

import com.sili.model.RegisterTO;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class UserRepository {

    private static final String TABLE_NAME = "ffs_ca.users";

    private final io.vertx.mutiny.pgclient.PgPool client;

    private static RegisterTO from(Row row) {
        return new RegisterTO(
            row.getString("username"),
            Arrays.asList(row.getLongArray("public_key"))
        );
    }

    public Uni<RegisterTO> getUserByUsername(String username) {
        return client.preparedQuery(
            "SELECT * FROM " + TABLE_NAME
                + " WHERE username = '" + username + "'")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? from(iterator.next())
                    : null
            );
    }

    public Uni<Long> getUserIdByUsername(String username) {
        return client.preparedQuery(
            "SELECT id FROM " + TABLE_NAME
                + " WHERE username = '" + username + "'")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? iterator.next().getLong("id")
                    : null
            );
    }

    public Uni<RegisterTO> registerUser(RegisterTO user) {
        return client.preparedQuery(
            "INSERT INTO " + TABLE_NAME
                + " (username, public_key) VALUES"
                + " ('" + user.getUsername() + "',"
                + " ARRAY[" + user.getPublic_key().stream().map(Object::toString).collect(Collectors.joining(",")) + "]"
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
