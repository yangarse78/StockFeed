package com.stockfeed.stockfeed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockFeedWatcher {

	
	@Autowired
	private StockFeedManager feedManager;
	

	/**
	 * Scheduled method that runs each "fixedDelay" time and checkers 
	 * if there were changes of 0.05% or 0.1$ in stock quotes
	 * @throws InterruptedException
	 */
	@Scheduled(fixedDelay = 1000)
	public void watch() throws InterruptedException {
		System.out.println("-----------------------------------------------------------------------------------------------------------------------");
		System.out.println("Proccess for quote details update started.");
		feedManager.checkLastQuotes();
	}
	
}
