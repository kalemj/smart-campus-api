package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.service.SensorService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorService sensorService = new SensorService();

    @GET
    public List<Sensor> list(@QueryParam("type") String type) {
        return sensorService.listSensors(type);
    }

    @GET
    @Path("{sensorId}")
    public Sensor get(@PathParam("sensorId") String sensorId) {
        return sensorService.getSensor(sensorId);
    }

    @POST
    public Response create(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null) {
            throw new BadRequestException("Sensor body is required.");
        }
        sensorService.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @Path("{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorService.getSensor(sensorId);
        if (sensor == null) {
            throw new BadRequestException("Sensor not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
