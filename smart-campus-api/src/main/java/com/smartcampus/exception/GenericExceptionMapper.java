package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable t) {
        if (t instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) t;
            int status = wae.getResponse().getStatus();
            String code = Response.Status.fromStatusCode(status) != null
                    ? Response.Status.fromStatusCode(status).name()
                    : "HTTP_ERROR";
            ErrorResponse body = new ErrorResponse(status, code, t.getMessage());
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(body)
                    .build();
        }

        LOG.log(Level.SEVERE, "Unhandled exception caught by GenericExceptionMapper", t);
        ErrorResponse body = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact the API administrator."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
