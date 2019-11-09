package net.ostrekalovsky.rat.service;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class Receipt {

    private final String cardNumber;
    private final long date;
    private final List<Product> products;
}
