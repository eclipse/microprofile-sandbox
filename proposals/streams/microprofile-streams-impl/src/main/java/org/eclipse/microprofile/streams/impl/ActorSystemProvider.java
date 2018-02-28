package org.eclipse.microprofile.streams.impl;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class ActorSystemProvider {
  private final ActorSystem system;
  private final Materializer materializer;

  public ActorSystemProvider() {
    this.system = ActorSystem.create();
    this.materializer = ActorMaterializer.create(system);
  }

  @PreDestroy
  public void terminateActorSystem() {
    system.terminate();
  }

  @Produces
  public ActorSystem actorSystem() {
    return system;
  }

  @Produces
  public Materializer materializer() {
    return materializer;
  }

}
