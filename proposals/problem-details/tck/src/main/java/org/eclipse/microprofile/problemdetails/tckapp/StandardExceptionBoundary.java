package org.eclipse.microprofile.problemdetails.tckapp;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/standard")
public class StandardExceptionBoundary {
    @Path("/plain-bad-request")
    @POST public void plainBadRequest() {
        throw new BadRequestException();
    }

    @Path("/bad-request-with-message")
    @POST public void badRequestWithMessage() {
        throw new BadRequestException("some message");
    }

    @Path("/bad-request-with-text-response")
    @POST public void badRequestWithResponse() {
        throw new BadRequestException(Response.status(BAD_REQUEST)
            .type(TEXT_PLAIN_TYPE).entity("the body").build());
    }

    @Path("/plain-service-unavailable")
    @POST public void plainServiceUnavailable() {
        throw new ServiceUnavailableException();
    }

    @Path("/illegal-argument-without-message")
    @POST public void illegalArgumentWithoutMessage() {
        throw new IllegalArgumentException();
    }

    @Path("/illegal-argument-with-message")
    @POST public void illegalArgumentWithMessage() {
        throw new IllegalArgumentException("some message");
    }

    @Path("/npe-without-message")
    @POST public void npeWithoutMessage() {
        throw new NullPointerException();
    }

    @Path("/npe-with-message")
    @POST public void npeWithMessage() {
        throw new NullPointerException("some message");
    }
}
