package org.otis.fruit.infrastructure;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "fruit.scheduler")
public interface FruitSchedulerConfig {
	/**
	 * Scheduler interval expression (e.g., "1m", "5m", "1h")
	 */
	@WithDefault("1m")
	String interval();

	/**
	 * Minimum number of fruits to insert per run (minimum value is 3)
	 */
	@WithDefault("10")
	int minInsert();

	/**
	 * Enable or disable the scheduler
	 */
	@WithDefault("true")
	boolean enabled();

	/**
	 * URL of the fruit API endpoint
	 */
	@WithDefault("https://www.fruityvice.com/api/fruit/all")
	String apiUrl();
}
