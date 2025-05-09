package org.zerox80.coingeckowebapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerox80.coingeckowebapp.model.Portfolio;
import org.zerox80.coingeckowebapp.model.User;
import org.zerox80.coingeckowebapp.repository.PortfolioRepository;
import org.zerox80.coingeckowebapp.repository.UserRepository;
import org.zerox80.coingeckowebapp.repository.PortfolioEntryRepository;
import org.zerox80.coingeckowebapp.model.PortfolioEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository; // Wird benötigt, um den User frisch zu laden in manchen Szenarien
    private final PortfolioEntryRepository portfolioEntryRepository;

    @Value("${portfolio.initial.balance:1000000.00}")
    private BigDecimal initialBalance;

    @Value("${portfolio.initial.currency:EUR}")
    private String initialCurrency;

    // Custom Exception for insufficient funds
    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository, PortfolioEntryRepository portfolioEntryRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.portfolioEntryRepository = portfolioEntryRepository;
    }

    @Transactional
    public Portfolio createInitialPortfolio(User user) {
        if (portfolioRepository.findByUser(user).isPresent()) {
            logger.warn("Portfolio for user {} already exists. Skipping creation.", user.getUsername());
            return portfolioRepository.findByUser(user).get();
        }

        Portfolio portfolio = new Portfolio(user, initialBalance, initialCurrency);
        user.setPortfolio(portfolio); // Bidirektionale Beziehung setzen
        
        logger.info("Creating initial portfolio for user {} with balance {} {}", user.getUsername(), initialBalance, initialCurrency);
        return portfolioRepository.save(portfolio);
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioForUser(User user) {
        return portfolioRepository.findByUserWithEntries(user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found for user: " + user.getUsername()));
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return getPortfolioForUser(user);
    }

    @Transactional
    public void buyCryptocurrency(User user, String cryptocurrencyId, String cryptocurrencyName, String cryptocurrencySymbol, double currentPriceDouble, BigDecimal amountToBuy) {
        Portfolio portfolio = getPortfolioForUser(user); // This already fetches with entries

        BigDecimal currentPrice = BigDecimal.valueOf(currentPriceDouble);
        BigDecimal totalCost = amountToBuy.multiply(currentPrice).setScale(portfolio.getBalance().scale(), RoundingMode.HALF_UP);

        if (portfolio.getBalance().compareTo(totalCost) < 0) {
            throw new InsufficientFundsException("Insufficient funds. You need " + totalCost + " " + portfolio.getCurrency() +
                                               " but only have " + portfolio.getBalance() + " " + portfolio.getCurrency() + ".");
        }

        portfolio.setBalance(portfolio.getBalance().subtract(totalCost));

        Optional<PortfolioEntry> existingEntryOpt = portfolioEntryRepository.findByPortfolioAndCryptocurrencyId(portfolio, cryptocurrencyId);

        if (existingEntryOpt.isPresent()) {
            PortfolioEntry existingEntry = existingEntryOpt.get();
            BigDecimal oldAmount = existingEntry.getAmount();
            BigDecimal oldAvgPrice = existingEntry.getAveragePurchasePrice();
            
            BigDecimal newAmount = oldAmount.add(amountToBuy);
            // ((oldAmount * oldAvgPrice) + (amountToBuy * currentPrice)) / newAmount
            BigDecimal newAvgPrice = (oldAmount.multiply(oldAvgPrice))
                                     .add(amountToBuy.multiply(currentPrice))
                                     .divide(newAmount, 4, RoundingMode.HALF_UP); // Scale 4 for price

            existingEntry.setAmount(newAmount);
            existingEntry.setAveragePurchasePrice(newAvgPrice);
            // portfolioEntryRepository.save(existingEntry); // Rely on cascading from portfolio save
        } else {
            PortfolioEntry newEntry = new PortfolioEntry(portfolio, cryptocurrencyId, cryptocurrencySymbol, cryptocurrencyName, amountToBuy, currentPrice);
            portfolio.getEntries().add(newEntry); // Add to portfolio's list of entries
            // portfolioEntryRepository.save(newEntry); // Rely on cascading from portfolio save
        }

        portfolioRepository.save(portfolio);
        logger.info("Successfully processed purchase for user {}: {} of {} for {} {}", 
            user.getUsername(), amountToBuy, cryptocurrencyId, totalCost, portfolio.getCurrency());
    }
} 