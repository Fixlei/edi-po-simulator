# EDI Purchase Order Simulator

> A backend automation prototype that simulates a hospital's inventory system auto-generating **ANSI X12 850** purchase orders for a medical supplier when stock falls below reorder thresholds.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green)
![Build](https://img.shields.io/badge/build-Maven-blue)

---

## 📌 Project Motivation

Large medical device suppliers (e.g. Philips Healthcare, GE Healthcare, Siemens) rely on **Electronic Data Interchange (EDI)** to automate B2B ordering with hospitals. Instead of a procurement clerk manually filling out forms, the hospital's ERP system directly speaks to the supplier's order system using standardized message formats like **ANSI X12 850 (Purchase Order)** and **X12 855 (PO Acknowledgement)**.

This project is a simplified prototype of the **buyer side** of such an integration. It demonstrates the core engineering concepts behind EDI-based supply chain automation:

- Scheduled inventory monitoring
- Event-driven purchase order generation
- Hand-rolled X12 850 document formatting
- Persistent audit logging of every generated transaction

> This is a learning project, not production software. A real EDI pipeline would use AS2 transport, digital certificates, and a commercial translator (Sterling B2B Integrator, Cleo, IBM Sterling). The goal here is to show understanding of the architecture and data format.

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                 EDI PO Simulator (Spring Boot)              │
│                                                             │
│   ┌──────────────────┐       ┌────────────────────────┐     │
│   │ InventoryMonitor │──────▶│  PurchaseOrderService  │     │
│   │  @Scheduled 30s  │       └──────────┬─────────────┘     │
│   └──────────────────┘                  │                   │
│           │                             ▼                   │
│           │                  ┌────────────────────┐         │
│           │                  │  EdiX12Generator   │         │
│           │                  │  (builds 850 doc)  │         │
│           │                  └──────────┬─────────┘         │
│           ▼                             ▼                   │
│   ┌────────────────┐          ┌─────────────────────┐       │
│   │ H2 Database    │          │  ./output/*.edi     │       │
│   │ PRODUCTS       │          │  (simulated         │       │
│   │ PURCHASE_ORDERS│          │   transmission)     │       │
│   └────────────────┘          └─────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

**Flow:**

1. The scheduler wakes up every 30 seconds and queries products where `currentStock < reorderThreshold`.
2. For each low-stock product, `PurchaseOrderService` assigns a PO number, calls the X12 generator, writes the resulting `.edi` file to disk, and records the transaction in the audit log table.
3. Stock is incremented by the reorder quantity to reflect the pending order (in real life this would happen after receiving an X12 855 acknowledgement).

---

## 🛠️ Tech Stack

| Layer              | Technology                          |
| ------------------ | ----------------------------------- |
| Language           | Java 17                             |
| Framework          | Spring Boot 3.2                     |
| Persistence        | Spring Data JPA + H2 (in-memory)    |
| Scheduling         | Spring `@Scheduled`                 |
| Build              | Maven                               |
| Dev utilities      | Lombok, H2 web console              |

---

## 📂 Project Structure

```
edi-po-simulator/
├── pom.xml
├── README.md
├── output/                              # generated .edi files land here
└── src/main/
    ├── java/com/example/edisimulator/
    │   ├── EdiPoSimulatorApplication.java    # main entry point
    │   ├── config/
    │   │   └── DataInitializer.java          # seeds sample products on startup
    │   ├── model/
    │   │   ├── Product.java                  # inventory item entity
    │   │   └── PurchaseOrder.java            # audit log entity
    │   ├── repository/
    │   │   ├── ProductRepository.java
    │   │   └── PurchaseOrderRepository.java
    │   ├── scheduler/
    │   │   └── InventoryMonitor.java         # @Scheduled inventory scan
    │   └── service/
    │       ├── EdiX12Generator.java          # builds X12 850 documents
    │       ├── PurchaseOrderService.java     # orchestrates PO generation
    │       └── InventoryController.java      # REST endpoints
    └── resources/
        └── application.properties
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or later → verify with `java -version`
- **IntelliJ IDEA** (Community edition is free and sufficient)

### Run

```bash
# Clone and enter the project
git clone https://github.com/<YOUR-USERNAME>/edi-po-simulator.git
cd edi-po-simulator

# Start the application
mvn spring-boot:run
```

Within ~5 seconds you'll see log output like:

```
14:02:11.432 INFO  [DataInitializer] - Seeded 4 products into the database
14:02:16.102 INFO  [InventoryMonitor] - === Running inventory check ===
14:02:16.118 INFO  [InventoryMonitor] - Found 3 product(s) below reorder threshold
14:02:16.245 INFO  [PurchaseOrderService] - Generated PO PO20260423-0001 for 50 x MRI Contrast Agent 10ml (total $2275.00) -> /.../output/PO20260423-0001.edi
```

Check the `output/` directory — you'll find `.edi` files containing real X12 850 content.

---

## 🔍 Exploring the Running System

### REST endpoints

| Method | Endpoint                                        | Purpose                         |
| ------ | ----------------------------------------------- | ------------------------------- |
| GET    | `/api/products`                                 | List current inventory          |
| GET    | `/api/purchase-orders`                          | List all generated POs          |
| POST   | `/api/products/{id}/consume?qty=N`              | Simulate stock consumption      |

Example: push a product below threshold to watch the scheduler auto-react:

```bash
curl -X POST "http://localhost:8080/api/products/PHIL-VENT-100/consume?qty=60"
```

### H2 database console

Navigate to **http://localhost:8080/h2-console** in your browser:

- JDBC URL: `jdbc:h2:mem:edidb`
- User: `sa`
- Password: *(leave empty)*

Run `SELECT * FROM PURCHASE_ORDERS;` to see the audit trail.

**Note** on Persistence: This project uses an **in-memory** H2 database for demonstration purposes. The console is accessible locally at http://localhost:8080/h2-console only when the application is running. For production, these credentials would be managed via environment variables.

---

## 📄 Sample Generated X12 850

```
ISA*00*          *00*          *ZZ*HOSPITAL123    *ZZ*PHILIPSNA      *260423*1402*U*00401*000000001*0*T*>~
GS*PO*HOSPITAL123*PHILIPSNA*20260423*1402*1*X*004010~
ST*850*0001~
BEG*00*NE*PO20260423-0001**20260423~
REF*DP*HOSPITAL-PROCUREMENT~
PER*BD*Procurement Department*TE*5551234567~
N1*BY*GENERAL HOSPITAL*92*HOSP-001~
N1*SE*PHILIPS HEALTHCARE NA*92*PHILIPS-NA~
PO1*1*50*EA*45.50*PE*VP*PHIL-MRI-001~
PID*F****MRI Contrast Agent 10ml~
CTT*1~
SE*10*0001~
GE*1*1~
IEA*1*000000001~
```

**Segment decoder:**

- `ISA` — Interchange envelope (who/when/what version)
- `GS` — Functional group wrapping the 850
- `ST` / `SE` — Transaction Set boundaries
- `BEG` — PO number, type, and date
- `N1*BY` / `N1*SE` — Buyer and Seller identification
- `PO1` — Line item (qty, unit, price, product ID)
- `CTT` — Totals

---

## 🧠 Concepts Demonstrated

This project was built to practice skills listed in supply-chain and B2B-integration engineering roles:

- **Standards-based messaging** — Hand-built X12 segments rather than relying on a library, to prove understanding of the format
- **Scheduled job design** — `@Scheduled` with proper error isolation so one failed PO doesn't halt the rest
- **Transactional consistency** — `@Transactional` around DB write + audit logging
- **Audit traceability** — Every generated PO is persisted with timestamp, file path, and status — the same pattern required for HIPAA-compliant systems
- **Separation of concerns** — Generator, service, scheduler, and controller each have one responsibility

---

## 🔭 Roadmap (if extended further)

- [ ] Build a "supplier-side" service that receives the 850 and returns an **X12 855** acknowledgement
- [ ] Replace file output with an HTTPS `POST` to simulate **AS2** transmission
- [ ] Add a retry queue with exponential backoff for failed transmissions
- [ ] Swap H2 for PostgreSQL + Flyway migrations
- [ ] Dockerize and deploy to a cloud runtime

---

## 📚 References

- [ANSI X12 850 specification overview](https://x12.org/examples/x12-edi-purchase-order-850)
- [Healthcare EDI basics (CMS)](https://www.cms.gov/medicare/coding-billing/electronic-billing-edi-transactions)
- [Spring Boot reference documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)

---

## 👤 Author & Tool
Minghua Lei & Claude
