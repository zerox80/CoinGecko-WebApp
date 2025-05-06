package org.zerox80.coingeckowebapp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CryptoCurrency {

    private final String id;
    private final String name;
    private final String symbol;

    @JsonProperty("current_price")
    private final double currentPrice;

    @JsonProperty("price_change_percentage_24h")
    private final double priceChangePercentage24h;

    @JsonProperty("market_cap")
    private final double marketCap;

    private final String image;

    @JsonCreator
    public CryptoCurrency(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("current_price") double currentPrice,
            @JsonProperty("price_change_percentage_24h") double priceChangePercentage24h,
            @JsonProperty("market_cap") double marketCap,
            @JsonProperty("image") String image) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.currentPrice = currentPrice;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.marketCap = marketCap;
        this.image = image;
    }
}