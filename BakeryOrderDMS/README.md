# Bakery Order Management System — Phase 4 (MySQL Database)

CEN 3024C · Kayla Ewing

A Java Swing application that manages custom cake orders for a small bakery.
Phase 1 built the logic and input validation, Phase 2 added JUnit 5 unit tests, Phase 3 added the GUI, and **Phase 4 stores every order in a MySQL database** instead of objects loaded from a text file.

Every Create, Read, Update and Delete in the window runs SQL against the database right away. You can see each change from the MySQL command line or MySQL Workbench as soon as it happens. Click **Refresh** in the program to see changes made outside it.

## Setup (do this once)

1. **Install MySQL 8** (MySQL Community Server, plus MySQL Workbench if you like) and remember the username and password you choose, usually `root`.
2. **Create the sample database** using the script `bakery_dms.sql`. It creates the database `bakery_dms` with one table, `orders`, holding **25 sample orders**. Use either of these:
   - **MySQL Workbench:** connect to your server, choose **File › Open SQL Script…**, pick `bakery_dms.sql`, then click the lightning-bolt ⚡ button to run it.
   - **Command line:** `mysql -u root -p < bakery_dms.sql`
   Running the script again resets the sample data.
3. **Open the project in IntelliJ** (File › Open › the `BakeryOrderDMS` folder). The first time, IntelliJ downloads two libraries from Maven Central automatically: the MySQL JDBC driver (`com.mysql:mysql-connector-j:8.0.33`) and JUnit 5.10.3. If it asks, click **Download** or **Load**.

## How to run

**In IntelliJ:** choose **BakeryApp** in the run menu at the top right and click Run.

**From the JAR:** double-click `BakeryOrderDMS.jar`, or open a terminal in its folder and run:

```
java -jar BakeryOrderDMS.jar
```

The JAR already contains the MySQL driver. It needs Java 11 or newer.

**Connecting:** at the top of the window, enter your MySQL details and click **Connect** (or press Enter):

| Box | What to type |
|---|---|
| Server | `localhost` if MySQL is on this computer, otherwise its address |
| Port | `3306` unless you changed it when installing (blank also means 3306) |
| Database | `bakery_dms` (the database the script creates) |
| Username / Password | your own MySQL login |

Nothing about your server or login is written in the code. The program builds the connection string from what you type:
`"jdbc:mysql://" + server + ":" + port + "/" + database + options`.
It never saves your password.

Problems are explained in plain English and never crash the program. For example: a blank box, an invalid port, a wrong username or password, an unknown database (run the script), MySQL not running, an unknown server name, or a lost connection.

## Features (all in one window)

| Window area | What the user can do |
|---|---|
| Header | Connection status (**Connected** / **Not connected**); **Exit** (asks to confirm; closing the window with X also asks) |
| Database bar | Server, Port, Database, Username, Password and **Connect** |
| Order form (left) | **Create:** fill in all 9 fields and click **Add Order** (INSERT). **Update:** click a row in the table, change any field (even the Order ID) and click **Update Order** (UPDATE). **Delete:** select an order or type its ID and click **Delete Order**; it asks you to confirm first (DELETE). **Clear Form** starts over. |
| Order table | **Read / Display:** every order in the database (SELECT), sorted by ID. **Search** by ID, customer or cake type; **Show All**; **Refresh** reloads from the database. Picked-up orders are shown in gray italics. |
| Report bar | **Custom action (Daily Production Report):** enter a pickup date and click **Generate Report** to see how many cakes of each type to bake and the expected revenue (subtotal + 7% tax). It counts only orders that are not picked up. |
| Status bar | Shows the result of every action in green (success) or red (problem) |

Bad input is never sent to the database. Problems are listed in a message window, the fields are outlined in red, and nothing is saved until every field is valid. The table also has its own CHECK constraints, so the database rejects invalid data even if it comes from somewhere else.

## The database

`bakery_dms.orders`, one row per order:

| Column | MySQL type | Rule (checked by the program, and by the database where noted) |
|---|---|---|
| order_id | INT, PRIMARY KEY | 1000–9999, unique (CHECK + PK) |
| customer_name | VARCHAR(30) | 2–30 letters, spaces, `'` `-` `.` |
| phone_number | CHAR(12) | exactly 10 digits, stored as 407-555-0142 |
| cake_type | VARCHAR(20) | Chocolate, Vanilla, Red Velvet or Carrot (CHECK) |
| quantity | INT | 1–50 (CHECK) |
| unit_price | DECIMAL(7,2) | more than $0, at most $1000 (CHECK) |
| pickup_date | DATE | today up to 2 years ahead |
| is_picked_up | BOOLEAN | true / false |
| special_instructions | VARCHAR(100) | optional, up to 100 characters |

Every query uses a `PreparedStatement` with `?` placeholders, so text typed by the user can never change the SQL.

## Unit tests

The `test` folder holds **67 JUnit 5 tests**. Each area has positive tests (valid input works) and negative tests (bad input is rejected and nothing changes). The tests do **not** need MySQL. `InMemoryOrderRepository` stands in for the database, so they prove the same business rules on any computer.

| Test class | What it proves |
|---|---|
| `DatabaseConnectorTest` | Valid connection details are accepted and the connection string is built from them; blank or invalid server, port, database and username are rejected; failures give readable messages |
| `AddOrderTest` | A valid order is added and found with the same data; every kind of bad field is rejected |
| `RemoveOrderTest` | An order is removed and no longer exists; removing a missing ID returns false |
| `UpdateOrderTest` | All 9 attributes can be updated from user-typed text; bad text for each attribute is rejected |
| `DailyReportTest` | The custom action counts cakes per type and calculates $181.00 + $12.67 tax = $193.67 |
| `InputValidationTest` | Typed numbers, prices, dates and yes/no answers are read safely |

**Run them in IntelliJ:** choose **All Unit Tests** in the run menu at the top right and click Run, or right-click the `test` folder › **Run 'All Tests'**.

## Classes

**Presentation layer**
- `BakeryApp`: starts the program and opens the window; holds `main`
- `BakeryOrderWindow`: the GUI, with the database bar, form, table, report bar and status bar
- `OrderTableModel`: connects the list of orders to the table on screen
- `Theme`: colors, fonts and styled components
- `CakeLogo`: draws the cake logo in code

**Business logic layer**
- `OrderManager`: CRUD operations and the daily report, through `OrderRepository`
- `OrderValidator`: every data rule in one place
- `Order`: the data object
- `DailyReport`: the result of the custom action

**Data layer (new in Phase 4)**
- `OrderRepository`: the interface for storing orders (planned in Module 5)
- `MySqlOrderRepository`: runs the SQL for every operation against the `orders` table
- `DatabaseConnector`: checks the user's connection details, builds the connection string and connects
- `DataAccessException`: carries a plain-English database error to the GUI

`OrderFileLoader` and the text files were removed. The user no longer supplies a text file.
