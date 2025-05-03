package org.zerox80.coingeckowebapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;
import org.zerox80.coingeckowebapp.service.CryptoService;

import java.util.Collections;
import java.util.List;

@Controller
public class CryptoController {

    private static final Logger log = LoggerFactory.getLogger(CryptoController.class);
    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(defaultValue = "eur") String currency,
                        @RequestParam(defaultValue = "10") int count) {

        log.info("Received request for index page. Currency: '{}', Count: {}", currency, count);

        String warningMessage = null;
        if (count <= 0) {
            log.warn("Invalid count requested: {}. Using default value 10.", count);
            count = 10;
            warningMessage = "Invalid count specified. Using default value (10).";
        } else if (count > 250) { // Assuming 250 is a reasonable limit
            log.warn("Requested count {} exceeds maximum (250). Reducing to 250.", count);
            count = 250;
            warningMessage = "Requested count exceeds maximum. Using maximum value (250).";
        }

        if (warningMessage != null) {
            model.addAttribute("warningMessage", warningMessage);
        }

        List<CryptoCurrency> coins = Collections.emptyList();
        try {
            log.debug("Calling CryptoService for Currency: '{}', Count: {}", currency, count);
            coins = cryptoService.getTopCryptocurrencies(currency, count);
            log.info("Successfully retrieved {} cryptocurrencies for currency '{}'.", coins.size(), currency);
        } catch (Exception e) {
            log.error("Error retrieving cryptocurrency data for currency '{}', count {}: {}", currency, count, e.getMessage(), e);
            model.addAttribute("errorMessage", "Could not load cryptocurrency data. The API might be unavailable or currency '" + currency + "' might not be supported. Please try again later.");
        }

        model.addAttribute("coins", coins);
        model.addAttribute("currency", currency);
        return "index";
    }
}