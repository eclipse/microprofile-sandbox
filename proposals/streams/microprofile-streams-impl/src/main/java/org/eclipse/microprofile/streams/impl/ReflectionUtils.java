package org.eclipse.microprofile.streams.impl;

import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.inject.spi.DefinitionException;
import java.lang.reflect.Type;
import java.util.function.Supplier;

class ReflectionUtils {
  static Type[] getTypeArguments(Type type, Supplier<String> message) {
    Type[] typeArguments = Reflections.getActualTypeArguments(type);
    if (typeArguments == null || typeArguments.length == 0) {
      throw new DefinitionException(message.get() + " is not parameterized.");
    }
    return typeArguments;
  }
}
