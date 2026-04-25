package com.smartcampus.resource;

import com.smartcampus.model.SensorReading;
import com.smartcampus.service.SensorReadingService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final SensorReadingService readingService = new SensorReadingService();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> history() {
        return readingService.getHistory(sensorId);
    }

    @POST
    public Response add(SensorReading body, @Context UriInfo uriInfo) {
        if (body == null) {
            throw new BadRequestException("Reading body is required.");
        }
        readingService.addReading(sensorId, body);
        URI location = uriInfo.getAbsolutePathBuilder().path(body.getId()).build();
        return Response.created(location).entity(body).build();
    }
}
