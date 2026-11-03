# Bakery Order Management System — Phase 3 (GUI)

CEN 3024C · Kayla Ewing

A Java Swing application that manages custom cake orders for a small bakery.
Phase 1 built the logic and input validation, Phase 2 added JUnit 5 unit tests, and Phase 3 adds a graphical user interface. There is no database yet.

## Features (all in one window)

| Window area | What the user can do |
|---|---|
| Header | See the bakery logo and title; **Exit** (asks to confirm; closing the window with X also asks) |
| File bar | Type a `.txt` file name or path, or click **Browse…**, then **Load File**. Blank, missing, folder and non-.txt names are reported. Bad lines are skipped and listed. |
| Order form (left) | Fill in all 9 fields and click **Add Order**; click a row in the table to load it, change any field and click **Update Order**; type or select an Order ID and click **Delete Order** (asks to confirm); **Clear Form** starts over |
| Order table | Displays every order, sorted by ID (click a column header to sort). **Search** by ID, customer or cake type; **Show All** clears the search. Picked-up orders are shown in gray italics. |
| Report bar | **Custom action:** enter a pickup date and click **Generate Report** to see how many cakes of each type to bake and the expected revenue (subtotal + 7% tax) |
| Status bar | Shows the result of every action in green (success) or red (problem) |

Bad input is never accepted: problems are listed in a message window, the fields are outlined in red, and nothing is saved until every field is valid.

## The Order object

| Attribute | Type | Rule |
|---|---|---|
| orderId | int | 1000–9999, unique |
| customerName | String | 2–30 letters, spaces, `'` `-` `.` |
| phoneNumber | String | exactly 10 digits, stored as 407-555-0142 |
| cakeType | String | Chocolate, Vanilla, Red Velvet or Carrot |
| quantity | int | 1–50 |
| unitPrice | double | more than $0, at most $1000, max 2 decimals |
| pickupDate | LocalDate | today up to 2 years ahead |
| pickedUp | boolean | true / false |
| specialInstructions | String | optional, up to 100 characters |

## Text file format

One order per line, fields separated by commas. Lines starting with `#` are ignored.

```
orderId,customerName,phoneNumber,cakeType,quantity,unitPrice,pickupDate,pickedUp,specialInstructions
1001,Mia Chen,407-555-0101,Chocolate,2,25.00,2027-02-13,false,Write Happy Valentine's Day in red icing
```

- `orders.txt` has 25 sample orders.
- `orders_with_errors.txt` has 2 good orders and 8 bad lines, for demonstrating validation.

## How to run

**In IntelliJ:** choose **BakeryApp** in the run menu at the top right and click Run. In the window, type `orders.txt` in the file box and click **Load File**.

**From the JAR:** double-click `BakeryOrderDMS.jar`, or open a terminal in its folder and run:

```
java -jar BakeryOrderDMS.jar
```

Requires Java 11 or newer. If `orders.txt` is not in the same folder, enter its full path.

## Unit tests (Phase 2)

The `test` folder holds 70 JUnit 5 tests. Each area has positive tests (valid input works) and negative tests (bad input is rejected and nothing changes).

| Test class | What it proves |
|---|---|
| `FileOpenTest` | A text file opens and its orders load; missing, blank, non-.txt, folder, empty and badly formatted files are handled |
| `AddOrderTest` | A valid order is added and found with the same data; every kind of bad field is rejected |
| `RemoveOrderTest` | An order is removed and no longer exists; removing a missing ID returns false |
| `UpdateOrderTest` | All 9 attributes can be updated from user-typed text; bad text for each attribute is rejected |
| `DailyReportTest` | The custom action counts cakes per type and calculates $181.00 + $12.67 tax = $193.67 |
| `InputValidationTest` | Typed numbers, prices, dates and yes/no answers are read safely |

`TestHelper` builds an `OrderManager` whose clock is fixed at 2026-11-01, so the date rules give the same results on any day.

**Run them in IntelliJ:** choose **All Unit Tests** in the run menu at the top right and click Run, or right-click the `test` folder › **Run 'All Tests'**. The first time, IntelliJ downloads JUnit 5.10.3 automatically.

## Classes

- `BakeryApp` – starts the program and opens the window; holds `main`
- `BakeryOrderWindow` – the GUI: form, table, file bar, report bar, status bar (presentation layer)
- `OrderTableModel` – connects the list of orders to the table on screen
- `Theme` – colors, fonts and styled buttons used throughout the GUI
- `CakeLogo` – draws the cake logo in code (header and window icon)
- `OrderManager` – CRUD operations and the daily report (business logic layer)
- `OrderValidator` – every data rule in one place
- `OrderFileLoader` – reads and parses text files (data layer)
- `Order` – the data object
- `DailyReport` – result of the custom action
