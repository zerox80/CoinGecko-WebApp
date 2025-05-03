package org.zerox80.coingeckowebapp.service;

import org.springframework.stereotype.Service;
import org.zerox80.coingeckowebapp.client.CoinGeckoApiClient;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;

import java.util.List;

@Service
public class CryptoService {
    private final CoinGeckoApiClient apiClient;

    public CryptoService(CoinGeckoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<CryptoCurrency> getTopCryptocurrencies(String currency, int count) {
        return apiClient.getCoins(currency, count);
    }
}