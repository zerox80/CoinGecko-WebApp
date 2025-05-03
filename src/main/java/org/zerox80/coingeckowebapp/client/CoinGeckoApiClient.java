package org.zerox80.coingeckowebapp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CoinGeckoApiClient {

    private static final Logger log = LoggerFactory.getLogger(CoinGeckoApiClient.class);
    private static final String API_BASE_URL = "https://api.coingecko.com/api/v3";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CoinGeckoApiClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public String getSimplePrice(String[] coinIds, String vsCurrency) {
        String idsString = String.join(",", coinIds);
        String url = String.format("%s/simple/price?ids=%s&vs_currencies=%s&include_24hr_change=true",
                API_BASE_URL, idsString, vsCurrency);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.error("Error getting simple price: Status Code {}", response.statusCode());
                log.error("Error response body: {}", response.body());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            log.error("IOException/InterruptedException during simple price request", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public List<CryptoCurrency> getCoins(String vsCurrency, int count) {
        String url = String.format("%s/coins/markets?vs_currency=%s&order=market_cap_desc&per_page=%d&page=1&sparkline=false&price_change_percentage=24h",
                API_BASE_URL, vsCurrency, count);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, CryptoCurrency.class));
            } else {
                log.error("Error getting coins/markets: Status Code {}", response.statusCode());
                log.error("Error response body: {}", response.body());
                return new ArrayList<>();
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON response for coins/markets", e);
            return new ArrayList<>();
        } catch (IOException | InterruptedException e) {
            log.error("IOException/InterruptedException during coins/markets request", e);
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        }
    }
}