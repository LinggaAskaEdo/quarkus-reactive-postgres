package org.otis.util;

import org.otis.constant.CommonConstant;
import org.otis.constant.StatusMsgEnum;
import org.otis.model.dto.DtoPagingResponse;
import org.otis.model.dto.DtoResponse;

public class DtoHelper {
    private DtoHelper() {
        // Utility class - prevent instantiation
    }

    public static DtoResponse constructResponse(StatusMsgEnum sme, String msg, String reason, Object data) {
        DtoResponse dtoRespond;

        if (sme == null) {
            dtoRespond = createFailedResponse("Invalid status", "Status message enum is null", null);
        } else {
            dtoRespond = switch (sme) {
            case FAILED -> createFailedResponse(msg, reason, data);
            case SUCCESS -> createSuccessResponse(msg, reason, data);
            default -> createFailedResponse("Unknown status: " + sme, "Unsupported status", data);
            };
        }

        return dtoRespond;
    }

    private static DtoResponse createFailedResponse(String msg, String reason, Object data) {
        DtoResponse resp = new DtoResponse();
        resp.setStatus(CommonConstant.ZERO);
        resp.setMessage(msg);
        resp.setReason(reason);
        resp.setData(data);

        return resp;
    }

    private static DtoResponse createSuccessResponse(String msg, String reason, Object data) {
        DtoResponse resp = new DtoResponse();
        resp.setStatus(CommonConstant.ONE);
        resp.setMessage(msg);
        resp.setReason(reason);
        resp.setData(data);

        return resp;
    }

    public static DtoPagingResponse constructPagingResponse(StatusMsgEnum sme, String msg, String reason, Object data,
            int totalFiltered, int totalData) {
        DtoPagingResponse dtoPagingResponse;

        if (sme == null) {
            dtoPagingResponse = createFailedPagingResponse("Invalid status", "Status message enum is null", data,
                    totalFiltered, totalData);
        } else {
            dtoPagingResponse = switch (sme) {
            case FAILED -> createFailedPagingResponse(msg, reason, data, totalFiltered, totalData);
            case SUCCESS -> createSuccessPagingResponse(msg, reason, data, totalFiltered, totalData);
            default -> createFailedPagingResponse("Unknown status: " + sme, "Unsupported status", data, totalFiltered,
                    totalData);
            };
        }

        return dtoPagingResponse;
    }

    private static DtoPagingResponse createFailedPagingResponse(String msg, String reason, Object data,
            int totalFiltered, int totalData) {
        DtoPagingResponse resp = new DtoPagingResponse();
        resp.setStatus(CommonConstant.ZERO);
        resp.setMessage(msg);
        resp.setReason(reason);
        resp.setData(data);
        resp.setRecordsFiltered(totalFiltered);
        resp.setRecordsTotal(totalData);

        return resp;
    }

    private static DtoPagingResponse createSuccessPagingResponse(String msg, String reason, Object data,
            int totalFiltered, int totalData) {
        DtoPagingResponse resp = new DtoPagingResponse();
        resp.setStatus(CommonConstant.ONE);
        resp.setMessage(msg);
        resp.setReason(reason);
        resp.setData(data);
        resp.setRecordsFiltered(totalFiltered);
        resp.setRecordsTotal(totalData);

        return resp;
    }
}
