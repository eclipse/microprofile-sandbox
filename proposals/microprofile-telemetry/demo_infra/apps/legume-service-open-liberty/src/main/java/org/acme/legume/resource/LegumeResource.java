/**
 *
 * @author Bruno Baptista.
 * <a href="https://twitter.com/brunobat_">https://twitter.com/brunobat_</a>
 *
 */

package org.acme.legume.resource;

import org.acme.legume.data.LegumeItem;
import org.acme.legume.data.LegumeNew;
import org.acme.legume.model.Legume;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@ApplicationScoped
public class LegumeResource  {

    @Inject
    EntityManager manager;

//    @Inject
//    MessageSender messageSender;

    @Transactional
    public Response provision() {
        final LegumeNew carrot = LegumeNew.builder()
                .name("Carrot")
                .description("Root vegetable, usually orange")
                .build();
        final LegumeNew zucchini = LegumeNew.builder()
                .name("Zucchini")
                .description("Summer squash")
                .build();
        return Response.status(CREATED).entity(asList(
                addLegume(carrot),
                addLegume(zucchini))).build();
    }

    @Transactional
    public Response add(@Valid final LegumeNew legumeNew) {
        return Response.status(CREATED).entity(addLegume(legumeNew)).build();
    }

    @Transactional
    public Response delete(@NotEmpty final String legumeId) {
        return find(legumeId)
                .map(legume -> {
                    manager.remove(legume);
                    return Response.status(NO_CONTENT).build();
                })
                .orElse(Response.status(NOT_FOUND).build());
    }

    @Fallback(fallbackMethod = "fallback")
    @Timeout(500)
    public List<Legume> list() {

        return manager.createQuery("SELECT l FROM Legume l").getResultList();
    }

    /**
     * To be used in case of exception or timeout
     *
     * @return a list of alternative legumes.
     */
    public List<Legume> fallback() {
        return singletonList(Legume.builder()
                                   .name("Failed Legume")
                                   .description("Fallback answer due to timeout")
                                   .build());
    }

    private Optional<Legume> find(final String legumeId) {
        return Optional.ofNullable(manager.find(Legume.class, legumeId));
    }

    private LegumeItem addLegume(final @Valid LegumeNew legumeNew) {
        final Legume legumeToAdd = Legume.builder()
                .name(legumeNew.getName())
                .description((legumeNew.getDescription()))
                .build();

        final Legume addedLegume = manager.merge(legumeToAdd);

        final LegumeItem legumeItem = LegumeItem.builder()
                .id(addedLegume.getId())
                .name(addedLegume.getName())
                .description(addedLegume.getDescription())
                .build();

//        messageSender.send(legumeItem);
        return legumeItem;
    }
}
