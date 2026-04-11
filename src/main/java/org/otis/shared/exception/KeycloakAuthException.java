package org.otis.shared.exception;

/**
 * Thrown when Keycloak authentication operations fail.
 */
public class KeycloakAuthException extends RuntimeException {
	public KeycloakAuthException(String message) {
		super(message);
	}
}
