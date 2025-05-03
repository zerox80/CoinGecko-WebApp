package org.zerox80.coingeckowebapp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zerox80.coingeckowebapp.client.CoinGeckoApiClient;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    @Mock
    private CoinGeckoApiClient apiClient;

    @InjectMocks
    private CryptoService cryptoService;

    @Test
    void testGetTopCryptocurrencies_Success() {
        String currency = "eur";
        int count = 5;
        List<CryptoCurrency> mockCoins = new ArrayList<>();
        CryptoCurrency bitcoin = new CryptoCurrency();
        bitcoin.setId("bitcoin");
        bitcoin.setName("Bitcoin");
        mockCoins.add(bitcoin);

        when(apiClient.getCoins(currency, count)).thenReturn(mockCoins);

        List<CryptoCurrency> actualCoins = cryptoService.getTopCryptocurrencies(currency, count);

        assertNotNull(actualCoins);
        assertEquals(mockCoins.size(), actualCoins.size());
        assertEquals(mockCoins.get(0).getName(), actualCoins.get(0).getName());

        verify(apiClient, times(1)).getCoins(currency, count);
    }

    @Test
    void testGetTopCryptocurrencies_ApiReturnsEmptyList() {
        String currency = "usd";
        int count = 10;
        when(apiClient.getCoins(currency, count)).thenReturn(new ArrayList<>());

        List<CryptoCurrency> actualCoins = cryptoService.getTopCryptocurrencies(currency, count);

        assertNotNull(actualCoins);
        assertTrue(actualCoins.isEmpty());
        verify(apiClient, times(1)).getCoins(currency, count);
    }
}