package edu.sprintboot.edisimulator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * product entity
 *
 * - currentStock: current amount
 * - reorderThreshold: lower than the value will trigger reorder
 * - reorderQuantity: Qty of each time reorder
 */
@Entity
@Table(name = "PRODUCTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  @Id
  private String productId;          //"PHIL-MRI-001"

  private String productName;        // "MRI dose 10ml"

  private String supplierId;         //  "PHILIPS-NA"

  private Integer currentStock;

  private Integer reorderThreshold;

  private Integer reorderQuantity;   // each time order Qty

  private BigDecimal unitPrice;
}