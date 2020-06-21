package com.sili.controller;

import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor

@Path("/api/ca")
@Produces(MediaType.APPLICATION_JSON)
public class Alpha {

    @GET
    @Path("/auth")
    public Uni<Response> authenticate() {
        return Uni.createFrom().item(Response.ok("Authenticated").build());
    }

}
