package net.ostrekalovsky.rat.service;

import java.time.LocalDate;
import java.util.Optional;

public interface ReportService {

    Optional<FavouriteReport> getFavouriteProductsByCard(String card, int limit);

    DailyReport getDailyReport(LocalDate date);
}
