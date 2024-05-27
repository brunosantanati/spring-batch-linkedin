package com.linkedin.batch;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class LinkedinBatchApplication {

	public static String[] tokens = new String[] { "order_id", "first_name", "last_name", "email", "cost", "item_id",
			"item_name", "ship_date" };

	public static String ORDER_SQL = "select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date "
			+ "from SHIPPED_ORDER order by order_id";
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;
	
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
		return this.stepBuilderFactory.get("chunkBasedStep").<Order, Order>chunk(10).reader(itemReader())
				.writer(new ItemWriter<Order>() {

					@Override
					public void write(List<? extends Order> items) throws Exception {
						System.out.println(String.format("Received list of size: %s", items.size()));
						items.forEach(System.out::println);
					}

				}).build();
	}

	@Bean
	public Job job() throws Exception {
		return this.jobBuilderFactory.get("job").start(chunkBasedStep()).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LinkedinBatchApplication.class, args);
	}

}
