package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> discover() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("apiName", "Smart Campus Sensor & Room Management API");
        body.put("version", "1.0.0");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("maintainer", "Smart Campus Backend Team");
        contact.put("email", "smart-campus-admin@westminster.ac.uk");
        body.put("contact", contact);

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("sensorReadings", "/api/v1/sensors/{sensorId}/readings");
        body.put("links", links);

        return body;
    }
}
