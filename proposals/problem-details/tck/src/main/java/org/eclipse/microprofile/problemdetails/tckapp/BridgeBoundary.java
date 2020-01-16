package org.eclipse.microprofile.problemdetails.tckapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Slf4j
@Path("/bridge")
public class BridgeBoundary {
    private static final String BASE_URI = "http://localhost:8080/problem-details-tck";
    private static final Entity<String> EMPTY = Entity.entity("{}", APPLICATION_JSON_TYPE);

    /** how to call the target */
    public enum Mode {
        /** JAX-RS WebTarget */
        jaxRs,

        /** Manually build a Microprofile Rest Client */
        mMpRest,

        /** Injected Microprofile Rest Client */
        iMpRest
    }

    /** how should the target behave */
    public enum State {ok, fails}

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Reply {
        private String value;
    }

    public static class ApiException extends IllegalArgumentException {}

    @Inject @RestClient API target;

    private final Client rest = ClientBuilder.newClient();

    @RegisterRestClient(baseUri = BASE_URI)
    public interface API {
        @Path("/bridge/target/{state}")
        @GET Reply request(@PathParam("state") State state) throws ApiException;
    }

    @Path("/indirect/{state}")
    @GET public Reply indirect(@PathParam("state") State state, @NotNull @QueryParam("mode") Mode mode) {
        log.debug("call indirect {} :: {}", state, mode);

        API target = target(mode);

        try {
            Reply reply = target.request(state);
            log.debug("indirect call reply {}", reply);
            return reply;
        } catch (RuntimeException e) {
            log.debug("indirect call exception", e);
            throw e;
        }
    }

    private API target(@QueryParam("mode") Mode mode) {
        switch (mode) {
            case jaxRs:
                return this::jaxRsCall;
            case mMpRest:
                return RestClientBuilder.newBuilder().baseUri(URI.create(BASE_URI)).build(API.class);
            case iMpRest:
                return this.target;
        }
        throw new UnsupportedOperationException();
    }

    private Reply jaxRsCall(State state) {
        // ProblemDetailExceptionRegistry.register(ApiException.class);
        return rest.target(BASE_URI)
            // .register(ProblemDetailClientResponseFilter.class)
            .path("/bridge/target")
            .path(state.toString())
            .request(APPLICATION_JSON_TYPE)
            .get(Reply.class);
    }

    @Path("/target/{state}")
    @GET public Reply target(@PathParam("state") State state) {
        log.debug("target {}", state);
        switch (state) {
            case ok:
                return new Reply("okay");
            case fails:
                throw new ApiException();
        }
        throw new UnsupportedOperationException();
    }
}
