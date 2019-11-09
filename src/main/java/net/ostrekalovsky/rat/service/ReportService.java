package net.ostrekalovsky.rat.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

public interface ReportService {

    Optional<FavouriteReport> getFavouriteProductsByCard(String card, int limit);

    DailyReport getDailyReport(LocalDate date);
}
