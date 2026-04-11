package org.otis.shared.util;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@ApplicationScoped
public class WebClients {
	@ConfigProperty(name = "webclient.max-pool-size", defaultValue = "20")
	int maxPoolSize;

	@ConfigProperty(name = "webclient.connect-timeout", defaultValue = "5000")
	int connectTimeout;

	@ConfigProperty(name = "webclient.idle-timeout", defaultValue = "60000")
	int idleTimeout;

	@ConfigProperty(name = "webclient.max-wait-queue-size", defaultValue = "5000")
	int maxWaitQueueSize;

	@Produces
	@Singleton
	public WebClient webClient(Vertx vertx) {
		var options = new WebClientOptions()
				.setMaxPoolSize(maxPoolSize)
				.setConnectTimeout(connectTimeout)
				.setIdleTimeout(idleTimeout)
				.setMaxWaitQueueSize(maxWaitQueueSize)
				.setKeepAlive(true)
				.setReuseAddress(true)
				.setReusePort(true)
				.setTcpKeepAlive(true);

		return WebClient.create(vertx, options);
	}
}
