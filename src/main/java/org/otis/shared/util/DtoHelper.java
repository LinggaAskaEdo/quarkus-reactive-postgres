package org.otis.shared.util;

import java.util.List;

import org.otis.shared.constant.StatusMsgEnum;
import org.otis.shared.dto.DtoPagingResponse;
import org.otis.shared.dto.DtoResponse;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.Response;

public final class DtoHelper {
	private DtoHelper() {
		// Utility class - prevent instantiation
	}

	public static DtoResponse constructResponse(StatusMsgEnum statusEnum, String reason, Object data) {
		DtoResponse response = new DtoResponse();
		response.setStatus(statusEnum == StatusMsgEnum.SUCCESS ? "0" : "1");
		response.setMessage(statusEnum.name());
		response.setReason(reason);
		response.setData(data);

		return response;
	}

	public static DtoPagingResponse constructPagingResponse(StatusMsgEnum statusEnum, String reason,
			List<?> data, int recordsTotal, int recordsFiltered) {
		DtoPagingResponse response = new DtoPagingResponse();
		response.setStatus(statusEnum == StatusMsgEnum.SUCCESS ? "0" : "1");
		response.setMessage(statusEnum.name());
		response.setReason(reason);
		response.setData(data);
		response.setRecordsTotal(recordsTotal);
		response.setRecordsFiltered(recordsFiltered);

		return response;
	}

	/**
	 * Build a 500 Internal Server Error JAX-RS response.
	 */
	public static Response internalServerError(String error, String message) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(new JsonObject()
						.put("error", error)
						.put("message", message))
				.build();
	}
}
