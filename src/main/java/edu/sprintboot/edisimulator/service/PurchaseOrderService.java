package edu.sprintboot.edisimulator.service;

import edu.sprintboot.edisimulator.model.Product;
import edu.sprintboot.edisimulator.model.PurchaseOrder;
import edu.sprintboot.edisimulator.repository.ProductRepository;
import edu.sprintboot.edisimulator.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * core logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

  private final EdiX12Generator ediX12Generator;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final ProductRepository productRepository;

  @Value("${edi.output.directory:./output}")
  private String outputDirectory;

  private final AtomicInteger controlNumberCounter = new AtomicInteger(1);

  @Transactional
  public PurchaseOrder createPurchaseOrder(Product product) {
    String poNumber = generatePoNumber();
    String controlNumber = String.format("%09d", controlNumberCounter.getAndIncrement());


    String ediContent = ediX12Generator.generate850(product, poNumber, controlNumber);

    String filePath = writeEdiFile(poNumber, ediContent);


    BigDecimal totalAmount = product.getUnitPrice()
        .multiply(BigDecimal.valueOf(product.getReorderQuantity()));

    PurchaseOrder po = new PurchaseOrder();
    po.setPoNumber(poNumber);
    po.setProductId(product.getProductId());
    po.setQuantityOrdered(product.getReorderQuantity());
    po.setTotalAmount(totalAmount);
    po.setGeneratedAt(LocalDateTime.now());
    po.setEdiFilePath(filePath);
    po.setStatus(filePath != null ? PurchaseOrder.Status.GENERATED : PurchaseOrder.Status.FAILED);

    PurchaseOrder saved = purchaseOrderRepository.save(po);

    product.setCurrentStock(product.getCurrentStock() + product.getReorderQuantity());
    productRepository.save(product);

    log.info("create order {} : {} x {} (Total ${}) -> {}",
        poNumber, product.getReorderQuantity(), product.getProductName(),
        totalAmount, filePath);

    return saved;
  }

  private String writeEdiFile(String poNumber, String ediContent) {
    try {
      Path dir = Paths.get(outputDirectory);
      Files.createDirectories(dir);
      Path file = dir.resolve(poNumber + ".edi");
      Files.writeString(file, ediContent);
      return file.toAbsolutePath().toString();
    } catch (IOException e) {
      log.error("write EDI file failed PO {}: {}", poNumber, e.getMessage());
      return null;
    }
  }

  private String generatePoNumber() {
    String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    int seq = controlNumberCounter.get();
    return String.format("PO%s-%04d", datePart, seq);
  }
}