package com.stockfeed.stockfeed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stockfeed.stockfeed.service.StockFeedRestService;

import pl.zankowski.iextrading4j.api.stocks.Chart;
import pl.zankowski.iextrading4j.api.stocks.Quote;

@Component
public class StockFeedManager {

	public final BigDecimal HALF_PERCENT = new BigDecimal(0.05);
	public final BigDecimal ONE_CENT = new BigDecimal(0.1);
	public final BigDecimal ONE_PERCENT = new BigDecimal(1);
	public final String AVERAGE = "_AVG";
	
	
	@Autowired
	private StockFeedRestService stockFeedRestService;
 
	@Value("#{'${stock.feed.symbols}'.split(',')}")
	private List<String> stockFeedSymbols;
	
	private Map<String, BigDecimal> lastQuotesPerSymbolMap = new HashMap<String, BigDecimal>(); 
	
	
	
	@PostConstruct
	public void StockMapInitialization() {
		System.out.println("Stock Map Initialization");
		for(String symbol : stockFeedSymbols) {
			symbol = symbol.toLowerCase();
			Quote quote = null;
			try {
				quote = stockFeedRestService.getStockFeed(symbol);
			} catch (Exception e) {
				System.out.println("ERROR: " + e);
			}
			lastQuotesPerSymbolMap.put(symbol, quote.getClose() != null ? quote.getClose() : quote.getPreviousClose());
			calculateLastMonthAveragePrice(symbol);
			System.out.println(symbol + " - Initialized");
		}
	}
	
	private void calculateLastMonthAveragePrice(String symbol) {
		List<Chart>  list = stockFeedRestService.getStockMonthChart(symbol);
		BigDecimal average = calculateClosePriceAverage(list);
		lastQuotesPerSymbolMap.put(symbol + AVERAGE, average);
	}

	private BigDecimal calculateClosePriceAverage(List<Chart> list) {
		BigDecimal sum = list.stream()
		        .map(Chart :: getClose)
		        .reduce(BigDecimal.ZERO, BigDecimal::add);
		return sum.divide(new BigDecimal(list.size()), RoundingMode.CEILING);
	}



	/**
	 * Runs over the list of desired symbols and check id quotes has been changes 
	 */
	public void checkLastQuotes() {
		System.out.println("Symbols: " + stockFeedSymbols.toString());
		for(String symbol : stockFeedSymbols) {
			Quote quote = null;
			try {
				quote = stockFeedRestService.getStockFeed(symbol);
			} catch (Exception e) {
				System.out.println("ERROR: " + e);
			}
			
			trackStockData(quote);
			notifyClient(quote);
			System.out.println("");
		}
	}

	/**
	 * In case of delta of quotes - notify the client
	 * @param lastQuote
	 * @param latestPrice
	 * @param symbol
	 */
	private void notifyClient(Quote quote) {
		notifyForPercentageDelta(quote);
		notifyForPriceDelta(quote);
		notifyClientOnAvgDelta(quote);
	}

	private void notifyForPercentageDelta(Quote quote) {
		String symbol = quote.getSymbol().toLowerCase();
		BigDecimal latestPrice = quote.getLatestPrice();
		BigDecimal lastQuote = lastQuotesPerSymbolMap.get(symbol);
		BigDecimal persentageDelta = checkPercentageDelta(lastQuote, latestPrice);		
		if(HALF_PERCENT.compareTo(persentageDelta.abs()) < 0) {
			if (persentageDelta.compareTo(BigDecimal.ZERO) > 0) {
				System.out.println("****!!!!!*****   " + symbol + " heigher for 0.05%   ****!!!!!*****");
			} else {
				System.out.println("****!!!!!*****   " + symbol + " lower for 0.05%   ****!!!!!*****");
			}
			System.out.println("CHANGE: " + persentageDelta.setScale(2, RoundingMode.CEILING) + "%");
		}
	}
	
	private void notifyForPriceDelta(Quote quote) {
		String symbol = quote.getSymbol().toLowerCase();
		BigDecimal latestPrice = quote.getLatestPrice();
		BigDecimal lastQuote = lastQuotesPerSymbolMap.get(symbol);
		BigDecimal priceDelta = checkPriceDelta(lastQuote, latestPrice);
		if(ONE_CENT.compareTo(priceDelta.abs()) < 0) {
			if (priceDelta.compareTo(BigDecimal.ZERO) > 0) {
				System.out.println("****!!!!!*****   " + symbol + " heigher for 0.1$   *****!!!!!*****");
			} else {
				System.out.println("****!!!!!*****   " + symbol + " lower for 0.1$   *****!!!!!*****");
			}
			System.out.println("CHANGE: " + priceDelta + "$");
		}
	}
	
	private void notifyClientOnAvgDelta(Quote quote) {
		String symbol = quote.getSymbol().toLowerCase();
		BigDecimal latestPrice = quote.getLatestPrice();
		BigDecimal avg = lastQuotesPerSymbolMap.get(symbol + AVERAGE);
		BigDecimal avgVolumeDelta = checkPercentageDelta(latestPrice, avg);
		if(ONE_PERCENT.compareTo(avgVolumeDelta.abs()) < 0) {
			if (avgVolumeDelta.compareTo(BigDecimal.ZERO) > 0) {
				System.out.println("****!!!!!*****   " + symbol + " heigher for 1% then AVG price for last month  *****!!!!!*****");
			} else {
				System.out.println("****!!!!!*****   " + symbol + " lower for 1% then AVG price for last month  *****!!!!!*****");
			}
			System.out.println("CHANGE for: " + avgVolumeDelta.setScale(2, RoundingMode.CEILING)  + "% from AVG, Average is: " 
											+ avg.setScale(2, RoundingMode.CEILING));
		}
	}
	
	private BigDecimal checkPriceDelta(BigDecimal lastQuote, BigDecimal latestPrice) {
		return lastQuote.subtract(latestPrice);
	}

	private BigDecimal checkPercentageDelta(BigDecimal lastQuote, BigDecimal latestPrice) {
		return (lastQuote.subtract(latestPrice)).divide(lastQuote, 10, RoundingMode.DOWN).multiply(new BigDecimal(100));
	}

	
	/**
	 * Printing stock details and changes
	 * @param quote
	 * @param lastQuote
	 */
	private void trackStockData(Quote quote) {
		BigDecimal lastQuote = lastQuotesPerSymbolMap.get(quote.getSymbol().toLowerCase());
		System.out.println("");
		System.out.println("****** ****** ***** ****** ***** ***** ***** *****");
		System.out.println(quote.getCompanyName());
		System.out.println("EXCHANGE: " + quote.getPrimaryExchange());
		System.out.println("PRICE: " + quote.getLatestPrice());
		System.out.println("LATEST VOLUME: " + quote.getLatestVolume());
		System.out.println("LAST QUOTE: " + lastQuote);
		System.out.println("LATEST PRICE: " + quote.getLatestPrice());
		System.out.println("CLOSE VOLUME: " + quote.getClose());
		System.out.println("****** ****** ***** ****** ***** ***** ***** *****");
	}
	
	
	
	
	
}
