package org.otis.employee.infrastructure;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.otis.employee.domain.Employee;
import org.otis.employee.domain.EmployeeRepository;
import org.otis.shared.util.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmployeeSchedulerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeSchedulerService.class);

	private static final String[] JOB_TITLES = {
			"Software Engineer", "Product Manager", "Data Analyst", "DevOps Engineer",
			"QA Engineer", "UI/UX Designer", "Backend Developer", "Frontend Developer",
			"Full Stack Developer", "Scrum Master", "Technical Lead", "Architect"
	};

	private final Random random = new Random();

	private final AtomicInteger insertedCount = new AtomicInteger(0);

	private final WebClient webClient;
	private final EmployeeRepository employeeRepository;
	private final EmployeeSchedulerConfig schedulerConfig;

	public EmployeeSchedulerService(WebClient webClient, EmployeeRepository employeeRepository,
			EmployeeSchedulerConfig schedulerConfig) {
		this.webClient = webClient;
		this.employeeRepository = employeeRepository;
		this.schedulerConfig = schedulerConfig;
	}

	/**
	 * Scheduled task that runs at configured interval to fill the employee table
	 * with
	 * random employee data from RandomUser API.
	 */
	@Scheduled(every = "${employee.scheduler.interval:5m}")
	public void scheduleEmployeeInsertion() {
		if (!schedulerConfig.enabled()) {
			LOGGER.debug("Employee scheduler is disabled");
			return;
		}

		RequestContext.setReqId(RequestContext.generateReqId());
		String reqId = RequestContext.getReqId();
		long startTime = System.currentTimeMillis();
		LOGGER.info("Starting scheduled employee insertion task...");

		fetchAndInsertEmployees(reqId)
				.invoke(() -> RequestContext.setReqId(reqId))
				.subscribe()
				.with(
						count -> {
							RequestContext.setReqId(reqId);
							long processTime = System.currentTimeMillis() - startTime;
							LOGGER.info(
									"Successfully inserted {} employees in this run. Total inserted: {} processTimeMs={}",
									count, insertedCount.get(), processTime);
							RequestContext.clear();
						},
						failure -> {
							RequestContext.setReqId(reqId);
							long processTime = System.currentTimeMillis() - startTime;
							LOGGER.error("Failed to insert employees: {} processTimeMs={}", failure.getMessage(),
									processTime);
							RequestContext.clear();
						});
		RequestContext.clear();
	}

	private Uni<Integer> fetchAndInsertEmployees(String reqId) {
		int minInsert = Math.max(3, schedulerConfig.minInsert());
		String apiUrl = schedulerConfig.apiUrl() + "?results=" + minInsert;

		return webClient.getAbs(apiUrl)
				.send()
				.onItem().transformToUni(response -> {
					RequestContext.setReqId(reqId);
					if (response.statusCode() == 200) {
						return parseAndInsertEmployees(response.bodyAsString(), reqId);
					} else {
						return Uni.createFrom().failure(new RuntimeException(
								"Failed to fetch employees from API. Status: " + response.statusCode()));
					}
				})
				.onFailure().retry().atMost(3);
	}

	private Uni<Integer> parseAndInsertEmployees(String jsonString, String reqId) {
		return Uni.createFrom().item(jsonString)
				.onItem().transformToUni(json -> {
					RequestContext.setReqId(reqId);
					JsonObject root;
					try {
						root = new JsonObject(json);
					} catch (Exception e) {
						LOGGER.error("Failed to parse RandomUser API response: {}", e.getMessage());
						return Uni.createFrom().item(0);
					}

					JsonArray results = root.getJsonArray("results");
					if (results == null || results.isEmpty()) {
						LOGGER.warn("No employee data fetched from API");
						return Uni.createFrom().item(0);
					}

					List<Employee> employees = results.stream()
							.filter(JsonObject.class::isInstance)
							.map(JsonObject.class::cast)
							.map(this::mapToEmployee)
							.toList();

					return employeeRepository.createBulk(employees)
							.invoke(() -> RequestContext.setReqId(reqId))
							.onItemOrFailure().transformToUni((inserted, failure) -> {
								RequestContext.setReqId(reqId);
								if (failure != null) {
									LOGGER.warn("Failed to bulk insert employees: {}", failure.getMessage());
									return Uni.createFrom().item(0);
								} else {
									LOGGER.debug("Successfully inserted {} employees in bulk", inserted);
									insertedCount.addAndGet(inserted);
									return Uni.createFrom().item(inserted);
								}
							});
				});
	}

	private Employee mapToEmployee(JsonObject user) {
		JsonObject name = user.getJsonObject("name");
		String firstName = name.getString("first");
		String lastName = name.getString("last");
		String email = user.getString("email");
		String phone = user.getString("phone");
		String jobTitle = JOB_TITLES[random.nextInt(JOB_TITLES.length)];

		return new Employee(
				java.util.UUID.randomUUID(),
				firstName,
				lastName,
				email,
				phone,
				jobTitle);
	}
}
