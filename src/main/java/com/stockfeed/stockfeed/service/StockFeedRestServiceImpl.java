package com.stockfeed.stockfeed.service;

import org.springframework.stereotype.Service;

import pl.zankowski.iextrading4j.api.stocks.Chart;
import pl.zankowski.iextrading4j.api.stocks.ChartRange;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import pl.zankowski.iextrading4j.client.rest.manager.RestClient;
import pl.zankowski.iextrading4j.client.rest.manager.RestClientMetadata;
import pl.zankowski.iextrading4j.client.rest.manager.RestManager;
import pl.zankowski.iextrading4j.client.rest.manager.RestRequest;
import pl.zankowski.iextrading4j.client.rest.manager.RestResponse;
import pl.zankowski.iextrading4j.client.rest.request.stocks.ChartRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;


@Service
public class StockFeedRestServiceImpl implements StockFeedRestService{

	@Override
	public Quote getStockFeed(String symbol) {

        final RestRequest<Quote> request = new QuoteRequestBuilder()
                .withSymbol(symbol)
                .build();
		
        Client client = ClientBuilder.newClient(); 
        RestClient restClient = new RestClient(client, new RestClientMetadata());
        RestManager restManager = new RestManager(restClient);
        RestResponse<Quote> res = restManager.executeRequest(request);
		return res.getResponse();
	}

	@Override
	public List<Chart> getStockMonthChart(String symbol) {

		final ChartRange chartRange = ChartRange.ONE_MONTH;
		final RestRequest<List<Chart>> request = new ChartRequestBuilder()
	            .withSymbol(symbol)
	            .withChartRange(chartRange)
	            .build();
		
        Client client = ClientBuilder.newClient(); 
        RestClient restClient = new RestClient(client, new RestClientMetadata());
        RestManager restManager = new RestManager(restClient);
        RestResponse<List<Chart>> res = restManager.executeRequest(request);

		return res.getResponse();
	}
	
}
