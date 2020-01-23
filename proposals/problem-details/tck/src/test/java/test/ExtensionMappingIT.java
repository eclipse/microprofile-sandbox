package test;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.BDDAssertions.then;
import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_JSON;
import static test.ContainerLaunchingExtension.testPost;

@ExtendWith(ContainerLaunchingExtension.class)
class ExtensionMappingIT {

    @Test void shouldMapExtensionStringMethod() {
        testPost("/custom/extension-method", ProblemDetailWithExtensionString.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some")
            .hasTitle("Some")
            .hasDetail(null)
            .hasUuidInstance()
            .check(detail -> then(detail.ex).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringMethodWithAnnotatedName() {
        testPost("/custom/extension-method-with-name", ProblemDetailWithExtensionStringFoo.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .check(detail -> then(detail.foo).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringField() {
        testPost("/custom/extension-field", ProblemDetailWithExtensionString.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .check(detail -> then(detail.ex).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringFieldWithAnnotatedName() {
        testPost("/custom/extension-field-with-name", ProblemDetailWithExtensionStringFoo.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .check(detail -> then(detail.foo).isEqualTo("some extension"));
    }

    @Test void shouldMapMultiplePackagePrivateExtensions() {
        testPost("/custom/multi-extension", ProblemDetailWithMultipleExtensions.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .check(detail -> {
                then(detail.m1).isEqualTo("method 1");
                then(detail.m2).isEqualTo("method 2");
                then(detail.f1).isEqualTo("field 1");
                then(detail.f2).isEqualTo("field 2");
            });
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ProblemDetailWithExtensionString extends ProblemDetail {
        private String ex;
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ProblemDetailWithExtensionStringFoo extends ProblemDetail {
        private String foo;
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ProblemDetailWithMultipleExtensions extends ProblemDetail {
        private String m1;
        private String m2;
        private String f1;
        private String f2;
    }
}
