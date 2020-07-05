package com.sili.controller;

import com.sili.exceptions.StateNotFoundException;
import com.sili.exceptions.UnauthorizedException;
import com.sili.exceptions.UserAlreadyExistException;
import com.sili.exceptions.UserNotFoundException;
import com.sili.model.RegisterTO;
import com.sili.model.SessionRequestTO;
import com.sili.model.UserTO;
import com.sili.model.XValueTO;
import com.sili.model.YValueTO;
import com.sili.service.AuthService;
import com.sili.service.RegisterService;
import com.sili.service.SessionService;
import io.smallrye.mutiny.Uni;
import io.vertx.pgclient.PgException;
import java.time.Duration;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.logging.Logger;

@ApplicationScoped
@AllArgsConstructor

@Path("/api/ca")
@Produces(MediaType.APPLICATION_JSON)
public class Alpha {

    private final RegisterService registerService;
    private final AuthService authService;
    private final SessionService sessionService;
    private static final Logger LOGGER = Logger.getLogger(Alpha.class);

    @POST
    @Path("/token")
    public Uni<Response> getToken(@RequestBody UserTO entity) {
        return Uni.createFrom().item(entity)
            .onItem().produceUni(authService::generateToken)
            .onItem().produceUni(u -> mapToResponse(u, "token", Status.NOT_FOUND));
    }

    @POST
    @Path("/X")
    public Uni<Response> generateVectorA(@RequestBody XValueTO entity) {
        // set X value for corresponding token value in AUTH_STATE
        // generate random vector 'A' of 0 - 1 values, and update it in AUTH_STATE (len(vector) == len(public_key))
        // send back generated 'A'
        // response format: {"A": [...ints...]}
        return Uni.createFrom().item(entity)
            .onItem().produceUni(authService::generateAVector)
            .onItem().produceUni(u -> mapToResponse(u, "vectorA", Status.NOT_FOUND));
    }

    @POST
    @Path("/Y")
    public Uni<Response> authUser(@RequestBody YValueTO entity) {
        // verify user
        // update number of positive tries, and return response
        // response format:
        // { "repeat": <true if we want user to authenticate again, false otherwise>,
        //   "is_authenticated": <true if authentication is finished, false if not yet authenticated or authentication failed>,
        //   "session_id": <generated session id if authenticated, null otherwise> }
        return Uni.createFrom().item(entity)
            .onItem().produceUni(authService::authenticate)
            .onItem().produceUni(u -> mapToResponse(u, "auth", Status.UNAUTHORIZED));
    }

    @POST
    @Path("/secret")
    public Uni<Response> getSecret(@RequestBody SessionRequestTO entity) {
        return Uni.createFrom().item(entity)
            .onItem().produceUni(sessionService::getSecret)
            .onItem().produceUni(u -> mapToResponse(u, "secret", Status.UNAUTHORIZED));
    }

    @POST
    @Path("/register")
    public Uni<Response> register(@RequestBody RegisterTO entity) {
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
                .onItem().apply(Response.ResponseBuilder::build))

            .onFailure(UnauthorizedException.class)
            .recoverWithUni(Uni.createFrom()
                .item(Response.status(Status.UNAUTHORIZED))
                .onItem().apply(Response.ResponseBuilder::build))

            .onFailure(UserAlreadyExistException.class)
            .recoverWithUni(Uni.createFrom()
                .item(Response.status(Status.BAD_REQUEST))
                .onItem().apply(Response.ResponseBuilder::build))

            .onFailure(UserNotFoundException.class)
            .recoverWithUni(Uni.createFrom()
                .item(Response.status(Status.NOT_FOUND))
                .onItem().apply(Response.ResponseBuilder::build))

            .onFailure(StateNotFoundException.class)
            .recoverWithUni(Uni.createFrom()
                .item(Response.status(Status.NOT_FOUND))
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
