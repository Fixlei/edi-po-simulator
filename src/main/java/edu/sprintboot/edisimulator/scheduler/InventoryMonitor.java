package edu.sprintboot.edisimulator.scheduler;

import edu.sprintboot.edisimulator.model.Product;
import edu.sprintboot.edisimulator.repository.ProductRepository;
import edu.sprintboot.edisimulator.service.PurchaseOrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that periodically scans inventory and triggers purchase orders.
 * <p>
 * In a real hospital system, this might run every hour or every few minutes.
 * For the demo we run every 30 seconds so you can watch it work.
 * <p>
 * This is the "automation" in "automated EDI" — no human clicks a button,
 * the system watches stock levels and reacts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryMonitor {

  private final ProductRepository productRepository;
  private final PurchaseOrderService purchaseOrderService;

  /**
   * Runs every 30 seconds after an initial 5-second delay.
   * Cron alternative: @Scheduled(cron = "0 0 * * * *") for hourly
   */
  @Scheduled(fixedDelay = 30_000, initialDelay = 5_000)
  public void checkInventoryAndReorder() {
    log.info("=== Running inventory check ===");

    List<Product> lowStockProducts = productRepository.findProductsBelowThreshold();

    if (lowStockProducts.isEmpty()) {
      log.info("All products have sufficient stock. No action needed.");
      return;
    }

    log.info("Found {} product(s) below reorder threshold", lowStockProducts.size());

    for (Product product : lowStockProducts) {
      log.info("  - {} ({}): stock={}, threshold={}",
          product.getProductName(), product.getProductId(),
          product.getCurrentStock(), product.getReorderThreshold());

      try {
        purchaseOrderService.createPurchaseOrder(product);
      } catch (Exception e) {
        // Robust error handling: one failed PO should not stop others
        log.error("Failed to generate PO for product {}: {}",
            product.getProductId(), e.getMessage(), e);
      }
    }

    log.info("=== Inventory check complete ===");
  }
}