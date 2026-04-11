package org.otis.auth.usecase;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import static org.otis.shared.constant.CommonConstant.CONTENT_TYPE_FORM;
import static org.otis.shared.constant.CommonConstant.CONTENT_TYPE_HEADER;
import static org.otis.shared.constant.CommonConstant.DETAIL_KEY;
import static org.otis.shared.constant.CommonConstant.ERROR_KEY;
import org.otis.shared.dto.AuthLoginRequest;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class LoginUser {
	private final WebClient webClient;
	private final String authServerUrl;
	private final String clientId;
	private final String clientSecret;

	public LoginUser(WebClient webClient,
			@ConfigProperty(name = "quarkus.oidc.auth-server-url") String authServerUrl,
			@ConfigProperty(name = "quarkus.oidc.client-id") String clientId,
			@ConfigProperty(name = "quarkus.oidc.credentials.secret") String clientSecret) {
		this.webClient = webClient;
		this.authServerUrl = authServerUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public Uni<Response> execute(AuthLoginRequest request) {
		String tokenUrl = authServerUrl + "/protocol/openid-connect/token";
		String body = "grant_type=password"
				+ "&client_id=" + clientId
				+ "&client_secret=" + clientSecret
				+ "&username=" + request.getUsername()
				+ "&password=" + request.getPassword();

		return webClient.postAbs(tokenUrl)
				.putHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_FORM)
				.sendBuffer(Buffer.buffer(body))
				.chain(response -> {
					if (response.statusCode() == 200) {
						Log.infof("User logged in: %s", request.getUsername());
						return Uni.createFrom().item(Response.ok(response.bodyAsString()).build());
					} else {
						Log.warnf("Login failed for %s: %d %s", request.getUsername(), response.statusCode(),
								response.bodyAsString());
						return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED)
								.entity(new JsonObject()
										.put(ERROR_KEY, "Invalid credentials")
										.put(DETAIL_KEY, response.bodyAsString()))
								.build());
					}
				})
				.onFailure().recoverWithItem(throwable -> {
					Log.errorf(throwable, "Error logging in user: %s", request.getUsername());
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.entity("Internal server error: " + throwable.getMessage())
							.build();
				});
	}
}
