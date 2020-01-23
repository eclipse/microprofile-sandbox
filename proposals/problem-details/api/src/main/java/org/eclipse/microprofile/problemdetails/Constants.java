package org.eclipse.microprofile.problemdetails;

import javax.ws.rs.core.MediaType;

public class Constants {
    /**
     * The JSON formatted details body of a failing http request.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7807">RFC-7807</a>
     */
    public static final String PROBLEM_DETAIL_JSON = "application/problem+json";
    /**
     * The JSON formatted details body of a failing http request.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7807">RFC-7807</a>
     */
    public static final MediaType PROBLEM_DETAIL_JSON_TYPE = MediaType.valueOf(PROBLEM_DETAIL_JSON);

    /**
     * The XML formatted details body of a failing http request.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7807">RFC-7807</a>
     */
    public static final String PROBLEM_DETAIL_XML = "application/problem+xml";
    /**
     * The XML formatted details body of a failing http request.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7807">RFC-7807</a>
     */
    public static final MediaType PROBLEM_DETAIL_XML_TYPE = MediaType.valueOf(PROBLEM_DETAIL_XML);
}
