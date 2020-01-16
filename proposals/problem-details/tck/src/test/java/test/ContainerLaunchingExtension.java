package test;

import com.github.t1.testcontainers.jee.JeeContainer;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.function.Consumer;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

class ContainerLaunchingExtension implements Extension, BeforeAllCallback {
    private static URI BASE_URI = null;

    /**
     * Stopping is done by the ryuk container... see
     * https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
     */
    @Override public void beforeAll(ExtensionContext context) {
        if (System.getProperty("testcontainer-running") != null) {
            BASE_URI = URI.create(System.getProperty("testcontainer-running"));
        } else if (BASE_URI == null) {
            JeeContainer container = JeeContainer.create()
                .withDeployment("target/problem-details-test.war");
            container.start();
            BASE_URI = container.baseUri();
        }
    }

    public static ProblemDetailAssert<ProblemDetail> testPost(String path) {
        return thenProblemDetail(post(path));
    }

    public static <T extends ProblemDetail> ProblemDetailAssert<T> testPost(String path, Class<T> type) {
        return thenProblemDetail(post(path), type);
    }

    public static ProblemDetailAssert<ProblemDetail> testPost(String path, String accept) {
        return thenProblemDetail(target(path).request(MediaType.valueOf(accept)).post(null));
    }

    public static ProblemDetailAssert<ProblemDetail> testPost(String path, String accept1, String accept2) {
        return thenProblemDetail(target(path).request(MediaType.valueOf(accept1), MediaType.valueOf(accept2)).post(null));
    }

    public static <T> ResponseAssert<T> testPost(String path, String accept, Class<T> type) {
        return new ResponseAssert<>(target(path).request(MediaType.valueOf(accept)).post(null), type);
    }

    public static Response post(String path) {
        return target(path).request(APPLICATION_JSON_TYPE).post(null);
    }

    public static WebTarget target(String path) {
        return target().path(path);
    }

    private static final Client CLIENT = ClientBuilder.newClient();

    public static WebTarget target() {
        return CLIENT.target(BASE_URI);
    }

    public static ProblemDetailAssert<ProblemDetail> thenProblemDetail(Response response) {
        return thenProblemDetail(response, ProblemDetail.class);
    }

    public static <T extends ProblemDetail> ProblemDetailAssert<T> thenProblemDetail(Response response, Class<T> type) {
        return new ProblemDetailAssert<>(response, type);
    }

    public static class ProblemDetailAssert<T extends ProblemDetail> extends ResponseAssert<T> {
        public ProblemDetailAssert(Response response, Class<T> type) { super(response, type); }

        @Override public ProblemDetailAssert<T> hasStatus(Status status) {
            super.hasStatus(status);
            assertThat(entity.getStatus()).describedAs("problem-detail.status")
                .isEqualTo(status.getStatusCode());
            return this;
        }

        @Override public ProblemDetailAssert<T> hasContentType(String contentType) {
            super.hasContentType(contentType);
            return this;
        }

        @Override public ProblemDetailAssert<T> hasContentType(MediaType contentType) {
            super.hasContentType(contentType);
            return this;
        }


        public ProblemDetailAssert<T> hasType(String type) {
            assertThat(entity.getType()).describedAs("problem-detail.type")
                .isEqualTo(URI.create(type));
            return this;
        }

        public ProblemDetailAssert<T> hasTitle(String title) {
            assertThat(entity.getTitle()).describedAs("problem-detail.title")
                .isEqualTo(title);
            return this;
        }

        public ProblemDetailAssert<T> hasDetail(String detail) {
            assertThat(getDetail()).describedAs("problem-detail.detail")
                .isEqualTo(detail);
            return this;
        }

        public String getDetail() {
            return entity.getDetail();
        }

        public ProblemDetailAssert<T> hasUuidInstance() {
            assertThat(entity.getInstance()).describedAs("problem-detail.instance")
                .has(new Condition<>(instance -> instance.toString().startsWith("urn:uuid:"), "some uuid urn"));
            return this;
        }

        public void check(Consumer<T> consumer) {
            consumer.accept(entity);
        }
    }

    public static class ResponseAssert<T> {
        protected final Response response;
        protected final T entity;

        public ResponseAssert(Response response, Class<T> type) {
            this.response = response;
            assertThat(this.response.hasEntity()).describedAs("response has entity").isTrue();
            this.entity = this.response.readEntity(type);
        }

        public ResponseAssert<T> hasStatus(Status status) {
            assertThat(response.getStatusInfo()).describedAs("response status")
                .isEqualTo(status);
            return this;
        }

        public ResponseAssert<T> hasContentType(String contentType) {
            return hasContentType(MediaType.valueOf(contentType));
        }

        public ResponseAssert<T> hasContentType(MediaType contentType) {
            assertThat(response.getMediaType().isCompatible(contentType))
                .describedAs("response content type [" + response.getMediaType() + "] "
                    + "is not compatible with [" + contentType + "]").isTrue();
            return this;
        }

        @SuppressWarnings("UnusedReturnValue") public ResponseAssert<T> hasBody(T entity) {
            assertThat(this.entity).isEqualTo(entity);
            return this;
        }
    }
}
