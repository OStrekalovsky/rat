package net.ostrekalovsky.rat.service;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyReport {

    private final LocalDate date;
    private final BigDecimal sum;
}
