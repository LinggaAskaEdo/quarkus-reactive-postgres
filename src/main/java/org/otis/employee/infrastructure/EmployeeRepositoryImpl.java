package org.otis.employee.infrastructure;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.otis.employee.domain.Employee;
import org.otis.employee.domain.EmployeeRepository;
import org.otis.shared.constant.FilterParamNames;
import org.otis.shared.constant.PagingConstants;
import org.otis.shared.dto.DtoEmployees;
import org.otis.shared.dto.DtoPagingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.elsql.ElSql;
import com.opengamma.elsql.ElSqlConfig;

import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmployeeRepositoryImpl implements EmployeeRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRepositoryImpl.class);

	private final Pool client;
	private final ElSql elSql;

	public EmployeeRepositoryImpl(Pool client) {
		this.client = client;
		URL resource = Thread.currentThread().getContextClassLoader().getResource("sql/employees.elsql");
		if (resource == null) {
			throw new IllegalArgumentException("ELSql resource not found: sql/employees.elsql");
		}
		this.elSql = ElSql.parse(ElSqlConfig.POSTGRES, resource);
	}

	@Override
	public Uni<DtoEmployees> getEmployees(DtoPagingRequest pagingRequest) {
		String orderColumn = getSafeOrderColumn(pagingRequest.getOrder());
		String sortDirection = getSafeSortDirection(pagingRequest.getSort());
		int limit = pagingRequest.getLimit() > 0 ? pagingRequest.getLimit() : PagingConstants.DEFAULT_LIMIT;
		int offset = pagingRequest.getOffset() >= 0 ? pagingRequest.getOffset() : PagingConstants.DEFAULT_OFFSET;

		Map<String, Object> params = new HashMap<>();
		params.put("orderColumn", orderColumn);
		params.put("sortDirection", sortDirection);
		params.put("limit", limit);
		params.put("offset", offset);

		if (!StringUtil.isNullOrEmpty(pagingRequest.getFirstName())) {
			params.put(FilterParamNames.EMPLOYEE_FIRST_NAME, "'%" + pagingRequest.getFirstName().toLowerCase() + "%'");
		}

		if (!StringUtil.isNullOrEmpty(pagingRequest.getLastName())) {
			params.put(FilterParamNames.EMPLOYEE_LAST_NAME, "'%" + pagingRequest.getLastName().toLowerCase() + "%'");
		}

		if (!StringUtil.isNullOrEmpty(pagingRequest.getEmail())) {
			params.put(FilterParamNames.EMPLOYEE_EMAIL, "'%" + pagingRequest.getEmail().toLowerCase() + "%'");
		}

		String sql = elSql.getSql("FindAllPaged", params);
		String sqlCount = elSql.getSql("CountAll", params);

		LOGGER.info(sql);
		LOGGER.info(sqlCount);

		Uni<List<Employee>> listUni = client.query(sql).execute()
				.map(rows -> rows.stream().map(row -> new Employee(row.getUUID(0),
						row.getString(1), row.getString(2), row.getString(3), row.getString(4), row.getString(5)))
						.toList());

		Uni<Long> countUni = client.query(sqlCount).execute()
				.map(rows -> rows.iterator().next().getLong(0));

		return Uni.combine().all().unis(listUni, countUni).asTuple().map(tuple2 -> {
			DtoEmployees dtoEmployees = new DtoEmployees();
			dtoEmployees.setEmployees(tuple2.getItem1());
			dtoEmployees.setCount(tuple2.getItem2().intValue());

			return dtoEmployees;
		});
	}

	@Override
	public Uni<Integer> createBulk(List<Employee> employees) {
		if (employees == null || employees.isEmpty()) {
			return Uni.createFrom().item(0);
		}

		StringBuilder values = new StringBuilder();
		Tuple tuple = Tuple.tuple();

		for (int i = 0; i < employees.size(); i++) {
			Employee emp = employees.get(i);
			String p1 = "$" + (i * 6 + 1);
			String p2 = "$" + (i * 6 + 2);
			String p3 = "$" + (i * 6 + 3);
			String p4 = "$" + (i * 6 + 4);
			String p5 = "$" + (i * 6 + 5);
			String p6 = "$" + (i * 6 + 6);

			if (i > 0) {
				values.append(", ");
			}

			values.append("(").append(p1).append(", ").append(p2).append(", ")
					.append(p3).append(", ").append(p4).append(", ")
					.append(p5).append(", ").append(p6).append(")");

			tuple.addUUID(emp.getId());
			tuple.addString(emp.getFirstName());
			tuple.addString(emp.getLastName());
			tuple.addString(emp.getEmail());
			tuple.addString(emp.getPhone());
			tuple.addString(emp.getJobTitle());
		}

		Map<String, Object> params = new HashMap<>();
		params.put("values", values.toString());

		String sql = elSql.getSql("CreateBulkBase", params);

		LOGGER.info(sql);

		return client.preparedQuery(sql).execute(tuple).onItem()
				.ifNotNull()
				.transform(SqlResult::rowCount);
	}

	/**
	 * Validates sort direction against allowlist to prevent SQL injection.
	 */
	private String getSafeSortDirection(String sort) {
		if (StringUtil.isNullOrEmpty(sort)) {
			return "ASC";
		}

		String upperSort = sort.toUpperCase();
		if (PagingConstants.ALLOWED_SORT_DIRECTIONS.contains(upperSort)) {
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
