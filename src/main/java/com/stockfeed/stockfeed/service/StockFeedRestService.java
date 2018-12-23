package com.stockfeed.stockfeed.service;

import java.util.List;

import pl.zankowski.iextrading4j.api.stocks.Chart;
import pl.zankowski.iextrading4j.api.stocks.Quote;

public interface StockFeedRestService {

	Quote getStockFeed(String symbol);
	
	List<Chart> getStockMonthChart(String symbol);
}
