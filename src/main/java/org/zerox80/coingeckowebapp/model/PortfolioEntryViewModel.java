package org.zerox80.coingeckowebapp.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PortfolioEntryViewModel {
    private PortfolioEntry entry;
    private BigDecimal currentPrice;
    private BigDecimal currentMarketValue;
    private BigDecimal profitOrLoss;
    private BigDecimal profitOrLossPercentage; // Added for percentage P/L

    public PortfolioEntryViewModel(PortfolioEntry entry, BigDecimal currentPrice) {
        this.entry = entry;
        this.currentPrice = currentPrice != null ? currentPrice : BigDecimal.ZERO; // Handle null current price

        if (entry != null && entry.getAmount() != null) {
            this.currentMarketValue = entry.getAmount().multiply(this.currentPrice);
            if (entry.getAveragePurchasePrice() != null) {
                BigDecimal totalPurchaseCost = entry.getAmount().multiply(entry.getAveragePurchasePrice());
                this.profitOrLoss = this.currentMarketValue.subtract(totalPurchaseCost);
                if (totalPurchaseCost.compareTo(BigDecimal.ZERO) != 0) {
                    this.profitOrLossPercentage = this.profitOrLoss
                            .divide(totalPurchaseCost, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal("100"));
                } else {
                    // Avoid division by zero if purchase cost is zero (e.g., airdrop or no cost basis)
                    // If current value is positive, it's 100% profit, if zero, 0%, if negative, -100% (though less likely here)
                    if (this.currentMarketValue.compareTo(BigDecimal.ZERO) > 0) {
                        this.profitOrLossPercentage = new BigDecimal("100");
                    } else if (this.currentMarketValue.compareTo(BigDecimal.ZERO) < 0) {
                        this.profitOrLossPercentage = new BigDecimal("-100");
                    }
                     else {
                        this.profitOrLossPercentage = BigDecimal.ZERO;
                    }
                }
            } else {
                this.profitOrLoss = this.currentMarketValue; // If no purchase price, current value is profit
                this.profitOrLossPercentage = this.currentMarketValue.compareTo(BigDecimal.ZERO) != 0 ? new BigDecimal("100") : BigDecimal.ZERO;
            }
        } else {
            this.currentMarketValue = BigDecimal.ZERO;
            this.profitOrLoss = BigDecimal.ZERO;
            this.profitOrLossPercentage = BigDecimal.ZERO;
        }
    }

    // Delegate getters for easier access in Thymeleaf
    public String getCryptocurrencyName() { return entry.getCryptocurrencyName(); }
    public String getCryptocurrencySymbol() { return entry.getCryptocurrencySymbol(); }
    public BigDecimal getAmount() { return entry.getAmount(); }
    public BigDecimal getAveragePurchasePrice() { return entry.getAveragePurchasePrice(); }
    public Long getId() { return entry.getId(); }
} 