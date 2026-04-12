package org.otis.ratelimit;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.sortedset.ReactiveSortedSetCommands;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RateLimitService {
	private final RateLimitConfig config;
	private final ReactiveSortedSetCommands<String, Double> sortedSet;

	public RateLimitService(RateLimitConfig config, ReactiveRedisDataSource redis) {
		this.config = config;
		this.sortedSet = redis.sortedSet(Double.class);
	}

	/**
	 * Check if the request is within the rate limit.
	 * Returns true if the request is allowed, false if rate limited.
	 */
	public Uni<Boolean> isAllowed(String userId, String group) {
		int limit = config.groupLimits().getOrDefault(group, config.getDefaultLimit());
		String key = "ratelimit:" + userId;
		double now = System.currentTimeMillis();
		double windowStart = now - (config.windowSeconds() * 1000L);

		// ZREMRANGEBYSCORE key -inf windowStart — remove expired entries
		return sortedSet.zremrangebyscore(key, ScoreRange.from(Double.NEGATIVE_INFINITY, windowStart))
				// ZADD key now now — add current request with timestamp as score
				.chain(() -> sortedSet.zadd(key, now, now))
				// ZCOUNT key windowStart +inf — count requests in current window
				.chain(() -> sortedSet.zcount(key, ScoreRange.from(windowStart, Double.POSITIVE_INFINITY)))
				.map(count -> count <= limit);
	}

	public int getLimitForGroup(String group) {
		return config.groupLimits().getOrDefault(group, config.getDefaultLimit());
	}
}
