package test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_JSON;
import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_XML;
import static test.ContainerLaunchingExtension.testPost;

@ExtendWith(ContainerLaunchingExtension.class)
class StandardExceptionMappingIT {
    // TODO TomEE doesn't write some problem detail entities https://github.com/t1/problem-details/issues/17
    @DisabledIfSystemProperty(named = "jee-testcontainer", matches = "tomee")
    @Test void shouldMapClientWebApplicationExceptionWithoutEntityOrMessage() {
        testPost("standard/plain-bad-request")
            .hasStatus(BAD_REQUEST)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:bad-request")
            .hasTitle("Bad Request")
            .hasDetail(null)
            .hasUuidInstance();
    }

    // TODO TomEE doesn't write some problem detail entities https://github.com/t1/problem-details/issues/17
    @DisabledIfSystemProperty(named = "jee-testcontainer", matches = "tomee")
    @Test void shouldMapClientWebApplicationExceptionWithoutEntityButMessage() {
        testPost("/standard/bad-request-with-message")
            .hasStatus(BAD_REQUEST)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:bad-request")
            .hasTitle("Bad Request")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Test void shouldUseEntityFromWebApplicationException() {
        testPost("/standard/bad-request-with-text-response", TEXT_PLAIN, String.class)
            .hasStatus(BAD_REQUEST)
            .hasContentType(TEXT_PLAIN)
            .hasBody("the body");
    }

    // TODO TomEE doesn't write some problem detail entities https://github.com/t1/problem-details/issues/17
    @DisabledIfSystemProperty(named = "jee-testcontainer", matches = "tomee")
    @Test void shouldMapServerWebApplicationExceptionWithoutEntityOrMessage() {
        testPost("/standard/plain-service-unavailable")
            .hasStatus(SERVICE_UNAVAILABLE)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:service-unavailable")
            .hasTitle("Service Unavailable")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapIllegalArgumentExceptionWithoutMessage() {
        testPost("/standard/illegal-argument-without-message")
            .hasStatus(BAD_REQUEST)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:illegal-argument")
            .hasTitle("Illegal Argument")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapIllegalArgumentExceptionWithMessage() {
        testPost("/standard/illegal-argument-with-message")
            .hasStatus(BAD_REQUEST)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:illegal-argument")
            .hasTitle("Illegal Argument")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Test void shouldMapNullPointerExceptionWithoutMessage() {
        testPost("/standard/npe-without-message")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapNullPointerExceptionWithMessage() {
        testPost("/standard/npe-with-message")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Disabled
    @Test void shouldMapToXml() {
        testPost("/standard/npe-with-message", APPLICATION_XML)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_XML)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Disabled
    @Test void shouldMapToSecondAcceptXml() {
        testPost("/standard/npe-with-message", TEXT_PLAIN, APPLICATION_XML)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_XML)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }
}
