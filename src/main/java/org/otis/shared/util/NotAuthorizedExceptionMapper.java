package org.otis.shared.util;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

	@Override
	public Response toResponse(NotAuthorizedException exception) {
		String message = exception.getMessage();
		String reason = (message != null && !message.isBlank()) ? message : "Authentication failed";

		Throwable cause = exception.getCause();
		if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
			reason = cause.getMessage();
		}

		return Response.status(Response.Status.UNAUTHORIZED)
				.type(MediaType.APPLICATION_JSON)
				.entity(new JsonObject()
						.put("error", "Unauthorized")
						.put("message", reason))
				.build();
	}
}
