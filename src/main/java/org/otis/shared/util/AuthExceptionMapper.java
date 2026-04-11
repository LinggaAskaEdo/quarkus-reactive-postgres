package org.otis.shared.util;

import io.quarkus.security.AuthenticationFailedException;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

	@Override
	public Response toResponse(AuthenticationFailedException exception) {
		String message = exception.getMessage();
		String reason = (message != null && !message.isBlank()) ? message : "Authentication failed";

		return Response.status(Response.Status.UNAUTHORIZED)
				.type(MediaType.APPLICATION_JSON)
				.entity(new JsonObject()
						.put("error", "Unauthorized")
						.put("message", reason))
				.build();
	}
}
