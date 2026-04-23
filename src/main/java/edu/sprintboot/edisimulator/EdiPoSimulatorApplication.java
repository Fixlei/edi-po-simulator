package edu.sprintboot.edisimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the EDI Purchase Order Simulator.
 *
 * This application simulates a medical supply chain automation system by:
 * 1. Monitoring inventory levels within the database.
 * 2. Automatically generating ANSI X12 850 purchase order files when inventory falls below a defined threshold.
 * 3. Writing the EDI files to an output directory (simulating transmission to suppliers).
 *
 * @EnableScheduling enables support for scheduled tasks using the @Scheduled annotation.
 */
@SpringBootApplication
@EnableScheduling
public class EdiPoSimulatorApplication {

  public static void main(String[] args) {
    SpringApplication.run(EdiPoSimulatorApplication.class, args);
  }
}