package net.ostrekalovsky.rat.service;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Product {

    private final long code;
    private final String name;
    private final BigDecimal price;
    private final long count;
}
