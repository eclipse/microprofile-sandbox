/**
 *
 * @author Bruno Baptista.
 * <a href="https://twitter.com/brunobat_">https://twitter.com/brunobat_</a>
 *
 */

package org.acme.legume.resource;

import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class EntityManagerProducer {

    @PersistenceContext()
    private EntityManager entityManager;

    @Produces
    public EntityManager entityManager() {
        return entityManager;
    }


}
