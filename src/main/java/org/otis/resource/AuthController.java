package org.otis.resource;

import java.util.List;

import org.otis.auth.usecase.KeycloakGroupInitializer;
import org.otis.auth.usecase.LoginUser;
import org.otis.auth.usecase.RegisterUser;
import org.otis.shared.dto.AuthLoginRequest;
import org.otis.shared.dto.AuthRegisterRequest;
import org.otis.shared.util.DtoHelper;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("auth")
@PermitAll
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {
	private final RegisterUser registerUser;
	private final LoginUser loginUser;
	private final KeycloakGroupInitializer groupInitializer;

	public AuthController(RegisterUser registerUser, LoginUser loginUser, KeycloakGroupInitializer groupInitializer) {
		this.registerUser = registerUser;
		this.loginUser = loginUser;
		this.groupInitializer = groupInitializer;
	}

	@POST
	@Path("register")
	public Uni<Response> register(@Valid AuthRegisterRequest request) {
		return registerUser.execute(request)
				.onFailure().recoverWithItem(throwable -> {
					Log.errorf(throwable, "Error registering user: %s", request.getUsername());
					return DtoHelper.internalServerError("Internal server error", throwable.getMessage());
				});
	}

	@POST
	@Path("login")
	public Uni<Response> login(@Valid AuthLoginRequest request) {
		return loginUser.execute(request);
	}

	@GET
	@Path("groups")
	public Response getAvailableGroups() {
		List<String> groups = groupInitializer.getDefaultGroups();
		JsonArray groupsArray = new JsonArray(groups);

		return Response.ok(new JsonObject().put("groups", groupsArray)).build();
	}
}
