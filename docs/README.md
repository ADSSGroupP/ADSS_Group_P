# ADSS Assignment 2

Name: Hadas Bakalzuk  
ID: 325213080  

Name: Rotem Onn  
ID: 322991910  

Name: Noa Eliyahou  
ID: 318939212  

Name: Coral Goldman  
ID: 322251976  

## Project Description

The system models an integrated enterprise supermarket management platform, split into two primary operational domains:

* **Inventory Management Subsystem:** Governs product logging, dual-location tracking (shelves vs. backroom storage), promotional discount calculations, and automatic supplier order forms creation upon low stock thresholds or scheduled delivery dates.
* **Employee & HR Management Subsystem:** Handles HR workflows, weekly shift templates, driver license checks, worker availability exceptions and assigning employees to shifts.

## Modeling Tool

The system models and diagrams were created using draw.io.

## Project Structure

* `docs/` — Stores all system modeling files, requirements, functional specs, and architectural diagrams.
* `dev/` — Contains the Java source packages distributed across decoupled presentation, service, domain, and data access layers.
* `release/` — Contains the standalone compiled, executable JAR artifact containing the entire application suite.
* `README.md` — Contains general information about the project, the tools used, and instructions for running it.

### 1. Analysis & Modeling Directory (`/docs`)
* **`/use-cases`** — Contains the overall Use-Case diagram along with detailed functional specifications for the chosen scenario pathways.
* **`/contracts`** — Houses formal business behavior specifications and operational invariants.
* **`/sequence-diagrams`** — Stores behavioral realizations mapped via sequence layouts to track active object communication flows.
* **`class-diagram`** — The fully updated system domain class diagram detailing relationships, fields, and method headers.
* **`requirements.pdf`** — The finalized functional and non-functional engineering requirement specifications ledger.

### 2. Implementation Codebase (`/dev`)

#### Inventory Module Components
* **`Domain/`** — Core business objects (`Product`, `Category`, `Discount`, `DefectiveItem`) containing state logic and localized baseline validation checks.
* **`DTO/`** — Data Transfer Objects engineered as modern Java structures ensuring lightweight, immutable transmission across system boundaries.
* **`Service/`** — The application facade layer (`InventoryService`) handling operational orchestration, database transactions, and supplier replenishment hooks.
* **`Presentation/`** — Interactive management console menu (`InventoryMenu`) and automated persistence table seed scripts.

#### Employees Module Components
* **`DomainLayer/`** — Rich business objects (`Employee`, `Driver`, `Constraint`, `Shift`, `StaffingRequirement`, `ShiftAssignment`) paired with `Role` enums and the `HRRepositoryImpl` mapping adapter.
* **`DTO/`** — Data Transfer Objects (`EmployeeDTO`, `ConstraintDTO`, `ShiftDTO`) utilized for flat data movement without business logic.
* **`ServiceLayer/`** — Contains `EmployeeService` (manages registration, archives, and deadlines) and `ShiftService` (validates schedules, enforces role quotas, and manages over-time limits).
* **`PresentationLayer/`** — Houses the the `UserInterface` command-line CLI layout.
* **`DataAccessLayer/`** — Contains the centralized `Database` broker alongside native SQL preparation mappers (`JdbcEmployeeDAO`, `JdbcConstraintDAO`, `JdbcShiftDAO`).

---

## Database Schema & Relational Tables (SQLite)

The application utilizes local relational database targets powered by embedded SQLite engines to handle data persistence seamlessly between runtime sessions:

### Employees Module Database (`superlee.db`)
Tracks personnel profiles, rolling calendar availability parameters, and assigned shift matrices:
* **`employees`**: Registers personal profiles, banking details, wage rules, active flags, a comma-separated string mapping certified profiles, and specialized vehicle categories (`license`) for commercial operators.
* **`constraints`**: Maps submitted worker availability exceptions, tracking time dimensions, double-shift flags, and malleability parameters (Flexible vs. Hard constraints).
* **`shifts`**: Records configured execution blocks mapping calendar dates, shift types (`m` for morning, `e` for evening), assigned shift managers, and branch properties.

### Inventory Module Database (`superli_inventory.db`)
Manages structural retail catalogs, multi-level category structures, waste history, and supply chain logistics:
* **`categories`**: Implements a self-referencing hierarchical structure to connect parent categories down to sub and sub-sub categories cleanly without collision risk.
* **`products`**: The core stock register tracking global barcode metrics, descriptive titles, minimum safety quantities, and specific manufacturer mappings.
* **`supplier_costs` / `product_discounts` / `category_discounts`**: Pivot layouts mapping chronological vendor costs and date-restricted promotion windows.
* **`defective_items` / `low_stock_alerts`**: Auditing files logging reported damaged warehouse goods and automated purchase ledger requests.

---

## How to Run

### Unified Application Entry
When you execute the program's `Main` class, a global system gateway menu allows you to choose which administrative subsystem to load:

1. **Inventory Management Subsystem**
2. **Employee & HR Subsystem**
3. **Exit System**

---

### Employees Module:

#### Overview
The Workers & HR subsystem coordinates workforce scheduling, shift tracking, operational role assignments, and weekly worker availability profiles. The architecture accommodates two user pathways: HR Manager Mode and Standard Employee Mode.

Upon booting this module, you must specify an active branch code (`Branch ID`). 

#### Core Subsystem Functions
* **HR Manager Actions (Password: `6789`):**
  * Register new Employees.
  * Assign, update, and manage professional role qualifications (`Role[]`) per employee.
  * Establish weekly constraint submission deadlines and configure shift blocks.
  * Enforce staffing quotas, block specific shifts, and distribute floor role assignments.
  * Audit active schedules, track historical logs, and review rolling over-time hours.
  * Archive staff members by toggling their employment status to inactive ("Firing").
* **Employee Actions (Authenticated via Employee ID):**
  * Submit, edit, or remove weekly calendar availability constraints prior to the deadline.
  * Flag exceptions as Flexible (allows manager override) or Hard (strictly blocks scheduling).
  * Preview finalized weekly branch schedules and assigned operational roles.

#### Shift Assignment Validation Rules
The system evaluates strict domain rules before committing a scheduling assignment:
* The worker must be explicitly certified for the selected operational role.
* The assignment must not conflict with the employee's submitted hard constraints or designated weekly day-off.
* **Supply Chain Alignment:** The module calls `TransportationMock` to check if a delivery is arriving. If true, it enforces having at least one **Storekeeper** assigned, and checks that the assigned **Driver** possesses the required license category.
---

### Inventory Module:

#### Overview
The Inventory subsystem oversees retail SKU registries, real-time stock balances, defective product deprecation, cascading category pricing trees, and automated vendor purchase loops.

#### Subsystem Initialization
Upon booting this module, the console will ask you to choose how to initialize and load the data:
1. **Default DB:** Seeds the persistent storage with a full roster of sample products (e.g., Milk, Cottage, Bamba) across nested categories.
2. **Custom DB:** Seeds a minimal testing target environment with one single test product.
3. **Start Empty:** Skips setup entirely and loads your active database file without adding new sample items.

#### Core Subsystem Functions
* **View & Update Actions (Options 2, 9, 11):**
  * Check detailed product fields, update active stock levels, or physical replenishment.
  * Safely move inventory items from the warehouse to the storefront shelves.
* **Discounts & Category Management (Options 5, 6, 10):**
  * Add new items, create parent/sub-category trees, and configure bulk promotions.
  * Apply custom discount percentages for specific date ranges that automatically cascade downward.
* **Reports & Orders Actions (Options 1, 12, 13):**
  * Print instant shortage alerts, log defective waste items, and track order histories.
  * Run checks for upcoming deliveries and generate formal supplier replenishment forms.

#### Supplier Orders Validation Rules
The system evaluates strict domain rules before committing inventory updates and order pipelines:
* Products monitor a `min_stock` threshold; if total counts drop below it, immediate alerts and pending orders are triggered.
* Automated loops query historical cost catalogs to identify the cheapest vendor before rendering order forms.
* Defect entries perform location sanity checks to confirm the logged waste quantity realistically exists on that location.
* Physical stock arrivals can be marked as received to automatically transition order states and inflate warehouse counts.

---

## Requirements & Libraries

* **Java Development Kit (JDK 8 or higher)**
* **IntelliJ IDEA** (or any Java-compatible IDE environment).
* **SQLite JDBC Driver (by Xerial):** Third-party dependency used for database connectivity and persistence execution (`superlee.db` & `superli_inventory.db`).
* **JUnit Framework:** External testing library used to power and execute the automated unit and integration testing suites.
* **draw.io** (required only for viewing or editing the architectural `.drawio` source diagrams).
