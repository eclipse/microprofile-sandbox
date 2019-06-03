package com.example;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

//import com.ibm.json.java.JSON;
//import com.ibm.json.java.JSONArray;
//import com.ibm.json.java.JSONObject;

// This class define the RESTful API to fetch the database service information
// <basepath>/api/hello

@Path("/hello")
public class HelloResource {

    @GET
    @Produces("text/plain")
    public String getInformation() throws Exception, IOException {
        return "Hello World From Your Friends at Liberty Boost EE!";
    }
}
