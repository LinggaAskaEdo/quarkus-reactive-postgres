package org.otis.employee.infrastructure;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.otis.employee.domain.Employee;
import org.otis.employee.domain.EmployeeRepository;
import org.otis.shared.util.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmployeeSchedulerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeSchedulerService.class);

	private static final String[] FIRST_NAMES = {
			"John", "Jane", "Alice", "Bob", "Carol", "David", "Emma", "Frank",
			"Grace", "Henry", "Ivy", "Jack", "Karen", "Leo", "Mia", "Noah",
			"Olivia", "Peter", "Quinn", "Rachel", "Sam", "Tina", "Uma", "Victor",
			"Wendy", "Xander", "Yara", "Zane"
	};

	private static final String[] LAST_NAMES = {
			"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
			"Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
			"Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"
	};

	private static final String[] JOB_TITLES = {
			"Software Engineer", "Product Manager", "Data Analyst", "DevOps Engineer",
			"QA Engineer", "UI/UX Designer", "Backend Developer", "Frontend Developer",
			"Full Stack Developer", "Scrum Master", "Technical Lead", "Architect"
	};

	private final Random random = new Random();
	private final AtomicInteger insertedCount = new AtomicInteger(0);

	private final EmployeeRepository employeeRepository;
	private final EmployeeSchedulerConfig schedulerConfig;

	public EmployeeSchedulerService(EmployeeRepository employeeRepository,
			EmployeeSchedulerConfig schedulerConfig) {
		this.employeeRepository = employeeRepository;
		this.schedulerConfig = schedulerConfig;
	}

	/**
	 * Scheduled task that runs at configured interval to fill the employee table with
	 * random employee data.
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

		generateAndInsertEmployees(reqId)
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

	private Uni<Integer> generateAndInsertEmployees(String reqId) {
		int minInsert = Math.max(3, schedulerConfig.minInsert());
		List<Employee> employees = IntStream.range(0, minInsert)
				.mapToObj(i -> generateRandomEmployee())
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
	}

	private Employee generateRandomEmployee() {
		String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
		String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
		String jobTitle = JOB_TITLES[random.nextInt(JOB_TITLES.length)];
		String email = (firstName + "." + lastName + random.nextInt(1000)).toLowerCase() + "@example.com";
		String phone = generateRandomPhone();

		return new Employee(UUID.randomUUID(), firstName, lastName, email, phone, jobTitle);
	}

	private String generateRandomPhone() {
		return String.format("+1-%03d-%03d-%04d",
				random.nextInt(900) + 100,
				random.nextInt(900) + 100,
				random.nextInt(10000));
	}

	public int getTotalInsertedCount() {
		return insertedCount.get();
	}
}
