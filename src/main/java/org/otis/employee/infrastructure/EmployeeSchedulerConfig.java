package org.otis.employee.infrastructure;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "employee.scheduler")
public interface EmployeeSchedulerConfig {
	/**
	 * Scheduler interval expression (e.g., "1m", "5m", "1h")
	 */
	@WithDefault("5m")
	String interval();

	/**
	 * Minimum number of employees to insert per run (minimum value is 3)
	 */
	@WithDefault("10")
	int minInsert();

	/**
	 * Enable or disable the scheduler
	 */
	@WithDefault("true")
	boolean enabled();

	/**
	 * URL of the RandomUser API endpoint
	 */
	@WithDefault("https://randomuser.me/api/")
	String apiUrl();
}
