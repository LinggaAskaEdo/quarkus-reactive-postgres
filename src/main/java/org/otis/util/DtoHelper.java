package org.otis.util;

import org.otis.constant.CommonConstant;
import org.otis.constant.StatusMsgEnum;
import org.otis.model.dto.DtoPagingResponse;
import org.otis.model.dto.DtoResponse;

import java.util.Objects;

public class DtoHelper {
    public static DtoResponse constructResponse(StatusMsgEnum sme, String msg, String reason, Object data) {
        DtoResponse dtoRespond = null;

        if (null != sme) {
            switch (sme) {
                case FAILED:
                    dtoRespond = new DtoResponse();
                    dtoRespond.setStatus(CommonConstant._0);
                    dtoRespond.setMessage(msg);
                    dtoRespond.setReason(reason);
                    break;
                case SUCCESS:
                    dtoRespond = new DtoResponse();
                    dtoRespond.setStatus(CommonConstant._1);
                    dtoRespond.setMessage(msg);
                    dtoRespond.setReason(reason);
                    break;
                default:
                    return null;
            }
        }

        Objects.requireNonNull(dtoRespond).setData(data);

        return dtoRespond;
    }

    public static DtoPagingResponse constructPagingResponse(StatusMsgEnum sme, String msg, String reason, Object data, int totalFiltered, int totalData) {
        DtoPagingResponse dtoPagingResponse = null;

        if (null != sme) {
            switch (sme) {
                case FAILED:
                    dtoPagingResponse = new DtoPagingResponse();
                    dtoPagingResponse.setStatus(CommonConstant._0);
                    dtoPagingResponse.setMessage(msg);
                    dtoPagingResponse.setReason(reason);
                    break;
                case SUCCESS:
                    dtoPagingResponse = new DtoPagingResponse();
                    dtoPagingResponse.setStatus(CommonConstant._1);
                    dtoPagingResponse.setMessage(msg);
                    dtoPagingResponse.setReason(reason);
                    break;
                default:
                    return null;
            }
        }

        Objects.requireNonNull(dtoPagingResponse).setData(data);
        dtoPagingResponse.setRecordsFiltered(totalFiltered);
        dtoPagingResponse.setRecordsTotal(totalData);

        return dtoPagingResponse;
    }
}
