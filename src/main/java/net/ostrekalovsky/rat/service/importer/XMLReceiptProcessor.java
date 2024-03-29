package net.ostrekalovsky.rat.service.importer;

import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.RATProps;
import net.ostrekalovsky.rat.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of receipts parser from XML files.
 * <br/>
 * The implementation (SAX Parser) was chosen based on the following considerations: <br/>
 * - We should upload data from file only after making sure that its structure is correct.
 * So In any case, we must first read this file.
 * <br/>
 * - Performance testing showed that XML parsing is not a real bottleneck in this case.
 * Implementation doesn't parse all new files at once before the first one will be saved by a storage.
 * It alternates between parsing and loading without using too much memory and does not delay loading too much,
 * because parsing is not a bottleneck.
 * Under certain circumstances, the parser can be replaced by a streaming one to reduce memory overhead and
 * to maximize storage bandwidth utilization, but I don’t think there is a need for this now.
 */
@Slf4j
@Service
public class XMLReceiptProcessor implements ReceiptProcessor {

    @Autowired
    private RATProps RATProps;

    @Autowired
    private ReceiptStorage storage;

    private Document loadDoc(File file) throws ReceiptsImportException {
        try {
            log.info("Open file for parsing:{}", file.getAbsolutePath());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document parsedDocument = builder.parse(file);
            parsedDocument.getDocumentElement().normalize();
            log.info("File structure was loaded");
            return parsedDocument;
        } catch (ParserConfigurationException | SAXException e) {
            throw new ReceiptsImportException("Failed to parse file:" + file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new ReceiptsImportException("Failed to open the file:" + file.getAbsolutePath(), e);
        }
    }

    private List<Receipt> parseSales(Document doc) {
        log.info("Creating receipts from file");
        NodeList salesNodes = doc.getElementsByTagName("SALE");
        List<Receipt> receipts = new ArrayList<>();
        for (int saleIdx = 0; saleIdx < salesNodes.getLength(); saleIdx++) {
            Node saleNode = salesNodes.item(saleIdx);
            if (saleNode.getNodeType() == Node.ELEMENT_NODE) {
                Element sale = (Element) saleNode;
                String cardNumber = sale.getElementsByTagName("CARD_NUMBER").item(0).getTextContent();
                long timestamp = Long.parseLong(sale.getElementsByTagName("DATE").item(0).getTextContent());
                log.debug("Card number={}, date={}", cardNumber, new java.util.Date(timestamp));
                NodeList saleProducts = sale.getElementsByTagName("PRODUCT");
                List<Product> products = new ArrayList<>(10);
                for (int i = 0; i < saleProducts.getLength(); i++) {
                    Node productNode = saleProducts.item(i);
                    if (productNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element productElement = (Element) productNode;
                        Product product = new Product(
                                Long.parseLong(productElement.getElementsByTagName("PRODUCT_CODE").item(0).getTextContent()),
                                productElement.getElementsByTagName("NAME").item(0).getTextContent(),
                                new BigDecimal(productElement.getElementsByTagName("PRICE").item(0).getTextContent().replaceAll(",", ".")),
                                Long.parseLong(productElement.getElementsByTagName("COUNT").item(0).getTextContent())
                        );
                        log.debug("Add product:{}", product);
                        products.add(product);
                    }
                }
                receipts.add(new Receipt(cardNumber, timestamp, products));
                log.info("Number of products in receipt:{}", products.size());
            }
        }
        log.info("Number of receipts in file:{}", receipts.size());
        return receipts;

    }

    @Override
    public void parseAndStore() throws ReceiptsImportException {
        File importDir = new File(RATProps.getImportDir());
        log.info("Read files from directory:{}", importDir.getAbsolutePath());
        File[] files = importDir.listFiles(File::isFile);
        if (Objects.isNull(files)) {
            throw new ReceiptsImportException("Is not a directory:" + importDir);
        }
        for (File file : files) {
            try {
                if (storage.wasFileProcessed(file)) {
                    log.info("File:{} was uploaded and will be skipped", file.getName());
                    continue;
                }
                Document doc = loadDoc(file);
                storage.store(file, parseSales(doc));
            } catch (ReceiptsImportException e) {
                log.error("Bad file:" + file.getAbsolutePath() + ". Now file will be skipped, another try will be on the next import iteration", e);
            } catch (ReceiptsStoreException e) {
                log.error("Failed to save all receipts from file:" + file.getAbsolutePath() + ". Now file will be skipped, another try will be on the next import iteration", e);
            }
        }
    }
}
