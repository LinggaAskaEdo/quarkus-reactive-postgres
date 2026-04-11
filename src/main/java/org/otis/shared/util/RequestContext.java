package org.otis.shared.util;

import org.jboss.logging.MDC;
import static org.otis.shared.constant.CommonConstant.REQ_ID_KEY;

import com.github.f4b6a3.uuid.UuidCreator;

/**
 * MDC-based request context for storing the current request ID.
 * Automatically available in JSON logs when MDC is enabled.
 */
public final class RequestContext {

	private RequestContext() {
		// Utility class
	}

	public static void setReqId(String reqId) {
		MDC.put(REQ_ID_KEY, reqId);
	}

	public static String getReqId() {
		Object val = MDC.get(REQ_ID_KEY);
		return val != null ? val.toString() : "system";
	}

	public static void clear() {
		MDC.remove(REQ_ID_KEY);
	}

	public static String generateReqId() {
		return UuidCreator.getTimeOrderedEpoch().toString().substring(0, 8);
	}
}
