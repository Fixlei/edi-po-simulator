package edu.sprintboot.edisimulator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Audit log entity for purchase orders.
 *
 * Reflects a core principle of EDI engineering: every transaction must be recorded
 * to ensure full traceability (audit trail).
 * In real-world enterprise systems, this is a strict compliance requirement
 * (e.g., HIPAA standards for the healthcare industry).
 */
@Entity
@Table(name = "PURCHASE_ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String poNumber;           // order number: "PO20260423-0001"

  private String productId;

  private Integer quantityOrdered;

  private BigDecimal totalAmount;

  private LocalDateTime generatedAt;

  private String ediFilePath;        // generate X12 850

  @Enumerated(EnumType.STRING)
  private Status status;

  public enum Status {
    GENERATED,   // EDI file created
    SENT,        // file transmission
    FAILED       // generating or passing failed
  }
}