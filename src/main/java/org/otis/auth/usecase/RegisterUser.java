package org.otis.auth.usecase;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import static org.otis.shared.constant.CommonConstant.AUTHORIZATION_HEADER;
import static org.otis.shared.constant.CommonConstant.CONTENT_TYPE_FORM;
import static org.otis.shared.constant.CommonConstant.CONTENT_TYPE_HEADER;
import static org.otis.shared.constant.CommonConstant.CONTENT_TYPE_JSON;
import static org.otis.shared.constant.CommonConstant.DETAIL_KEY;
import static org.otis.shared.constant.CommonConstant.ERROR_KEY;
import static org.otis.shared.constant.CommonConstant.MESSAGE_KEY;
import org.otis.shared.dto.AuthRegisterRequest;
import org.otis.shared.exception.KeycloakAuthException;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class RegisterUser {
	private final WebClient webClient;
	private final String authServerUrl;
	private final String adminUsername;
	private final String adminPassword;
	private final String realm;

	public RegisterUser(WebClient webClient,
			@ConfigProperty(name = "quarkus.oidc.auth-server-url") String authServerUrl,
			@ConfigProperty(name = "keycloak.admin.username") String adminUsername,
			@ConfigProperty(name = "keycloak.admin.password") String adminPassword) {
		this.webClient = webClient;
		this.authServerUrl = authServerUrl;
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
		this.realm = extractRealm(authServerUrl);
	}

	public Uni<Response> execute(AuthRegisterRequest request) {
		return getAdminToken()
				.chain(token -> createUser(request, token));
	}

	private Uni<String> getAdminToken() {
		String tokenUrl = authServerUrl.replace("/realms/" + realm, "")
				+ "/realms/master/protocol/openid-connect/token";
		String body = "grant_type=password"
				+ "&client_id=admin-cli"
				+ "&username=" + adminUsername
				+ "&password=" + adminPassword;

		return webClient.postAbs(tokenUrl)
				.putHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_FORM)
				.sendBuffer(Buffer.buffer(body))
				.chain(response -> {
					if (response.statusCode() != 200) {
						return Uni.createFrom().failure(new KeycloakAuthException(
								"Failed to get admin token: " + response.bodyAsString()));
					}

					String token = new JsonObject(response.bodyAsString()).getString("access_token");
					return Uni.createFrom().item(token);
				});
	}

	private Uni<Response> createUser(AuthRegisterRequest request, String adminToken) {
		String createUserUrl = authServerUrl.replace("/realms/" + realm, "")
				+ "/admin/realms/" + realm + "/users";

		JsonObject userJson = new JsonObject()
				.put("username", request.getUsername())
				.put("email", request.getEmail())
				.put("enabled", true)
				.put("emailVerified", true)
				.put("requiredActions", new JsonArray())
				.put("firstName", request.getFirstName())
				.put("lastName", request.getLastName())
				.put("credentials", new JsonArray()
						.add(new JsonObject()
								.put("type", "password")
								.put("value", request.getPassword())
								.put("temporary", false)));

		return webClient.postAbs(createUserUrl)
				.putHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
				.putHeader(AUTHORIZATION_HEADER, "Bearer " + adminToken)
				.sendBuffer(Buffer.buffer(userJson.encode()))
				.map(response -> {
					if (response.statusCode() == 201) {
						return Response.status(Response.Status.CREATED)
								.entity(new JsonObject()
										.put(MESSAGE_KEY, "User registered successfully")
										.put("username", request.getUsername()))
								.build();
					} else {
						return Response.status(Response.Status.BAD_REQUEST)
								.entity(new JsonObject()
										.put(ERROR_KEY, "Failed to register user")
										.put(DETAIL_KEY, response.bodyAsString()))
								.build();
					}
				});
	}

	private String extractRealm(String authServerUrl) {
		String[] parts = authServerUrl.split("/");
		return parts[parts.length - 1];
	}
}
