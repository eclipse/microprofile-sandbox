/*
 * Copyright (c) 2016-2018 Contributors to the Eclipse Foundation
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.eclipse.microprofile.jwt.tck.config;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.microprofile.jwt.tck.TCKConstants.TEST_GROUP_CONFIG;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.microprofile.jwt.config.Names;
import org.eclipse.microprofile.jwt.tck.TCKConstants;
import org.eclipse.microprofile.jwt.tck.util.MpJwtTestVersion;
import org.eclipse.microprofile.jwt.tck.util.TokenUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

/**
 * Validate that config property values of type resource path to JWK works to validate the JWT which is signed with
 * privateKey4k.pem
 */
public class PublicKeyAsJWKLocationTest extends Arquillian {

    /**
     * The base URL for the container under test
     */
    @ArquillianResource
    private URL baseURL;

    /**
     * Create a CDI aware base web application archive that includes an embedded PEM public key that is referenced via
     * the mp.jwt.verify.publickey.location as an embedded resource property. The root url is /jwks
     * 
     * @return the base base web application archive
     * @throws IOException
     *             - on resource failure
     */
    @Deployment()
    public static WebArchive createLocationDeployment() throws IOException {
        URL publicKey = PublicKeyAsJWKLocationTest.class.getResource("/publicKey4k.pem");
        URL signerJwk = PublicKeyAsJWKLocationTest.class.getResource("/signer-key4k.jwk");
        // Setup the microprofile-config.properties content
        Properties configProps = new Properties();
        // Location points to the JWKS bundled in the deployment
        configProps.setProperty(Names.VERIFIER_PUBLIC_KEY_LOCATION, "/signer-key4k.jwk");
        configProps.setProperty(Names.ISSUER, TCKConstants.TEST_ISSUER);
        StringWriter configSW = new StringWriter();
        configProps.store(configSW, "PublicKeyAsJWKLocationTest microprofile-config.properties");
        StringAsset configAsset = new StringAsset(configSW.toString());
        WebArchive webArchive = ShrinkWrap
                .create(WebArchive.class, "PublicKeyAsJWKLocationTest.war")
                .addAsManifestResource(new StringAsset(MpJwtTestVersion.MPJWT_V_1_1.name()),
                        MpJwtTestVersion.MANIFEST_NAME)
                .addAsResource(publicKey, "/publicKey4k.pem")
                .addAsResource(publicKey, "/publicKey.pem")
                .addAsResource(signerJwk, "/signer-key4k.jwk")
                .addClass(PublicKeyEndpoint.class)
                .addClass(JwksApplication.class)
                .addClass(SimpleTokenUtils.class)
                .addAsWebInfResource("beans.xml", "beans.xml")
                .addAsManifestResource(configAsset, "microprofile-config.properties");
        System.out.printf("WebArchive: %s\n", webArchive.toString(true));
        return webArchive;
    }

    @RunAsClient
    @Test(groups = TEST_GROUP_CONFIG, description = "Validate specifying the mp.jwt.verify.publickey.location as resource path to a JWK key")
    public void testKeyAsLocation() throws Exception {
        Reporter.log("testKeyAsLocation, expect HTTP_OK");

        PrivateKey privateKey = TokenUtils.readPrivateKey("/privateKey4k.pem");
        String kid = "publicKey4k";
        HashMap<String, Long> timeClaims = new HashMap<>();
        String token = TokenUtils.generateTokenString(privateKey, kid, "/Token1.json", null, timeClaims);

        String uri = baseURL.toExternalForm() + "jwks/endp/verifyKeyLocationAsJWKResource";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri)
                .queryParam("kid", kid);
        Response response =
                echoEndpointTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        String replyString = response.readEntity(String.class);
        JsonReader jsonReader = Json.createReader(new StringReader(replyString));
        JsonObject reply = jsonReader.readObject();
        Reporter.log(reply.toString());
        Assert.assertTrue(reply.getBoolean("pass"), reply.getString("msg"));
    }
}
