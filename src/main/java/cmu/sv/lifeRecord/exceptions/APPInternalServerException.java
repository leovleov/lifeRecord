package cmu.sv.lifeRecord.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Create 500
 */
public class APPInternalServerException extends WebApplicationException {
    public APPInternalServerException(int errorCode, String errorMessage) {
        super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new APPExceptionInfo(
                Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                errorCode,
                errorMessage)
        ).type("application/json").build());
    }
}