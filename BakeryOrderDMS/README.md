# Bakery Order Management System — Phase 2 (Testing)

CEN 3024C · [Your Name]

A command-line Java application that manages custom cake orders for a small bakery.
Phase 1 built the logic and input validation. Phase 2 adds JUnit 5 unit tests. There is no GUI and no database yet.

## Features

| Menu option | What it does |
|---|---|
| 1. Load orders from a text file | Reads many orders at once from a `.txt` file. Bad lines are skipped and explained. |
| 2. Display all orders | Prints every order in a table, sorted by order ID. |
| 3. Add a new order | Asks for each field and checks it as you type. |
| 4. Remove an order | Removes an order by its ID after you confirm. |
| 5. Update an order | Changes any of the 9 fields of any order. |
| 6. Daily Production Report | **Custom action.** For a pickup date, counts the cakes of each type still to be baked and calculates the expected revenue (subtotal + 7% sales tax). |
| 7. Exit | Ends the program (after confirmation). |

Type `cancel` at any prompt while adding, removing or updating to return to the menu without changes.

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

**In IntelliJ:** open the project folder, then open `src/bakerydms/BakeryApp.java` and click the green Run arrow next to `main`. When asked for a file, type `orders.txt`.

**From the JAR:** open a terminal in the folder that contains the JAR and run:

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

- `BakeryApp` – menu and user input (presentation layer); holds `main`
- `OrderManager` – CRUD operations and the daily report (business logic layer)
- `OrderValidator` – every data rule in one place
- `OrderFileLoader` – reads and parses text files (data layer)
- `Order` – the data object
- `DailyReport` – result of the custom action
- `InputCancelledException`, `InputEndedException` – let the user cancel safely and stop the program crashing if input closes
