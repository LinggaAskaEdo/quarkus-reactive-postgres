package org.otis.shared.constant;

public class CommonConstant {
	private CommonConstant() {
		// Utility class - prevent instantiation
	}

	public static final String ONE = "1";
	public static final String ZERO = "0";
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILED = "FAILED";

	// HTTP headers
	public static final String CONTENT_TYPE_HEADER = "Content-Type";
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

	// JSON keys
	public static final String ERROR_KEY = "error";
	public static final String DETAIL_KEY = "detail";
	public static final String MESSAGE_KEY = "message";
	public static final String REQ_ID_KEY = "req_id";

	// User groups
	public static final String GROUP_ADMIN = "admin";
	public static final String GROUP_USER = "user";
	public static final String GROUP_GUEST = "guest";
}
