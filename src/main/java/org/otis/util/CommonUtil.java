package org.otis.util;

public class CommonUtil {
    public static String extractErrorMessage(Exception e) {
        String errMsg;

        if (null != e.getCause()) {
            errMsg = e.getCause().getMessage();
        } else {
            errMsg = e.getMessage();
        }

        return errMsg;
    }
}
