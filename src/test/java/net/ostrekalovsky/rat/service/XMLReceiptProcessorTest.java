package net.ostrekalovsky.rat.service;


import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.RATProps;
import net.ostrekalovsky.rat.service.importer.XMLReceiptProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
public class XMLReceiptProcessorTest {

    @TestConfiguration
    static class TestContextConfiguration {

        @Bean
        public ReceiptProcessor getReceiptParser() {
            return new XMLReceiptProcessor();
        }

        @Bean
        RATProps getImportProps() {
            RATProps importProps = new RATProps();
            importProps.setImportDir("src/test/resources/importdir");
            return importProps;
        }

        @Bean
        ReceiptStorage getReceiptStorage() {
            return new ReceiptStorage() {
                @Override
                public void store(File fileName, List<Receipt> receipts) throws ReceiptsStoreException {
                    log.info("Store {} receipts from file:{}", receipts.size(), fileName.getAbsolutePath());
                    assertThat(receipts).containsExactly(
                            new Receipt("78483", 1528205653605L,
                                    Arrays.asList(
                                            new Product(15, "Чипсы", new BigDecimal("47.20"), 1),
                                            new Product(3, "Вода", new BigDecimal("25.99"), 2))),
                            new Receipt("77394", 1528205653605L,
                                    Collections.singletonList(
                                            new Product(16, "Увлажнитель картошки", new BigDecimal("1500.00"), 99))));
                }

                @Override
                public boolean wasFileProcessed(File file) {
                    return false;
                }
            };
        }

    }

    @Autowired
    private
    ReceiptProcessor receiptProcessor;

    @Test
    public void sampleFileShouldBeParsed() throws ReceiptsImportException {
        receiptProcessor.parseAndStore();
    }
}
