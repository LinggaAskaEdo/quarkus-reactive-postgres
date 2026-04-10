package org.otis.shared.util;

import java.util.logging.Logger;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) {
        RequestContext.setReqId(RequestContext.generateReqId());
        requestContext.setProperty("startTime", System.currentTimeMillis());
        LOGGER.info(() -> String.format("event=START httpMethod=%s requestPath=%s remoteHost=%s requestParam=%s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().getPath(),
                requestContext.getUriInfo().getRequestUri().getHost() + ":" + requestContext.getUriInfo().getRequestUri().getPort(),
                requestContext.getUriInfo().getQueryParameters()));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Long startTime = (Long) requestContext.getProperty("startTime");
        long processTime = startTime != null ? System.currentTimeMillis() - startTime : -1;
        LOGGER.info(() -> String.format("event=END httpMethod=%s requestPath=%s statusCode=%s processTimeMs=%d",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().getPath(),
                responseContext.getStatus(),
                processTime));
        RequestContext.clear();
    }
}
