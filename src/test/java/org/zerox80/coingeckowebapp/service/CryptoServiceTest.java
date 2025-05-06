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

        // HIER IST DIE WICHTIGE ÄNDERUNG:
        // Erstelle das CryptoCurrency-Objekt mit dem neuen Konstruktor,
        // der alle Argumente erwartet.
        CryptoCurrency bitcoin = new CryptoCurrency(
                "bitcoin",                  // id
                "Bitcoin",                  // name
                "BTC",                      // symbol
                50000.0,                    // currentPrice (Beispielwert)
                2.5,                        // priceChangePercentage24h (Beispielwert)
                1000000000000.0,            // marketCap (Beispielwert)
                "http://example.com/btc.png" // image (Beispielwert)
        );
        // Die folgenden Zeilen sind nicht mehr nötig und würden Fehler verursachen,
        // da es keine Setter mehr gibt und die Felder final sind:
        // bitcoin.setId("bitcoin");
        // bitcoin.setName("Bitcoin");
        mockCoins.add(bitcoin);

        // Der Rest des Tests kann wahrscheinlich so bleiben:
        when(apiClient.getCoins(currency, count)).thenReturn(mockCoins);

        List<CryptoCurrency> actualCoins = cryptoService.getTopCryptocurrencies(currency, count);

        assertNotNull(actualCoins);
        assertEquals(mockCoins.size(), actualCoins.size());
        // Stelle sicher, dass du auf Eigenschaften zugreifst, die du im Konstruktor gesetzt hast
        assertEquals("Bitcoin", actualCoins.get(0).getName());
        assertEquals("bitcoin", actualCoins.get(0).getId());

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