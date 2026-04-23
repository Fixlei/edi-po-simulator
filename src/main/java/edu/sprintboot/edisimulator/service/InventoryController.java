package edu.sprintboot.edisimulator.service;

import edu.sprintboot.edisimulator.model.Product;
import edu.sprintboot.edisimulator.model.PurchaseOrder;
import edu.sprintboot.edisimulator.repository.ProductRepository;
import edu.sprintboot.edisimulator.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for inspecting and interacting with the simulator.
 *
 * Useful endpoints once the app is running:
 *   GET  http://localhost:8080/api/products          - list all products
 *   GET  http://localhost:8080/api/purchase-orders   - list generated POs
 *   POST http://localhost:8080/api/products/{id}/consume?qty=5
 *        - simulate consuming stock (to re-trigger reordering)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InventoryController {

  private final ProductRepository productRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;

  @GetMapping("/products")
  public List<Product> listProducts() {
    return productRepository.findAll();
  }

  @GetMapping("/purchase-orders")
  public List<PurchaseOrder> listPurchaseOrders() {
    return purchaseOrderRepository.findAll();
  }

  /**
   * Simulates consumption of inventory — e.g. a department used some supplies.
   * Useful for demos: call this to push a product below threshold and watch
   * the scheduler auto-generate a PO on its next tick.
   */
  @PostMapping("/products/{productId}/consume")
  public Map<String, Object> consumeStock(
      @PathVariable String productId,
      @RequestParam(defaultValue = "1") int qty) {

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

    product.setCurrentStock(Math.max(0, product.getCurrentStock() - qty));
    productRepository.save(product);

    return Map.of(
        "productId", productId,
        "newStock", product.getCurrentStock(),
        "belowThreshold", product.getCurrentStock() < product.getReorderThreshold()
    );
  }
}