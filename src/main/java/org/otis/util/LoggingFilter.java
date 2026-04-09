package org.otis.util;

import org.jboss.logging.Logger;
import org.otis.model.dto.DtoResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOG = Logger.getLogger(LoggingFilter.class);

    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws JsonProcessingException {
        final String method = requestContext.getMethod();
        final String path = info.getPath();
        final String address = request.remoteAddress().toString();
        final String param = request.params().toString();
        final String body = request.body().toString();

        LOG.infof("event=START httpMethod=%s requestPath=%s remoteHost=%s requestParam=%s requestBody=%s", method, path,
                address, param, body);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws JsonProcessingException {
        final String method = requestContext.getMethod();
        final String path = info.getPath();
        final String body = requestContext.getRequest().toString();

        LOG.infof("event=END httpMethod=%s requestPath=%s requestBody=%s responseBody=%s", method, path, body,
                convertResponseEntity(responseContext.getEntity()));
    }

    private String convertResponseEntity(Object object) throws JsonProcessingException {
        if (!(object instanceof DtoResponse dtoResponse)) {
            return object != null ? object.toString() : "null";
        }

        return new ObjectMapper().writer().writeValueAsString(dtoResponse);
    }
}
