/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package application;

import java.io.IOException;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

@Path("/")
public class JPAResource {

    /**
     * The JNDI name for the persistence context is the one defined in web.xml
     */
    private static final String JNDI_NAME = "java:comp/env/jpasample/entitymanager";

    private static String newline = System.getProperty("line.separator");

    @GET
    @Produces("text/plain")
    public String getInformation() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello JPA World").append(newline);

        try {
            // First create a Thing in the database, then retrieve it
            createThing(builder);
            retrieveThing(builder);
        } catch (Exception e) {
            builder.append("Something went wrong. Caught exception " + e).append(newline);
        }
        return builder.toString();
    }

    public void createThing(StringBuilder builder)
            throws NamingException, NotSupportedException, SystemException, IllegalStateException, SecurityException,
            HeuristicMixedException, HeuristicRollbackException, RollbackException {
        Context ctx = new InitialContext();
        // Before getting an EntityManager, start a global transaction
        UserTransaction tran = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        tran.begin();

        // Now get the EntityManager from JNDI
        EntityManager em = (EntityManager) ctx.lookup(JNDI_NAME);
        builder.append("Creating a brand new Thing with " + em.getDelegate().getClass()).append(newline);

        // Create a Thing object and persist it to the database
        Thing thing = new Thing();
        em.persist(thing);

        // Commit the transaction
        tran.commit();
        int id = thing.getId();
        builder.append("Created Thing " + id + ":  " + thing).append(newline);
    }

    @SuppressWarnings("unchecked")
    public void retrieveThing(StringBuilder builder) throws SystemException, NamingException {
        // Look up the EntityManager in JNDI
        Context ctx = new InitialContext();
        EntityManager em = (EntityManager) ctx.lookup(JNDI_NAME);
        // Compose a JPQL query
        String query = "SELECT t FROM Thing t";
        Query q = em.createQuery(query);

        // Execute the query
        List<Thing> things = q.getResultList();
        builder.append("Query returned " + things.size() + " things").append(newline);

        // Let's see what we got back!
        for (Thing thing : things) {
            builder.append("Thing in list " + thing).append(newline);
        }
    }
}
