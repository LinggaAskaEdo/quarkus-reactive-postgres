package org.otis.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRegisterRequest {
	@NotBlank
	@Size(min = 3, max = 100)
	private String username;

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 8)
	private String password;

	@NotBlank
	@Size(min = 3, max = 100)
	private String firstName;

	@NotBlank
	@Size(min = 3, max = 100)
	private String lastName;

	@NotBlank
	private String groupName;
}
