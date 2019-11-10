package net.ostrekalovsky.rat.api;


import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.RATProps;
import net.ostrekalovsky.rat.service.DailyReport;
import net.ostrekalovsky.rat.service.FavouriteReport;
import net.ostrekalovsky.rat.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1", produces = "application/json")
@CrossOrigin(origins = "*")
public class ReceiptsController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private RATProps props;

    @GetMapping(value = "/favouriteProducts")
    public ResponseEntity<FavouriteReport> getFavouriteProducts(@RequestParam(value = "limit", required = false) Integer limit, @RequestParam("card") String card) {
        if (Objects.isNull(limit)) {
            limit = props.getReportsFavouriteProductsLimit();
        }
        Optional<FavouriteReport> optReport = reportService.getFavouriteProductsByCard(card, limit);
        return optReport
                .map(report -> new ResponseEntity<>(report, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/sumByDate", params = "date")
    @ResponseBody
    public DailyReport getDailyReport(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.getDailyReport(date);
    }
}
