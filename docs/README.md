# ADSS Assignment 2 - SuperLi Supermarket Management System

## 👥 Team Members & Contribution
* **Hadas Bakalzuk** - ID: 325213080
* **Rotem Onn** - ID: 322991910
* **Noa Eliyahou** - ID: 318939212
* **Coral Goldman** - ID: 322251976

---

##  Modeling Tools & Environment
* **System Modeling & Diagrams:** All architecture models, diagrams, and structural trees were built using **draw.io**.
* **Deliverable Formats:** In accordance with the submission guidelines, all diagrams have been exported to **PDF/PNG** for viewing, and their matching editable **XML** files are included in the corresponding project directories to facilitate direct verification.

---

##  Project Description
This project is an enterprise-grade Supermarket Management System developed as part of the ADSS course. The system provides a comprehensive, modular, and layered console-based solution to manage a supermarket's daily logistics, supply chain, and human resources.

The system seamlessly integrates two core operational domains:
1. **Employee & Shift Management Module (Workers):** Handles HR operations, weekly shifting schedules, staffing constraint validations, dynamic role definitions, and employee availability submissions.
2. **Inventory Management Module:** Manages all supermarket products and their stock. It divides stock between the warehouse and the store shelves. It calculates the best discounts for customers and logs damaged or expired items. When stock is too low or a delivery is planned, it automatically creates order forms for the best supplier.
The codebase is written entirely in **Java**, leverages an **SQLite database** for robust data persistence between sessions, and strictly adheres to Object-Oriented Programming (OOP) design patterns, separation of concerns, and clean architectural principles.

---

## Project Structure & Deliverables Matrix
To simplify project verification and ensure no required documents are missing, all analysis, behavioral contracts, and system code have been structured into distinct, self-contained directories:

### 1. Analysis & Modeling Directory (`/docs`)
* **`/use-cases`** - Contains the overall Use-Case diagram (`use-case-diagram.pdf` & `.xml`) along with detailed functional specifications for our chosen scenario pathways (`use-case-d.pdf` / `main.xml` / `alt.xml`).
* **`/contracts`** - Houses formal business behavior specifications written using camelCase function conventions (e.g., `contract-actionName.pdf`).
* **`/sequence-diagrams`** - Stores the corresponding behavioral realizations mapped via sequence layouts (`seq-diagram-actionName.pdf` & `.xml`) to track active system event sequences.
* **`class-diagram.pdf` & `class-diagram.xml`** - The fully updated domain class diagram detailing system relationships, fields, and operations.
* **`requirements.pdf`** - The finalized, updated non-functional and functional project requirements ledger[cite: 27].

### 2. Implementation Codebase (`/dev/src`)
* **`Domain/`** - Core business objects (`Product`, `Category`, `DefectiveItem`), state logic, and baseline validation checks.
* **`DTO/`** - Data Transfer Objects built as modern Java `record` types ensuring lightweight, immutable data transmission across system boundaries.
* **`Service/`** - The application facade layer (`InventoryService`) handling operational flow execution, persistence repository routing, and supplier automation hooks.
* **`Presentation/`** - Interactive 13-option management console menu (`InventoryMenu`) and automated persistence table seed scripts (`DataInitializer`).
* **`UnitTests/`** - Comprehensive testing framework splitting operational quality assurance into `InventoryDomainTests` (Unit logic) and `InventoryDBTests` (Integration/SQLite connectivity).
* **`WorkersModule/`** - Comprehensive structural layer managing employee and shift allocation states.

---

## 🚀 How to Run & User Guide

### 1. Initial Launch and Data Sources
Run the `Main` class from your preferred IDE. At boot, the system instantiates a database connection hook via `DatabaseManager` and evaluates the storage landscape. A startup prompt presents three data seeding behaviors:
* `1` - **Default DB:** Seeds the persistent storage with sample entities (e.g., Tnuva Milk 3%, Cottage Cheese, Osem Bamba) mapping various hierarchical categories, if the system is blank.
* `2` - **Custom DB:** Seeds a minimal testing target product environment (e.g., FreshCo test item).
* `3` - **Start Empty / Load Existing:** Skips seeding entirely and boots directly using the active state data saved inside the persistent `superli_inventory.db` file.

*A graceful shutdown hook automatically triggers at application exit to safely close the SQLite database connection cleanly.*

---

### 👥 Workers Module (HR & Shifts)
The application roots an access portal dividing users into roles:
1. **HR Manager Mode** (Password protected via default password: `1234`)
2. **Employee Mode** (Authenticated via Employee ID)

#### **HR Manager Actions:**
* **Workforce Administration:** Register new employees, view comprehensive team grids, and toggle employment status flags (Active/Inactive).
* **Role Catalogs:** Dynamically append, customize, or track operational job descriptions.
* **Shift Logistics:** Generate future weekly shift timelines, enforce custom staffing counts per shift, select certified Shift Managers, block shifts, and extract historical shift logs.

> ⚠️ **Automated Shift Assignment Validation Rules:**
> During scheduling assignments, the system automatically checks that the target employee exists, is active, carries the explicit role certification, has submitted availability, and that the chosen shift is open (not blocked). For **Drivers**, it rigorously ensures that their driving license classification matches the required truck type for delivery.

#### **Employee Actions:**
* File future shift availability matrices before the HR-defined locking deadline.
* View the finalized and published weekly calendar grid.

---

### 📦 Inventory Module (Menu Guide & Domain Rules)
Selecting the Inventory portal boots the `InventoryMenu` console context loop offering a broad array of administrative capabilities:

#### **Core Features:**
* **Strict Category Trees (`Category`):** Multi-level category mappings strictly follow a tree hierarchy path (`Main Category -> Sub-Category -> Sub-Sub-Category`). This prevents conflicting category assignments and structural indexing contradictions.
* **Smart Promotion Engine (`Discount`):** Category discounts automatically cascade downward to affect all children sub-categories. If multiple discounts overlap on a single SKU, the calculation engine dynamically resolves the conflicts, matching the item against all valid parameters to output the **optimal (lowest) consumer price** relative to the original base price.
* **Stock & Replenishment Thresholds:** Products monitor an active `min_stock` threshold. If the combined total stock (Storage + Shelf) drops to or below this minimum limit, immediate console warnings are generated, and a replenishment entry records the required units to reach `targetQuantity`.
* **Warehouse Refill Flow (Flow 3):** Allows administrators to log the physical replenishment movement of goods from back storage to front-end sales shelves (`transferToShelf()`), ensuring quantities balance smoothly and safely short-circuiting transactions if warehouse volume drops too low.
* **Defect Quantities Validation (`DefectiveItem`):** When declaring damaged goods, the system performs a sanity check against actual stock files to confirm that the reported quantity realistically exists at that location (Warehouse vs. Shelf) before approving the loss.

---

## 🔌 Cross-Module Integration Bridge (Inventory ↔ Suppliers)
The system implements a rigid **Low Coupling** architecture hook via `IntegrationDummyFunctions` to communicate safely with the Suppliers Module without violating layer boundaries:

* **Automated Shortage Orders:** When an administrator updates a product's stock levels or logs a defective item, if the overall count falls below its `min_stock` threshold, an automated trigger passes through `handleShortageOrder()`. The `SuppliersServiceDummy` instantly intercepts this event, looks up the cheapest vendor using historical cost logs, and renders a formal **Supplier Order Form**.
* **Periodic Delivery Check:** Driven by `checkAndProcessPeriodicOrders(tomorrowDay)`, this service allows managers to run checks for upcoming deliveries. It evaluates items scheduled for tomorrow, calculates the missing quantity needed to surpass minimum targets, and sends purchase requests to the best supplier.

---

## 🧪 Testing Suite & Quality Assurance
The system features a robust automated testing framework containing comprehensive unit and integration test coverage powered by **JUnit**.

### 1. Domain Unit Tests (`InventoryDomainTests`)
Focuses on verifying isolated business logic, memory rules, and constraint compliance without hitting external files (testing overlapping discounts, category cascading, and boundary checks).

### 2. DB Integration Tests (`InventoryDBTests`)
Verifies end-to-end operational pipelines, persistent SQLite side-effects, transfer flows, and active component bridges.

*Note: The integration tests use isolated test-bed ranges (SKUs 901–910) and an automated `tearDown()` script to clean up table fields after execution, ensuring tests leave no permanent dirt in your working DB file.*