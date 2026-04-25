package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.service.RoomService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final RoomService roomService = new RoomService();

    @GET
    public Collection<Room> list() {
        return roomService.listRooms();
    }

    @GET
    @Path("{roomId}")
    public Room get(@PathParam("roomId") String roomId) {
        return roomService.getRoom(roomId);
    }

    @POST
    public Response create(Room room, @Context UriInfo uriInfo) {
        if (room == null) {
            throw new BadRequestException("Room body is required.");
        }
        roomService.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @DELETE
    @Path("{roomId}")
    public Response delete(@PathParam("roomId") String roomId) {
        roomService.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
