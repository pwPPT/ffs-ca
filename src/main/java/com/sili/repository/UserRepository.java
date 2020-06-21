package com.sili.repository;

import com.sili.model.UserTO;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class UserRepository {

    private static final String TABLE_NAME = "ffs_ca.users";

    private final io.vertx.mutiny.pgclient.PgPool client;

    private static UserTO from(Row row) {
        return new UserTO(
            row.getString("username")
        );
    }

    public Uni<UserTO> getUserByUsername(String username) {
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

    public Uni<UserTO> registerUser(UserTO user) {
        return client.preparedQuery(
            "INSERT INTO " + TABLE_NAME
                + " (username) VALUES"
                + " ('" + user.getUsername()
                + "')  RETURNING *")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? from(iterator.next())
                    : null
            );
    }
}
