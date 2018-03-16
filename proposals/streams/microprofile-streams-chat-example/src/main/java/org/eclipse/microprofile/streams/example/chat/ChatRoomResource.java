package org.eclipse.microprofile.streams.example.chat;

import org.eclipse.microprofile.streams.*;
import org.eclipse.microprofile.streams.example.chat.models.ChatMessage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

@ApplicationScoped
@Path("chat")
public class ChatRoomResource {

  private final IncomingStream<Envelope<ChatMessage>> incomingStream;
  private final OutgoingStream<ChatMessage> outgoingStream;

  @Inject
  public ChatRoomResource(
      @Incoming(topic = "default") IncomingStream<Envelope<ChatMessage>> incomingStream,
      @Outgoing(topic = "default") OutgoingStream<ChatMessage> outgoingStream
  ) {
    this.incomingStream = incomingStream;
    this.outgoingStream = outgoingStream;
  }

  @GET
  @Path("rooms/{name}/stream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void connectToRoom(
      @PathParam("name") String chatRoomName,
      @HeaderParam(HttpHeaders.LAST_EVENT_ID_HEADER) @DefaultValue("-1") long lastEventId,
      @Context SseEventSink sseSink, @Context Sse sse) {

    IncomingStream<Envelope<ChatMessage>> roomStream = incomingStream.withTopic(chatRoomName);
    if (lastEventId > -1) {
      roomStream = roomStream.fromOffset(lastEventId + 1);
    }

    roomStream.subscribe(new Flow.Subscriber<>() {
      private Flow.Subscription subscription;

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        Objects.requireNonNull(subscription);
        if (this.subscription != null) {
          subscription.cancel();
        } else {
          this.subscription = subscription;
          subscription.request(1);
        }
      }

      @Override
      public void onNext(Envelope<ChatMessage> item) {
        Objects.requireNonNull(item);

        if (sseSink.isClosed()) {
          subscription.cancel();
        } else {
          OutboundSseEvent.Builder sseEvent = sse.newEventBuilder()
              .data(item.getPayload())
              .mediaType(MediaType.APPLICATION_JSON_TYPE)
              .name("message");

          if (item.offset().isPresent()) {
            sseEvent = sseEvent.id(Long.toString(item.offset().get()));
          }

          sseSink.send(sseEvent.build())
              .whenComplete((v, error) -> {
                if (error == null) {
                  subscription.request(1);
                } else {
                  error.printStackTrace();
                  subscription.cancel();
                }
              });
        }
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        sseSink.close();
      }

      @Override
      public void onComplete() {
        sseSink.close();
      }
    });
  }

  @POST
  @Path("rooms/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> postMessage(
      @PathParam("name") String chatRoomName,
      ChatMessage chatMessage
  ) {
    return outgoingStream.withTopic(chatRoomName).publish(chatMessage)
        .thenApply(v -> Response.accepted().build());
  }

  @GET
  @Path("rooms/{name}")
  @Produces(MediaType.TEXT_HTML)
  public InputStream index(
      @PathParam("name") String chatRoomName
  ) {
    return getClass().getClassLoader().getResourceAsStream("chat/index.html");
  }
}
