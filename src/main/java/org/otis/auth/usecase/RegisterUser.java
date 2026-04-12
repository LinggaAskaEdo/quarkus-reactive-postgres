package org.otis.auth.usecase;

import java.util.List;
import java.util.Optional;

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

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class RegisterUser {
	private static final String REALMS_PREFIX = "/realms/";
	private static final String ADMIN_REALMS_PREFIX = "/admin/realms/";
	private static final String BEARER_PREFIX = "Bearer ";

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
				.chain(token -> createUser(request, token)
						.chain(response -> {
							if (response.getStatus() == 201) {
								String groupName = request.getGroupName();
								if (groupName == null || groupName.isBlank()) {
									return Uni.createFrom().item(response);
								}

								return assignUserToGroup(request.getUsername(), groupName, token)
										.map(assignResponse -> {
											if (assignResponse.getStatus() == 204) {
												Log.infof("User %s assigned to group %s", request.getUsername(),
														groupName);
											} else {
												Log.warnf("Failed to assign user %s to group %s", request.getUsername(),
														groupName);
											}

											return response;
										});
							}

							return Uni.createFrom().item(response);
						}));
	}

	private Uni<String> getAdminToken() {
		String tokenUrl = authServerUrl.replace(REALMS_PREFIX + realm, "")
				+ REALMS_PREFIX + "master/protocol/openid-connect/token";
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
		String createUserUrl = authServerUrl.replace(REALMS_PREFIX + realm, "")
				+ ADMIN_REALMS_PREFIX + realm + "/users";

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
				.putHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + adminToken)
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

	private Uni<Response> assignUserToGroup(String username, String groupName, String adminToken) {
		return getUserId(username, adminToken)
				.chain(userId -> getGroupByName(groupName, adminToken)
						.chain(groupId -> assignUserToGroupById(userId, groupId, adminToken)));
	}

	private Uni<String> getUserId(String username, String adminToken) {
		String getUsersUrl = authServerUrl.replace(REALMS_PREFIX + realm, "")
				+ ADMIN_REALMS_PREFIX + realm + "/users?username=" + username;

		return webClient.getAbs(getUsersUrl)
				.putHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + adminToken)
				.send()
				.chain(response -> {
					if (response.statusCode() != 200) {
						return Uni.createFrom().failure(new KeycloakAuthException(
								"Failed to get user ID: " + response.bodyAsString()));
					}

					List<JsonObject> users = response.bodyAsJsonArray().stream()
							.map(obj -> new JsonObject(obj.toString()))
							.toList();

					if (users.isEmpty()) {
						return Uni.createFrom().failure(new KeycloakAuthException(
								"User not found: " + username));
					}

					return Uni.createFrom().item(users.get(0).getString("id"));
				});
	}

	private Uni<String> getGroupByName(String groupName, String adminToken) {
		String getGroupsUrl = authServerUrl.replace(REALMS_PREFIX + realm, "")
				+ ADMIN_REALMS_PREFIX + realm + "/groups";

		return webClient.getAbs(getGroupsUrl)
				.putHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + adminToken)
				.send()
				.chain(response -> {
					if (response.statusCode() != 200) {
						return Uni.createFrom().failure(new KeycloakAuthException(
								"Failed to get groups: " + response.bodyAsString()));
					}

					List<JsonObject> groups = response.bodyAsJsonArray().stream()
							.map(obj -> new JsonObject(obj.toString()))
							.toList();
					Optional<JsonObject> matchingGroup = groups.stream()
							.filter(g -> groupName.equals(g.getString("name")))
							.findFirst();

					if (matchingGroup.isEmpty()) {
						return Uni.createFrom().failure(new KeycloakAuthException(
								"Group not found: " + groupName));
					}

					return Uni.createFrom().item(matchingGroup.get().getString("id"));
				});
	}

	private Uni<Response> assignUserToGroupById(String userId, String groupId, String adminToken) {
		String assignUrl = authServerUrl.replace(REALMS_PREFIX + realm, "")
				+ ADMIN_REALMS_PREFIX + realm + "/users/" + userId + "/groups/" + groupId;

		return webClient.putAbs(assignUrl)
				.putHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
				.putHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + adminToken)
				.sendBuffer(Buffer.buffer(new JsonObject().put("realm", realm).encode()))
				.map(response -> {
					if (response.statusCode() == 204) {
						return Response.noContent().build();
					} else {
						return Response.status(Response.Status.BAD_REQUEST)
								.entity(new JsonObject()
										.put(ERROR_KEY, "Failed to assign user to group")
										.put(DETAIL_KEY, response.bodyAsString()))
								.build();
					}
				});
	}
}
