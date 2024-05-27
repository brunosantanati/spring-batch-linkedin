package com.linkedin.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
@EnableBatchProcessing
public class LinkedinBatchApplication {

	public static String[] names = new String[] { "orderId", "firstName", "lastName", "email", "cost", "itemId",
			"itemName", "shipDate" };
	
	public static String ORDER_SQL = "select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date "
			+ "from SHIPPED_ORDER order by order_id";
	
	public static String INSERT_ORDER_SQL = "insert into "
			+ "SHIPPED_ORDER_OUTPUT(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date)"
			+ " values(:orderId,:firstName,:lastName,:email,:itemId,:itemName,:cost,:shipDate)";

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	@Bean
	public ItemProcessor<TrackedOrder, TrackedOrder> freeShippingItemProcessor() {
		return new FreeShippingItemProcessor();
	}

	@Bean
	public ItemProcessor<Order, TrackedOrder> compositeItemProcessor() {
		return new CompositeItemProcessorBuilder<Order,TrackedOrder>()
				.delegates(orderValidatingItemProcessor(), trackedOrderItemProcessor(), freeShippingItemProcessor())
				.build();
	}

	@Bean
	public ItemProcessor<Order, TrackedOrder> trackedOrderItemProcessor() {
		return new TrackedOrderItemProcessor(); 
	}

	@Bean
	public ItemProcessor<Order, Order> orderValidatingItemProcessor() {
		BeanValidatingItemProcessor<Order> itemProcessor = new BeanValidatingItemProcessor<Order>();
		itemProcessor.setFilter(true);
		return itemProcessor;
	}

	@Bean
	public ItemWriter<TrackedOrder> itemWriter() {
		return new JsonFileItemWriterBuilder<TrackedOrder>()
				.jsonObjectMarshaller(new JacksonJsonObjectMarshaller<TrackedOrder>())
				.resource(new FileSystemResource("/data/shipped_orders_output.json"))
				.name("jsonItemWriter")
				.build();
	}

	@Bean
	public PagingQueryProvider queryProvider() throws Exception {
		SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
		
		factory.setSelectClause("select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date");
		factory.setFromClause("from SHIPPED_ORDER");
		factory.setSortKey("order_id");
		factory.setDataSource(dataSource);
		return factory.getObject();
	}

	@Bean
	public ItemReader<Order> itemReader() throws Exception {
		return new JdbcPagingItemReaderBuilder<Order>()
				.dataSource(dataSource)
				.name("jdbcCursorItemReader")
				.queryProvider(queryProvider())
				.rowMapper(new OrderRowMapper())
				.pageSize(10)
				.build();

	}

	@Bean
	public Step chunkBasedStep() throws Exception {
		return this.stepBuilderFactory.get("chunkBasedStep")
				.<Order, TrackedOrder>chunk(10)
				.reader(itemReader())
				.processor(compositeItemProcessor())
				.faultTolerant()
				.retry(OrderProcessingException.class)
				.retryLimit(3)
				.listener(new CustomRetryListener())
				.writer(itemWriter())
				.build();
	}

	@Bean
	public Job job() throws Exception {
		return this.jobBuilderFactory.get("job").start(chunkBasedStep()).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LinkedinBatchApplication.class, args);
	}

}
