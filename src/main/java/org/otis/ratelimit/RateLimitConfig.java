package org.otis.ratelimit;

import java.util.Map;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "ratelimit")
public interface RateLimitConfig {
	boolean enabled();

	int windowSeconds();

	Map<String, Integer> groupLimits();

	default int getDefaultLimit() {
		return groupLimits().getOrDefault("default", 1);
	}
}
