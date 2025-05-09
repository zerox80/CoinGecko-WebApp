package org.zerox80.coingeckowebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableCaching
public class CoinGeckoWebApplication {
    private static final Logger logger = LoggerFactory.getLogger(CoinGeckoWebApplication.class);
    public static void main(String[] args) {
        // Log startup
        logger.info("Starting CoinGeckoWebApplication");
        // Let SpringApplication.run(...) throw any real errors,
        // and allow DevTools' SilentExitException to bubble up
        SpringApplication.run(CoinGeckoWebApplication.class, args);
        // If we get here, it really started successfully
        logger.info("CoinGeckoWebApplication started successfully");
    }
}
