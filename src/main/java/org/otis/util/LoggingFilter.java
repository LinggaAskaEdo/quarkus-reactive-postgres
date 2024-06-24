package org.otis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import org.jboss.logging.Logger;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.otis.model.dto.DtoRequest;
import org.otis.model.dto.DtoResponse;

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
//        final String body = requestContext.getRequest().toString();

        LOG.infof("event=START httpMethod=%s requestPath=%s remoteHost=%s requestParam=%s requestBody=%s", method, path, address, param, body);
    }

    private String convertRequestEntity(Object object) throws JsonProcessingException {
        DtoRequest dtoRequest = (DtoRequest) object;

        return new ObjectMapper().writer().writeValueAsString(dtoRequest);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws JsonProcessingException {
        final String method = requestContext.getMethod();
        final String path = info.getPath();
        final String body = requestContext.getRequest().toString();

        LOG.infof("event=END httpMethod=%s requestPath=%s requestBody=%s responseBody=%s", method, path, body, convertResponseEntity(responseContext.getEntity()));
    }

    private String convertResponseEntity(Object object) throws JsonProcessingException {
        DtoResponse dtoResponse = (DtoResponse) object;

        return new ObjectMapper().writer().writeValueAsString(dtoResponse);
    }
}
