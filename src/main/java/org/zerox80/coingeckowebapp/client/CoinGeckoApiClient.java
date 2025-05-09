package org.zerox80.coingeckowebapp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${api.coingecko.retry.max-attempts:3}")
    private int maxRetries;

    @Value("${api.coingecko.retry.delay-ms:60000}")
    private long retryDelayMs;


    public CoinGeckoApiClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }


    public List<CryptoCurrency> getCoins(String vsCurrency, int count) {
        String url = String.format("%s/coins/markets?vs_currency=%s&order=market_cap_desc&per_page=%d&page=1&sparkline=false&price_change_percentage=24h",
                API_BASE_URL, vsCurrency, count);

        for (int retryCount = 0; retryCount <= maxRetries; retryCount++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, CryptoCurrency.class));
                } else if (response.statusCode() == 429) {
                    log.warn("Rate limit reached. Attempt {} of {}. Waiting {} ms before retry...",
                            retryCount + 1, maxRetries, retryDelayMs);

                    if (retryCount < maxRetries) {
                        Thread.sleep(retryDelayMs);
                    } else {
                        log.error("Max retries ({}) reached for rate limit error (Status 429).", maxRetries);
                        return new ArrayList<>();
                    }
                } else {
                    log.error("Error getting coins/markets: Status Code {}", response.statusCode());
                    log.error("Error response body: {}", response.body());
                    return new ArrayList<>();
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing JSON response for coins/markets", e);
                return new ArrayList<>();
            } catch (IOException e) {
                log.error("IOException during coins/markets request, Attempt {}", retryCount + 1, e);
                if (retryCount < maxRetries) {
                    log.warn("Retrying after {} ms...", retryDelayMs);
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        log.error("Retry delay interrupted", ie);
                        Thread.currentThread().interrupt();
                        return new ArrayList<>();
                    }
                } else {
                    log.error("Max retries ({}) reached for IOException.", maxRetries, e);
                    return new ArrayList<>();
                }
            } catch (InterruptedException e) {
                log.error("Request interrupted during coins/markets request", e);
                Thread.currentThread().interrupt();
                return new ArrayList<>();
            }
        }

        log.error("Exited retry loop without success. Returning empty list.");
        return new ArrayList<>();
    }
}
