package test;

import lombok.Data;

import javax.ws.rs.core.MediaType;
import java.net.URI;

@Data
public class ProblemDetail {
    public static final MediaType JSON_MEDIA_TYPE = MediaType.valueOf("application/problem+json");

    private URI type;
    private String title;
    private String detail;
    private Integer status;
    private URI instance;
}
