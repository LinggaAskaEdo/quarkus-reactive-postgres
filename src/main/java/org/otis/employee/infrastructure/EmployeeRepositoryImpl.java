package org.otis.employee.infrastructure;

import java.util.List;
import java.util.Set;

import org.otis.employee.domain.Employee;
import org.otis.employee.domain.EmployeeRepository;
import org.otis.shared.dto.DtoEmployees;
import org.otis.shared.dto.DtoPagingRequest;
import org.otis.shared.util.SqlManager;

import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmployeeRepositoryImpl implements EmployeeRepository {
	private static final Set<String> ALLOWED_SORT_DIRECTIONS = Set.of("ASC", "DESC");
	private static final int DEFAULT_LIMIT = 10;
	private static final int DEFAULT_OFFSET = 0;

	private final Pool client;
	private final SqlManager sqlManager;

	public EmployeeRepositoryImpl(Pool client) {
		this.client = client;
		this.sqlManager = new SqlManager("sql/employees.elsql");
	}

	@Override
	public Uni<List<Employee>> getEmployees(DtoPagingRequest pagingRequest) {
		String orderColumn = getSafeOrderColumn(pagingRequest.getOrder());
		String sortDirection = getSafeSortDirection(pagingRequest.getSort());
		int limit = pagingRequest.getLimit() > 0 ? pagingRequest.getLimit() : DEFAULT_LIMIT;
		int offset = pagingRequest.getOffset() >= 0 ? pagingRequest.getOffset() : DEFAULT_OFFSET;

		String sql = sqlManager.getSql("FindAllPaged").formatted(
				orderColumn, sortDirection, limit, offset);

		return client.query(sql).execute().map(rows -> rows.stream().map(row -> new Employee(row.getUUID(0),
				row.getString(1), row.getString(2), row.getString(3), row.getString(4), row.getString(5))).toList());
	}

	@Override
	public Uni<DtoEmployees> getAllEmployee(DtoPagingRequest pagingRequest) {
		String orderColumn = getSafeOrderColumn(pagingRequest.getOrder());
		String sortDirection = getSafeSortDirection(pagingRequest.getSort());
		int limit = pagingRequest.getLimit() > 0 ? pagingRequest.getLimit() : DEFAULT_LIMIT;
		int offset = pagingRequest.getOffset() >= 0 ? pagingRequest.getOffset() : DEFAULT_OFFSET;

		String sql = sqlManager.getSql("FindAllPaged").formatted(
				orderColumn, sortDirection, limit, offset);
		String sqlCount = sqlManager.getSql("CountAll");

		Uni<List<Employee>> listUni = client.query(sql).execute()
				.map(rows -> rows.stream().map(row -> new Employee(row.getUUID(0), row.getString(1), row.getString(2),
						row.getString(3), row.getString(4), row.getString(5))).toList());

		Uni<Long> countUni = client.query(sqlCount).execute().map(rows -> rows.iterator().next().getLong(0));

		return Uni.combine().all().unis(listUni, countUni).asTuple().map(tuple -> {
			DtoEmployees dtoEmployees = new DtoEmployees();
			dtoEmployees.setEmployees(tuple.getItem1());
			dtoEmployees.setCount(tuple.getItem2().intValue());

			return dtoEmployees;
		});
	}

	/**
	 * Validates sort direction against allowlist to prevent SQL injection.
	 */
	private String getSafeSortDirection(String sort) {
		if (StringUtil.isNullOrEmpty(sort)) {
			return "ASC";
		}

		String upperSort = sort.toUpperCase();
		if (ALLOWED_SORT_DIRECTIONS.contains(upperSort)) {
			return upperSort;
		}

		throw new IllegalArgumentException("Invalid sort direction: " + sort + ". Must be ASC or DESC.");
	}

	/**
	 * Validates order column against known columns to prevent SQL injection. Throws
	 * exception if column name is not
	 * recognized.
	 */
	private String getSafeOrderColumn(String order) {
		if (StringUtil.isNullOrEmpty(order)) {
			return "employee_id";
		}

		return switch (order) {
			case "id" -> "employee_id";
			case "firstName" -> "first_name";
			case "lastName" -> "last_name";
			case "email" -> "email";
			case "phone" -> "phone";
			case "hireDate" -> "hire_date";
			case "jobTitle" -> "job_title";
			default -> throw new IllegalArgumentException("Invalid order column: " + order);
		};
	}
}
