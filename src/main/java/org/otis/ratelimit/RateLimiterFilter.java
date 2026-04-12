package org.otis.ratelimit;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import static org.otis.shared.constant.CommonConstant.ERROR_KEY;
import static org.otis.shared.constant.CommonConstant.GROUP_ADMIN;
import static org.otis.shared.constant.CommonConstant.GROUP_GUEST;
import static org.otis.shared.constant.CommonConstant.GROUP_USER;
import static org.otis.shared.constant.CommonConstant.MESSAGE_KEY;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class RateLimiterFilter {
	private final RateLimitService rateLimitService;

	public RateLimiterFilter(RateLimitService rateLimitService) {
		this.rateLimitService = rateLimitService;
	}

	@ServerRequestFilter
	public Uni<Response> filter(RoutingContext routingContext) {
		JsonWebToken jwt = resolveJwt();

		String path = routingContext.normalizedPath();

		if (path.equals("/auth/register") || path.equals("/auth/login") || path.equals("/auth/groups")) {
			return Uni.createFrom().nullItem();
		}

		if (jwt == null) {
			return Uni.createFrom().nullItem();
		}

		String userId = jwt.getSubject();
		String group = resolveGroup(jwt);

		Log.infof("Rate limit check: user=%s, group=%s, path=%s", userId, group, path);

		return rateLimitService.isAllowed(userId, group)
				.onItem().invoke(allowed -> Log.infof("Rate limit result: user=%s, allowed=%s", userId, allowed))
				.map(allowed -> {
					if (Boolean.FALSE.equals(allowed)) {
						int limit = rateLimitService.getLimitForGroup(group);
						Log.warnf("Rate limit exceeded for user %s (group: %s, limit: %d/min)", userId, group, limit);

						return Response.status(Response.Status.TOO_MANY_REQUESTS)
								.entity(new JsonObject()
										.put(ERROR_KEY, "Rate limit exceeded")
										.put("group", group)
										.put("limit", limit)
										.put("window", "1 minute")
										.put(MESSAGE_KEY, "Too many requests. Please try again later."))
								.build();
					}
					return null;
				})
				.onFailure()
				.invoke(e -> Log.errorf(e, "Rate limiting failed for user %s (group: %s), allowing request", userId,
						group))
				.onFailure().recoverWithNull();
	}

	private JsonWebToken resolveJwt() {
		try {
			var instance = CDI.current().select(JsonWebToken.class);
			return instance.isResolvable() ? instance.get() : null;
		} catch (Exception e) {
			return null;
		}
	}

	private String resolveGroup(JsonWebToken jwt) {
		try {
			var groups = jwt.getClaim("groups");
			return switch (groups) {
				case java.util.Set<?> set -> matchFromSet(set);
				case java.util.List<?> list -> matchFromList(list);
				case JsonArray arr -> matchFromArray(arr);
				default -> null;
			};
		} catch (Exception e) {
			// ignore
		}

		return "default";
	}

	private String matchFromSet(java.util.Set<?> set) {
		for (Object g : set) {
			String name = g.toString().toLowerCase();
			if (GROUP_ADMIN.equals(name)) {
				return GROUP_ADMIN;
			}

			if (GROUP_USER.equals(name)) {
				return GROUP_USER;
			}

			if (GROUP_GUEST.equals(name)) {
				return GROUP_GUEST;
			}
		}

		return null;
	}

	private String matchFromList(java.util.List<?> list) {
		for (Object g : list) {
			String name = g.toString().toLowerCase();
			if (GROUP_ADMIN.equals(name)) {
				return GROUP_ADMIN;
			}

			if (GROUP_USER.equals(name)) {
				return GROUP_USER;
			}

			if (GROUP_GUEST.equals(name)) {
				return GROUP_GUEST;
			}
		}

		return null;
	}

	private String matchFromArray(JsonArray arr) {
		for (Object g : arr) {
			String name = g.toString().toLowerCase();
			if (GROUP_ADMIN.equals(name)) {
				return GROUP_ADMIN;
			}

			if (GROUP_USER.equals(name)) {
				return GROUP_USER;
			}

			if (GROUP_GUEST.equals(name)) {
				return GROUP_GUEST;
			}
		}

		return null;
	}
}
