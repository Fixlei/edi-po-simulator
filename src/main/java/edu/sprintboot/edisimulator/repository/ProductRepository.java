package edu.sprintboot.edisimulator.repository;

import edu.sprintboot.edisimulator.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product entity data access layer.
 *
 * Spring Data JPA impl class created on runtime.
 * query method declaration only.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

  /**
   * Retrieves all products with inventory levels falling below their reorder threshold.
   * This query serves as the core driver for the automated replenishment logic.
   */
  @Query("SELECT p FROM Product p WHERE p.currentStock < p.reorderThreshold")
  List<Product> findProductsBelowThreshold();
}