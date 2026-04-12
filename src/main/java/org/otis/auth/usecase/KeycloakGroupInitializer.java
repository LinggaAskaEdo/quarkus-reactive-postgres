package org.otis.auth.usecase;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.otis.shared.constant.CommonConstant;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeycloakGroupInitializer {
	private final WebClient webClient;
	private final String authServerUrl;
	private final String adminUsername;
	private final String adminPassword;
	private final String realm;
	private final List<String> defaultGroups;

	public KeycloakGroupInitializer(WebClient webClient,
			@ConfigProperty(name = "quarkus.oidc.auth-server-url") String authServerUrl,
			@ConfigProperty(name = "keycloak.admin.username") String adminUsername,
			@ConfigProperty(name = "keycloak.admin.password") String adminPassword,
			@ConfigProperty(name = "keycloak.default.groups", defaultValue = "admin,user,guest") String defaultGroups) {
		this.webClient = webClient;
		this.authServerUrl = authServerUrl;
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
		this.realm = extractRealm(authServerUrl);
		this.defaultGroups = List.of(defaultGroups.split(","));
	}

	@SuppressWarnings("unused")
	@PostConstruct
	void init() {
		initializeGroups().await().indefinitely();
	}

	private Uni<Void> initializeGroups() {
		return getAdminToken()
				.chain(this::createGroupsIfNotExist)
				.replaceWithVoid();
	}

	private Uni<String> getAdminToken() {
		String tokenUrl = authServerUrl.replace("/realms/" + realm, "")
				+ "/realms/master/protocol/openid-connect/token";
		String body = "grant_type=password"
				+ "&client_id=admin-cli"
				+ "&username=" + adminUsername
				+ "&password=" + adminPassword;

		return webClient.postAbs(tokenUrl)
				.putHeader(CommonConstant.CONTENT_TYPE_HEADER, CommonConstant.CONTENT_TYPE_FORM)
				.sendBuffer(Buffer.buffer(body))
				.chain(response -> {
					if (response.statusCode() != 200) {
						return Uni.createFrom().failure(new RuntimeException(
								"Failed to get admin token for group initialization: " + response.bodyAsString()));
					}
					String token = new JsonObject(response.bodyAsString()).getString("access_token");
					return Uni.createFrom().item(token);
				});
	}

	private Uni<Void> createGroupsIfNotExist(String adminToken) {
		return createGroupsSequentially(defaultGroups, adminToken, 0);
	}

	private Uni<Void> createGroupsSequentially(List<String> groups, String adminToken, int index) {
		if (index >= groups.size()) {
			return Uni.createFrom().voidItem();
		}

		return getOrCreateGroup(groups.get(index), adminToken)
				.chain(() -> createGroupsSequentially(groups, adminToken, index + 1))
				.replaceWithVoid();
	}

	private Uni<String> getOrCreateGroup(String groupName, String adminToken) {
		String baseUrl = authServerUrl.replace("/realms/" + realm, "")
				+ "/admin/realms/" + realm;

		String getGroupsUrl = baseUrl + "/groups";

		return webClient.getAbs(getGroupsUrl)
				.putHeader(CommonConstant.AUTHORIZATION_HEADER, "Bearer " + adminToken)
				.send()
				.chain(response -> {
					if (response.statusCode() != 200) {
						return Uni.createFrom().failure(new RuntimeException(
								"Failed to fetch groups: " + response.bodyAsString()));
					}

					List<JsonObject> existingGroups = response.bodyAsJsonArray().stream()
							.map(obj -> new JsonObject(obj.toString()))
							.toList();

					boolean groupExists = existingGroups.stream()
							.anyMatch(g -> groupName.equals(g.getString("name")));

					if (groupExists) {
						return Uni.createFrom().item(groupName);
					}

					return createGroup(groupName, adminToken, baseUrl);
				});
	}

	private Uni<String> createGroup(String groupName, String adminToken, String baseUrl) {
		String createGroupUrl = baseUrl + "/groups";

		JsonObject groupJson = new JsonObject().put("name", groupName);

		return webClient.postAbs(createGroupUrl)
				.putHeader(CommonConstant.CONTENT_TYPE_HEADER, CommonConstant.CONTENT_TYPE_JSON)
				.putHeader(CommonConstant.AUTHORIZATION_HEADER, "Bearer " + adminToken)
				.sendBuffer(Buffer.buffer(groupJson.encode()))
				.chain(response -> {
					if (response.statusCode() != 201) {
						return Uni.createFrom().failure(new RuntimeException(
								"Failed to create group '" + groupName + "': " + response.bodyAsString()));
					}
					return Uni.createFrom().item(groupName);
				});
	}

	private String extractRealm(String authServerUrl) {
		String[] parts = authServerUrl.split("/");
		return parts[parts.length - 1];
	}

	public List<String> getDefaultGroups() {
		return defaultGroups;
	}
}
