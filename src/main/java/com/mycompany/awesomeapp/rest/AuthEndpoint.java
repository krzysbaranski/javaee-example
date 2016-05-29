package com.mycompany.awesomeapp.rest;

import com.mycompany.awesomeapp.rest.security.JsonWebTokenGenerator;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/auth")
public class AuthEndpoint {

    @GET
    @Produces("text/plain")
    public Response doGet() {

        try {
            String jsonWebToken = JsonWebTokenGenerator.INSTANCE.newToken("krzys");
            return Response.ok("method doGet invoked " + jsonWebToken).build();
        } catch (Exception e) {
            return Response.status(500).build();
        }
    }

    @POST
    @Consumes({"text/plain", "application/json"})
    public Response doPost(String entity) {
        Response response = Response.created(
                UriBuilder.fromResource(AuthEndpoint.class).build()).build();
        return response;
    }

    @PUT
    @Consumes({"text/plain", "application/json"})
    public Response doPut(
            @Context HttpHeaders httpHeaders,
            String entity) {
        String auth = httpHeaders.getRequestHeader("Authorization").iterator().next();

        try {
            String decoded = JsonWebTokenGenerator.INSTANCE.verifyJsonWebToken(auth);
            return Response.ok("method doPut invoked " + decoded).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
//		return Response.created(
//				UriBuilder.fromResource(AuthEndpoint.class).build()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response doDelete(@PathParam("id") Long id) {
        return Response.noContent().build();
    }
}