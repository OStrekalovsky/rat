package net.ostrekalovsky.rat.service;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Receipt {

    private final String cardNumber;
    private final long date;
    private final List<Product> products;
    private final BigDecimal sum;

    public Receipt(String cardNumber, long date, List<Product> products) {
        this.cardNumber = cardNumber;
        this.date = date;
        this.products = products;
        this.sum = products.stream().map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getCount()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }
}
