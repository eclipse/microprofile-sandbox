package org.eclipse.microprofile.streams.example.chat;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MyExceptionMapper implements ExceptionMapper<Throwable> {
  {
    System.out.println("HI!!!");
  }

  @Override
  public Response toResponse(Throwable exception) {
    exception.printStackTrace();
    return Response.status(500).build();
  }
}
