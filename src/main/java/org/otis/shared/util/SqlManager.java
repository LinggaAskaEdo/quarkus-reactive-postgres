package org.otis.shared.util;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.elsql.ElSql;
import com.opengamma.elsql.ElSqlConfig;

/**
 * Loads and caches ELSql files. Provides thread-safe access to named SQL
 * queries.
 */
public final class SqlManager {
	private final Map<String, ElSql> sqlFiles = new ConcurrentHashMap<>();

	public SqlManager(String... resourcePaths) {
		for (String path : resourcePaths) {
			URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
			if (resource == null) {
				throw new IllegalArgumentException("ELSql resource not found: " + path);
			}

			ElSql elSql = ElSql.parse(ElSqlConfig.POSTGRES, resource);
			sqlFiles.put(path, elSql);
		}
	}

	/**
	 * Get SQL by name from any loaded file.
	 */
	public String getSql(String name) {
		for (ElSql elSql : sqlFiles.values()) {
			try {
				return elSql.getSql(name);
			} catch (IllegalArgumentException e) {
				// Not found in this file, try next
			}
		}

		throw new IllegalArgumentException("SQL not found: " + name);
	}

	/**
	 * Get SQL by name from a specific file, with parameters.
	 */
	public String getSql(String fileName, String name, Map<String, Object> params) {
		ElSql elSql = sqlFiles.get(fileName);
		if (elSql == null) {
			throw new IllegalArgumentException("SQL file not loaded: " + fileName);
		}

		return elSql.getSql(name, params);
	}

	/**
	 * Get SQL by name from any loaded file, with parameters.
	 */
	public String getSql(String name, Map<String, Object> params) {
		for (ElSql elSql : sqlFiles.values()) {
			try {
				return elSql.getSql(name, params);
			} catch (IllegalArgumentException e) {
				// Not found in this file, try next
			}
		}

		throw new IllegalArgumentException("SQL not found: " + name);
	}
}
