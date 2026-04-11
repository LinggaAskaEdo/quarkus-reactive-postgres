package org.otis.fruit.infrastructure;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.otis.fruit.domain.FruitRepository;
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
public class FruitSchedulerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(FruitSchedulerService.class);

	private final Random random = new Random();
	private final AtomicInteger insertedCount = new AtomicInteger(0);

	private final WebClient webClient;
	private final FruitRepository fruitRepository;
	private final FruitSchedulerConfig schedulerConfig;

	public FruitSchedulerService(WebClient webClient, FruitRepository fruitRepository,
			FruitSchedulerConfig schedulerConfig) {
		this.webClient = webClient;
		this.fruitRepository = fruitRepository;
		this.schedulerConfig = schedulerConfig;
	}

	/**
	 * Scheduled task that runs at configured interval to fill the fruit table with
	 * random fruits.
	 */
	@Scheduled(every = "${fruit.scheduler.interval:1m}")
	public void scheduleFruitInsertion() {
		if (!schedulerConfig.enabled()) {
			LOGGER.debug("Fruit scheduler is disabled");
			return;
		}

		RequestContext.setReqId(RequestContext.generateReqId());
		String reqId = RequestContext.getReqId();
		long startTime = System.currentTimeMillis();
		LOGGER.info("Starting scheduled fruit insertion task...");

		fetchAndInsertFruits(reqId)
				.invoke(() -> RequestContext.setReqId(reqId))
				.subscribe()
				.with(
						count -> {
							RequestContext.setReqId(reqId);
							long processTime = System.currentTimeMillis() - startTime;
							LOGGER.info(
									"Successfully inserted {} fruits in this run. Total inserted: {} processTimeMs={}",
									count, insertedCount.get(), processTime);
							RequestContext.clear();
						},
						failure -> {
							RequestContext.setReqId(reqId);
							long processTime = System.currentTimeMillis() - startTime;
							LOGGER.error("Failed to insert fruits: {} processTimeMs={}", failure.getMessage(),
									processTime);
							RequestContext.clear();
						});
		RequestContext.clear();
	}

	private Uni<Integer> fetchAndInsertFruits(String reqId) {
		return webClient.getAbs(schedulerConfig.apiUrl())
				.send()
				.onItem().transformToUni(response -> {
					RequestContext.setReqId(reqId);
					if (response.statusCode() == 200) {
						return handleSuccessResponse(response.bodyAsString(), reqId);
					} else {
						return Uni.createFrom().failure(new RuntimeException(
								"Failed to fetch fruits from API. Status: " + response.statusCode()));
					}
				})
				.onFailure().retry().atMost(3);
	}

	private Uni<Integer> handleSuccessResponse(String jsonString, String reqId) {
		return Uni.createFrom().item(jsonString)
				.onItem().transformToUni(json -> {
					RequestContext.setReqId(reqId);
					JsonArray jsonArray;

					try {
						jsonArray = new JsonArray(json);
					} catch (Exception e) {
						LOGGER.error("Failed to parse API response: {}", e.getMessage());
						return Uni.createFrom().item(0);
					}

					if (jsonArray.isEmpty()) {
						LOGGER.warn("No fruits fetched from API");
						return Uni.createFrom().item(0);
					}

					// Convert to mutable list and shuffle
					List<JsonObject> fruits = jsonArray.stream()
							.filter(JsonObject.class::isInstance)
							.map(JsonObject.class::cast)
							.collect(Collectors.toList());

					Collections.shuffle(fruits, random);

					// Get configurable minimum count (enforce minimum of 3)
					int minInsert = Math.max(3, schedulerConfig.minInsert());

					// Take minimum number of fruits or all available if less than minimum
					int count = Math.min(minInsert, fruits.size());
					List<JsonObject> selectedFruits = fruits.subList(0, count);

					return insertFruitsBulk(selectedFruits, reqId);
				});
	}

	private Uni<Integer> insertFruitsBulk(List<JsonObject> fruits, String reqId) {
		List<String> fruitNames = fruits.stream()
				.map(fruit -> fruit.getString("name", "Unknown Fruit " + random.nextInt(1000)))
				.toList();

		return fruitRepository.createBulk(fruitNames)
				.invoke(() -> RequestContext.setReqId(reqId))
				.onItemOrFailure().transformToUni((inserted, failure) -> {
					RequestContext.setReqId(reqId);
					if (failure != null) {
						LOGGER.warn("Failed to bulk insert fruits: {}", failure.getMessage());
						return Uni.createFrom().item(0);
					} else {
						LOGGER.debug("Successfully inserted {} fruits in bulk", inserted);
						insertedCount.addAndGet(inserted);
						return Uni.createFrom().item(inserted);
					}
				});
	}

	public int getTotalInsertedCount() {
		return insertedCount.get();
	}
}
