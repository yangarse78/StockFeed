package com.stockfeed.stockfeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 
 * @author Yan Garse
 * phone: 052-6313361
 *
 */
@SpringBootApplication
@EnableScheduling
public class StockfeedApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockfeedApplication.class, args);
	}

}

