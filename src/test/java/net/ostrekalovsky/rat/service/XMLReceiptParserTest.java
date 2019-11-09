package net.ostrekalovsky.rat.service;


import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.ImportProps;
import net.ostrekalovsky.rat.service.importer.XMLReceiptParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
public class XMLReceiptParserTest {

    @TestConfiguration
    static class TestContextConfiguration {

        @Bean
        public ReceiptParser getReceiptParser() {
            return new XMLReceiptParser();
        }

        @Bean
        ImportProps getImportProps() {
            ImportProps importProps = new ImportProps();
//            importProps.setDir("/home/ostrekalovsky/Downloads/maxi/import/");
            importProps.setImportDir("src/test/resources/importdir");
            return importProps;
        }

        @Bean
        ReceiptStorage getReceiptStorage() {
            return (fileName, receipts) -> {
                log.info("Store {} receipts from file:{}", receipts.size(), fileName.getAbsolutePath());
                assertThat(receipts).containsExactlyInAnyOrder(
                        new Receipt("78483", 1528205653605L,
                                Arrays.asList(
                                        new Product(15, "Чипсы", new BigDecimal("47.20"), 1),
                                        new Product(3, "Вода", new BigDecimal("25.99"), 2))),
                        new Receipt("77394", 1528205653605L,
                                Collections.singletonList(
                                        new Product(16, "Увлажнитель картошки", new BigDecimal("1500.00"), 99))));
            };
        }

    }

    @Autowired
    private
    ReceiptParser receiptParser;

    @Test
    public void sampleFileShouldBeParsed() throws ReceiptsImportException {
        receiptParser.parseAndStore();
    }
}
