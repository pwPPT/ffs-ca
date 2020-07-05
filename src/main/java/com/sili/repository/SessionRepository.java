package com.sili.repository;

import com.sili.model.SessionTO;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class SessionRepository {

    private static final String TABLE_NAME = "ffs_ca.sessions";

    private final io.vertx.mutiny.pgclient.PgPool client;

    private static SessionTO from(Row row) {
        System.out.println("SESSION WAS SAVED");
        return SessionTO.of(
            row.getUUID("token"),
            row.getInteger("user_id").longValue(),
            row.getLocalDateTime("authentication_time")
        );
    }

    public Uni<SessionTO> saveSession(SessionTO session) {
        System.out.println("EXECUTE SESSION INSERT");
        return client.preparedQuery(
            "INSERT INTO " + TABLE_NAME
                + " (token, user_id) VALUES"
                + " ('" + session.getToken() + "',"
                + " " + session.getUserId()
                + ")  RETURNING *")
            .execute()
            .onItem().apply(RowSet::iterator)
            .onItem().invoke(iter -> System.out.println("NEXT: " + iter.hasNext()))
            .onItem().apply(iterator ->
                iterator.hasNext()
                    ? from(iterator.next())
                    : null
            );
    }
}
