package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_JSON;
import static test.ContainerLaunchingExtension.testPost;

@ExtendWith(ContainerLaunchingExtension.class)
class CustomExceptionIT {

    @Test void shouldMapCustomRuntimeException() {
        testPost("/custom/runtime-exception")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:custom")
            .hasTitle("Custom")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapCustomIllegalArgumentException() {
        testPost("/custom/illegal-argument-exception")
            .hasStatus(BAD_REQUEST)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:custom")
            .hasTitle("Custom")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitType() {
        testPost("/custom/explicit-type")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("http://error-codes.org/out-of-memory")
            .hasTitle("Some")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitTitle() {
        testPost("/custom/explicit-title")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some")
            .hasTitle("Some Title")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitStatus() {
        testPost("/custom/explicit-status")
            .hasStatus(FORBIDDEN)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:something-forbidden")
            .hasTitle("Something Forbidden")
            .hasDetail(null)
            .hasUuidInstance();
    }


    @Test void shouldMapDetailMethod() {
        testPost("/custom/public-detail-method")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapPrivateDetailMethod() {
        testPost("/custom/private-detail-method")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapFailingDetailMethod() {
        testPost("/custom/failing-detail-method")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:failing-detail")
            .hasTitle("Failing Detail")
            .hasDetail("could not invoke FailingDetailException.failingDetail: java.lang.RuntimeException: inner")
            .hasUuidInstance();
    }

    @Test void shouldMapPublicDetailFieldOverridingMessage() {
        testPost("/custom/public-detail-field")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapPrivateDetailField() {
        testPost("/custom/private-detail-field")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapMultipleDetailFields() {
        testPost("/custom/multi-detail-fields")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("detail a. detail b")
            .hasUuidInstance();
    }

    @Test void shouldMapDetailMethodAndTwoFields() {
        testPost("/custom/mixed-details")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("detail a. detail b. detail c")
            .hasUuidInstance();
    }

    @Test void shouldFailToMapDetailMethodTakingAnArgument() {
        testPost("/custom/detail-method-arg")
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("could not invoke SomeMessageException.detail: expected no args but got 1")
            .hasUuidInstance();
    }
}
