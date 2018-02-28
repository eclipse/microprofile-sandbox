package org.eclipse.microprofile.streams.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.Flow;

class IngestSubscriber<T> {
  private final String topic;
  private final Method method;
  private final Type messageType;

  public IngestSubscriber(String topic, Method method, Type messageType) {
    this.topic = topic;
    this.method = method;
    this.messageType = messageType;
  }

  public String getTopic() {
    return topic;
  }

  public Type getMessageType() {
    return messageType;
  }

  public Flow.Subscriber<?> subscriber(T t) throws InvocationTargetException, IllegalAccessException {
    return (Flow.Subscriber<?>) method.invoke(t);
  }
}
