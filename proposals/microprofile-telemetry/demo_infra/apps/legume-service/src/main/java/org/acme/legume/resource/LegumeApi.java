/**
 *
 * @author Bruno Baptista.
 * <a href="https://twitter.com/brunobat_">https://twitter.com/brunobat_</a>
 *
 */

package org.acme.legume.resource;

import org.acme.legume.data.LegumeNew;
import org.acme.legume.model.Legume;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/legumes")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public interface LegumeApi {

    @POST
    @Path("/init")
    @Operation(
            operationId = "ProvisionLegumes",
            summary = "Add default legumes to the Database"
    )
    @APIResponse(
            responseCode = "201",
            description = "Default legumes created"
    )
    @APIResponse(
            name = "notFound",
            responseCode = "404",
            description = "Legume provision not found"
    )
    @APIResponse(
            name = "internalError",
            responseCode = "500",
            description = "Internal Server Error"
    )
    Response provision();

    @POST
    @Operation(
            operationId = "AddLegume",
            summary = "Add a Legume"
    )
    @RequestBody(
            content = @Content(
                    mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = LegumeNew.class, ref = "legume_new")),
            description = "The Legume to create",
            required = true
    )
    @APIResponse(
            responseCode = "201",
            description = "Legume created",
            content = @Content(
                    mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = Legume.class, ref = "error"))
    )
    @APIResponse(
            name = "notFound",
            responseCode = "400",
            description = "Legume data is invalid"
    )
    @APIResponse(
            name = "notFound",
            responseCode = "404",
            description = "Legume provision not found"
    )
    @APIResponse(
            name = "internalError",
            responseCode = "500",
            description = "Internal Server Error"
    )
    public Response add(@Valid final LegumeNew legume);

    @DELETE
    @Path("{id}")
    @Operation(
            operationId = "DeleteLegume",
            summary = "Delete a Legume"
    )
    @APIResponse(
            responseCode = "204",
            description = "Empty response"
    )
    @APIResponse(
            name = "notFound",
            responseCode = "404",
            description = "Legume not found"
    )
    @APIResponse(
            name = "internalError",
            responseCode = "500",
            description = "Internal Server Error"
    )
    Response delete(
            @Parameter(name = "id",
                    description = "Id of the Legume to delete",
                    required = true,
                    example = "81471222-5798-11e9-ae24-57fa13b361e1",
                    schema = @Schema(description = "uuid", required = true))
            @PathParam("id")
            @NotEmpty final String legumeId);

    @Operation(
            operationId = "ListLegumes",
            summary = "List all legumes"
    )
    @APIResponse(
            responseCode = "200",
            description = "The List with all legumes"
    )
    @APIResponse(
            name = "notFound",
            responseCode = "404",
            description = "Legume list not found"
    )
    @APIResponse(
            name = "internalError",
            responseCode = "500",
            description = "Internal Server Error"
    )
    @GET
    List<Legume> list();
}
