package com.sili.controller;

import com.sili.exceptions.UserAlreadyExistException;
import com.sili.model.UserTO;
import com.sili.service.AuthService;
import com.sili.service.RegisterService;
import io.smallrye.mutiny.Uni;
import io.vertx.pgclient.PgException;
import java.time.Duration;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@ApplicationScoped
@AllArgsConstructor

@Path("/api/ca")
@Produces(MediaType.APPLICATION_JSON)
public class Alpha {

    private final RegisterService registerService;
    private final AuthService authService;
    private static final Logger LOGGER = Logger.getLogger(Alpha.class);

    @GET
    @Path("/auth")
    public Uni<Response> authenticate() {
        return Uni.createFrom().item(Response.ok("Authenticated").build());
    }

    @GET
    @Path("/auth/{username}")
    public Uni<Response> authenticateUser(@PathParam String username) {
        return Uni.createFrom().item(username)
            .onItem().produceUni(authService::authenticateUser)
            .onItem().produceUni(u -> mapStringToResponse(u, "Authenticated " + username, "authenticateUser", Status.NOT_FOUND));
    }

    @POST
    @Path("/register")
    public Uni<Response> register(@RequestBody UserTO entity) {
        return Uni.createFrom().item(entity)
            .onItem().produceUni(registerService::registerUser)
            .onItem().produceUni(u -> mapToResponse(u, "register", Status.CONFLICT));
    }

    public Uni<Response> mapToResponse(Object obj, String methodName, Status nullStatus) {
        return Uni.createFrom().item(obj)
            .onItem().apply(o -> o != null
                ? Response.status(Status.OK).entity(o)
                : Response.status(nullStatus))
            .onItem().apply(Response.ResponseBuilder::build)

            .ifNoItem().after(Duration.ofMillis(500))
            .recoverWithUni(Uni.createFrom()
                .item(Response.status(Response.Status.REQUEST_TIMEOUT))
                .onItem().apply(Response.ResponseBuilder::build))

            .onFailure(PgException.class).apply(e -> {
                LOGGER.error("ERROR in " + methodName + ": " + e.getMessage());
                return e;
            })
            .onFailure(PgException.class)
            .recoverWithUni(Uni.createFrom()
                .item(Response.status(Response.Status.INTERNAL_SERVER_ERROR))
                .onItem().apply(Response.ResponseBuilder::build));
    }

    public Uni<Response> mapStringToResponse(Object obj, String message, String methodName, Status nullStatus) {
        return Uni.createFrom().item(obj)
            .onItem().apply(o -> o != null
                ? Response.status(Status.OK).entity(message)
                : Response.status(nullStatus))
            .onItem().apply(Response.ResponseBuilder::build)

            .ifNoItem().after(Duration.ofMillis(500))
            .recoverWithUni(Uni.createFrom()
                .item(Response.status(Response.Status.REQUEST_TIMEOUT))
                .onItem().apply(Response.ResponseBuilder::build))

            .onFailure(PgException.class).apply(e -> {
                LOGGER.error("ERROR in " + methodName + ": " + e.getMessage());
                return e;
            })
            .onFailure(PgException.class)
            .recoverWithUni(Uni.createFrom()
                .item(Response.status(Response.Status.INTERNAL_SERVER_ERROR))
                .onItem().apply(Response.ResponseBuilder::build));
    }
}
