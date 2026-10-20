/*
 * Name:   Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026
 * Class:   BakeryApp
 *
 * Purpose: The presentation layer and starting point of the Bakery Order
 *          Management System (Phase 1: command-line version). It shows the
 *          main menu, reads what the user types, checks every value, and
 *          calls OrderManager to do the real work. All output is printed to
 *          the console.
 *
 *          The program can never be crashed by bad input:
 *            - every number, date and yes/no answer is checked and re-asked
 *            - typing "cancel" during add/update/remove returns to the menu
 *            - closing the input stream ends the program politely
 *            - any unexpected error is caught and the menu is shown again
 *          The program ends only when the user chooses Exit.
 *
 *          main() is the only static method in the project. Java requires the
 *          program's entry point to be static; main() simply creates the
 *          objects and calls run(), and all other code is in instance methods.
 */
package bakerydms;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Command-line menu for the Bakery Order Management System.
 */
public class BakeryApp {

    /** Performs all CRUD operations and the daily report. */
    private final OrderManager manager;

    /** Shortcut to the validation rules (the same object the manager uses). */
    private final OrderValidator validator;

    /** Reads what the user types. */
    private final Scanner scanner;

    /** The word the user types to stop adding/updating/removing and go back to the menu. */
    private final String cancelWord = "cancel";

    /** Menu option that ends the program. */
    private final String exitChoice = "7";

    /**
     * Starts the program. This is the only static method: it builds the
     * objects the program needs and hands control to run().
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        OrderValidator validator = new OrderValidator(List.of("Chocolate", "Vanilla", "Red Velvet", "Carrot"));
        OrderFileLoader loader = new OrderFileLoader(",");
        OrderManager manager = new OrderManager(validator, loader, 0.07);
        BakeryApp app = new BakeryApp(manager, new Scanner(System.in));
        app.run();
    }

    /**
     * Creates the app.
     *
     * @param manager the order manager that stores and processes orders
     * @param scanner reads keyboard input
     */
    public BakeryApp(OrderManager manager, Scanner scanner) {
        this.manager = manager;
        this.validator = manager.getValidator();
        this.scanner = scanner;
    }

    /**
     * Shows the menu over and over until the user chooses Exit.
     *
     * @return 0 when the program ends normally
     */
    public int run() {
        System.out.println();
        System.out.println("Welcome to the Bakery Order Management System!");
        boolean running = true;
        while (running) {
            try {
                String choice = readLine(buildMenu()).trim();
                if (choice.equals(exitChoice)) {
                    running = !confirmExit();
                } else {
                    System.out.println(handleChoice(choice));
                }
            } catch (InputCancelledException e) {
                // The user typed "cancel" in the middle of a task.
                System.out.println("Cancelled. No changes were made. Returning to the main menu.");
            } catch (InputEndedException e) {
                // Keyboard input was closed, so there is nothing more to read.
                System.out.println(System.lineSeparator() + "Input was closed. Exiting the program. Goodbye!");
                return 0;
            } catch (RuntimeException e) {
                // Last line of defense: report the problem and keep the program running.
                System.out.println("Something unexpected went wrong (" + e.getClass().getSimpleName()
                        + "). No data was lost. Returning to the main menu.");
            }
        }
        System.out.println("Goodbye! Thank you for using the Bakery Order Management System.");
        return 0;
    }

    /**
     * Builds the main menu text.
     *
     * @return the menu, ending with the prompt for a choice
     */
    public String buildMenu() {
        return System.lineSeparator()
                + "========== BAKERY ORDER MANAGEMENT SYSTEM ==========" + System.lineSeparator()
                + "  Orders in the system: " + manager.getOrderCount() + System.lineSeparator()
                + "  1. Load orders from a text file" + System.lineSeparator()
                + "  2. Display all orders" + System.lineSeparator()
                + "  3. Add a new order" + System.lineSeparator()
                + "  4. Remove an order" + System.lineSeparator()
                + "  5. Update an order" + System.lineSeparator()
                + "  6. Daily Production Report (cakes to bake and expected revenue)" + System.lineSeparator()
                + "  7. Exit" + System.lineSeparator()
                + "====================================================" + System.lineSeparator()
                + "Choose an option (1-7): ";
    }

    /**
     * Runs the task the user picked from the menu.
     *
     * @param choice what the user typed at the menu
     * @return the message to show the user
     */
    public String handleChoice(String choice) {
        switch (choice) {
            case "1":
                return loadFileFromInput();
            case "2":
                return displayAllOrders();
            case "3":
                return addOrderFromInput();
            case "4":
                return removeOrderFromInput();
            case "5":
                return updateOrderFromInput();
            case "6":
                return reportFromInput();
            default:
                return "\"" + shorten(choice) + "\" is not a menu option. Please enter a number from 1 to 7.";
        }
    }

    // ===============================================================
    // MENU OPTION 1: LOAD FROM FILE
    // ===============================================================

    /**
     * Asks for a file name and loads the orders in it.
     *
     * @return a summary of what was loaded and any lines that were skipped
     */
    public String loadFileFromInput() {
        System.out.println();
        System.out.println("--- Load Orders From a Text File ---");
        System.out.println("Enter the file name or full path, for example:");
        System.out.println("   orders.txt");
        System.out.println("   C:\\Users\\you\\Desktop\\orders.txt   or   /Users/you/Desktop/orders.txt");
        String path = readLine("File: ");
        return String.join(System.lineSeparator(), manager.loadFromFile(path));
    }

    // ===============================================================
    // MENU OPTION 2: DISPLAY ALL
    // ===============================================================

    /**
     * Builds a table of every order in the system.
     *
     * @return the table, or a message if there are no orders
     */
    public String displayAllOrders() {
        List<Order> all = manager.getAllOrders();
        if (all.isEmpty()) {
            return "There are no orders in the system yet. Use option 1 to load a file or option 3 to add an order.";
        }
        String rowFormat = "%-5s %-22s %-13s %-11s %4s %8s  %-10s  %-9s %9s  %s%n";
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator()).append("ALL ORDERS (sorted by order ID)").append(System.lineSeparator());
        sb.append(String.format(rowFormat, "ID", "Customer", "Phone", "Cake", "Qty", "Unit $",
                "Pickup", "Status", "Total", "Special Instructions"));
        sb.append("-".repeat(130)).append(System.lineSeparator());
        for (Order order : all) {
            sb.append(String.format(rowFormat,
                    order.getOrderId(),
                    shorten(order.getCustomerName(), 22),
                    order.getPhoneNumber(),
                    order.getCakeType(),
                    order.getQuantity(),
                    String.format("$%.2f", order.getUnitPrice()),
                    order.getPickupDate(),
                    order.getStatus(),
                    String.format("$%,.2f", manager.calculateTotal(order)),
                    order.getSpecialInstructions().isEmpty() ? "-" : order.getSpecialInstructions()));
        }
        sb.append("-".repeat(130)).append(System.lineSeparator());
        sb.append("Total orders: ").append(all.size())
                .append("   (Total includes ").append(percentText()).append(" sales tax.)");
        return sb.toString();
    }

    // ===============================================================
    // MENU OPTION 3: ADD
    // ===============================================================

    /**
     * Asks for each field of a new order, checking every value as it is typed.
     *
     * @return a confirmation showing the new order, or the reasons it was not added
     */
    public String addOrderFromInput() {
        System.out.println();
        System.out.println("--- Add a New Order ---  (type \"cancel\" at any prompt to return to the menu)");
        int orderId = promptOrderId(-1);
        String name = promptName();
        String phone = promptPhone();
        String cakeType = promptCakeType();
        int quantity = promptQuantity();
        double price = promptPrice();
        LocalDate pickupDate = promptPickupDate(manager.getToday());
        String instructions = promptInstructions();

        // New orders always start as "not picked up yet".
        Order order = new Order(orderId, name, phone, cakeType, quantity, price, pickupDate, false, instructions);
        List<String> errors = manager.addOrder(order);
        if (!errors.isEmpty()) {
            return "The order was NOT added:" + System.lineSeparator() + bulletList(errors);
        }
        Order saved = manager.findOrderById(orderId).orElse(order);
        return System.lineSeparator() + "Order #" + orderId + " was added successfully:" + System.lineSeparator()
                + formatOrderDetails(saved) + System.lineSeparator()
                + "The system now holds " + manager.getOrderCount() + " orders.";
    }

    // ===============================================================
    // MENU OPTION 4: REMOVE
    // ===============================================================

    /**
     * Asks for an order ID, shows the order and removes it after the user confirms.
     *
     * @return a message saying whether the order was removed
     */
    public String removeOrderFromInput() {
        if (manager.getOrderCount() == 0) {
            return "There are no orders to remove.";
        }
        System.out.println();
        System.out.println("--- Remove an Order ---  (type \"cancel\" to return to the menu)");
        int orderId = readInt("Enter the order ID to remove: ");
        Optional<Order> found = manager.findOrderById(orderId);
        if (found.isEmpty()) {
            return "No order #" + orderId + " was found. Nothing was removed. (Use option 2 to see all order IDs.)";
        }
        System.out.println(formatOrderDetails(found.get()));
        if (!promptYesNo("Remove this order? (Y/N): ")) {
            return "Removal cancelled. Order #" + orderId + " was kept.";
        }
        if (manager.removeOrder(orderId)) {
            return "Order #" + orderId + " was removed. " + manager.getOrderCount() + " order(s) remain.";
        }
        return "Order #" + orderId + " could not be removed.";
    }

    // ===============================================================
    // MENU OPTION 5: UPDATE
    // ===============================================================

    /**
     * Asks for an order ID, then lets the user change any of its 9 fields,
     * one at a time, until they choose Done.
     *
     * @return a summary of the order after all changes
     */
    public String updateOrderFromInput() {
        if (manager.getOrderCount() == 0) {
            return "There are no orders to update.";
        }
        System.out.println();
        System.out.println("--- Update an Order ---  (type \"cancel\" to return to the menu)");
        int orderId = readInt("Enter the order ID to update: ");
        Optional<Order> found = manager.findOrderById(orderId);
        if (found.isEmpty()) {
            return "No order #" + orderId + " was found. (Use option 2 to see all order IDs.)";
        }
        Order current = found.get();
        int changes = 0;

        while (true) {
            System.out.println();
            System.out.println(formatOrderDetails(current));
            System.out.println("  0. Done updating");
            int field = readInt("Which field do you want to change? (0-9): ");
            if (field == 0) {
                break;
            }
            if (field < 0 || field > 9) {
                System.out.println("Please enter a number from 0 to 9.");
                continue;
            }
            // Work on a copy; the stored order only changes if the manager accepts the edit.
            Order edited = current.copy();
            String fieldName = applyFieldChange(edited, field, current);
            List<String> errors = manager.updateOrder(current.getOrderId(), edited);
            if (errors.isEmpty()) {
                current = manager.findOrderById(edited.getOrderId()).orElse(edited);
                changes++;
                System.out.println("Saved. " + fieldName + " for order #" + current.getOrderId() + " was updated.");
            } else {
                System.out.println("That change was NOT saved:" + System.lineSeparator() + bulletList(errors));
            }
        }
        return "Finished updating. " + changes + " change(s) saved." + System.lineSeparator()
                + formatOrderDetails(current);
    }

    /**
     * Asks for a new value for one field and puts it into the edited order.
     *
     * @param edited  the copy of the order being edited
     * @param field   the field number from the update menu (1-9)
     * @param current the order as it is now stored (used for rules such as "keep the same ID")
     * @return the field name that was changed
     */
    private String applyFieldChange(Order edited, int field, Order current) {
        switch (field) {
            case 1:
                edited.setOrderId(promptOrderId(current.getOrderId()));
                return "Order ID";
            case 2:
                edited.setCustomerName(promptName());
                return "Customer name";
            case 3:
                edited.setPhoneNumber(promptPhone());
                return "Phone number";
            case 4:
                edited.setCakeType(promptCakeType());
                return "Cake type";
            case 5:
                edited.setQuantity(promptQuantity());
                return "Quantity";
            case 6:
                edited.setUnitPrice(promptPrice());
                return "Unit price";
            case 7:
                // An order whose day has passed may keep its old date; otherwise the date must be today or later.
                LocalDate today = manager.getToday();
                LocalDate earliest = current.getPickupDate().isBefore(today) ? current.getPickupDate() : today;
                edited.setPickupDate(promptPickupDate(earliest));
                return "Pickup date";
            case 8:
                edited.setPickedUp(promptYesNo("Has the customer picked up this order? (Y/N): "));
                return "Picked up";
            default:
                edited.setSpecialInstructions(promptInstructions());
                return "Special instructions";
        }
    }

    // ===============================================================
    // MENU OPTION 6: CUSTOM ACTION - DAILY PRODUCTION REPORT
    // ===============================================================

    /**
     * Asks for a pickup date and shows the Daily Production Report for it.
     *
     * @return the formatted report
     */
    public String reportFromInput() {
        System.out.println();
        System.out.println("--- Daily Production Report ---  (type \"cancel\" to return to the menu)");
        System.out.println("Shows how many cakes of each type to bake and the expected revenue");
        System.out.println("for orders due on a date that have NOT been picked up yet.");
        LocalDate date = readDate("Enter the pickup date (YYYY-MM-DD or MM/DD/YYYY): ");
        DailyReport report = manager.generateDailyReport(date);
        return report.format();
    }

    // ===============================================================
    // MENU OPTION 7: EXIT
    // ===============================================================

    /**
     * Asks the user to confirm before the program ends.
     *
     * @return true if the user really wants to exit
     */
    private boolean confirmExit() {
        System.out.println("Note: in Phase 1, orders are kept in memory only and are cleared when the program ends.");
        try {
            return promptYesNo("Are you sure you want to exit? (Y/N): ");
        } catch (InputCancelledException e) {
            return false; // "cancel" here means "don't exit"
        }
    }

    // ===============================================================
    // FIELD PROMPTS: each one keeps asking until the value is valid
    // ===============================================================

    /**
     * Asks for an order ID that is 4 digits and not used by another order.
     *
     * @param allowedExistingId an ID that may be reused (the order's own ID when updating), or -1
     * @return a valid, unused order ID
     */
    private int promptOrderId(int allowedExistingId) {
        while (true) {
            int id = readInt("Order ID (1000-9999): ");
            if (!validator.isValidOrderId(id)) {
                System.out.println(validator.ruleFor("orderId"));
            } else if (id != allowedExistingId && manager.isIdTaken(id)) {
                System.out.println("Order #" + id + " already exists. Please choose a different ID.");
            } else {
                return id;
            }
        }
    }

    /**
     * Asks for a customer name.
     *
     * @return a valid customer name with extra spaces removed
     */
    private String promptName() {
        while (true) {
            String name = readCancellable("Customer name: ").trim().replaceAll("\\s+", " ");
            if (validator.isValidName(name)) {
                return name;
            }
            System.out.println(validator.ruleFor("name"));
        }
    }

    /**
     * Asks for a phone number.
     *
     * @return the phone number in 407-555-0142 format
     */
    private String promptPhone() {
        while (true) {
            Optional<String> phone = validator.formatPhone(readCancellable("Phone number (10 digits): "));
            if (phone.isPresent()) {
                return phone.get();
            }
            System.out.println(validator.ruleFor("phone"));
        }
    }

    /**
     * Shows the cake types as a numbered list and asks the user to pick one
     * by number or by name.
     *
     * @return a valid cake type
     */
    private String promptCakeType() {
        List<String> types = validator.getValidCakeTypes();
        StringBuilder options = new StringBuilder("Cake types: ");
        for (int i = 0; i < types.size(); i++) {
            options.append(i + 1).append(". ").append(types.get(i)).append("   ");
        }
        System.out.println(options.toString().trim());
        while (true) {
            String input = readCancellable("Cake type (number or name): ").trim();
            // Accept the menu number...
            if (input.matches("\\d{1,2}")) {
                int number = Integer.parseInt(input);
                if (number >= 1 && number <= types.size()) {
                    return types.get(number - 1);
                }
            }
            // ...or the name, in any upper/lower case.
            Optional<String> match = validator.matchCakeType(input);
            if (match.isPresent()) {
                return match.get();
            }
            System.out.println("Please enter 1-" + types.size() + " or a cake name. " + validator.ruleFor("cakeType"));
        }
    }

    /**
     * Asks for the number of cakes.
     *
     * @return a quantity from 1 to 50
     */
    private int promptQuantity() {
        while (true) {
            int quantity = readInt("Quantity (1-50): ");
            if (validator.isValidQuantity(quantity)) {
                return quantity;
            }
            System.out.println(validator.ruleFor("quantity"));
        }
    }

    /**
     * Asks for the price of one cake.
     *
     * @return a valid unit price
     */
    private double promptPrice() {
        while (true) {
            double price = readDouble("Unit price per cake (e.g. 28.00): $");
            if (validator.isValidPrice(price)) {
                return price;
            }
            System.out.println(validator.ruleFor("price"));
        }
    }

    /**
     * Asks for a pickup date that is not before the earliest allowed date and
     * not more than 2 years ahead.
     *
     * @param earliestAllowed the first date that is accepted (normally today)
     * @return a valid pickup date
     */
    private LocalDate promptPickupDate(LocalDate earliestAllowed) {
        LocalDate today = manager.getToday();
        while (true) {
            LocalDate date = readDate("Pickup date (YYYY-MM-DD or MM/DD/YYYY): ");
            if (validator.isValidPickupDate(date, earliestAllowed, today)) {
                return date;
            }
            System.out.println("Pickup date must be from " + earliestAllowed + " to " + today.plusYears(2) + ".");
        }
    }

    /**
     * Asks for optional special instructions.
     *
     * @return the instructions, or an empty string for none
     */
    private String promptInstructions() {
        while (true) {
            String text = readCancellable("Special instructions (press Enter for none): ").trim();
            if (validator.isValidInstructions(text)) {
                return text;
            }
            System.out.println(validator.ruleFor("instructions"));
        }
    }

    /**
     * Asks a yes/no question.
     *
     * @param prompt the question to show
     * @return true for yes, false for no
     */
    private boolean promptYesNo(String prompt) {
        while (true) {
            String answer = readCancellable(prompt).trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println("Please type Y for yes or N for no.");
        }
    }

    // ===============================================================
    // LOW-LEVEL INPUT HELPERS
    // ===============================================================

    /**
     * Shows a prompt and reads one line of input.
     *
     * @param prompt the text to show
     * @return the line the user typed
     * @throws InputEndedException if keyboard input has been closed
     */
    private String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException | IllegalStateException e) {
            throw new InputEndedException();
        }
    }

    /**
     * Reads a line, but lets the user type "cancel" to go back to the menu.
     *
     * @param prompt the text to show
     * @return the line the user typed
     * @throws InputCancelledException if the user typed "cancel"
     */
    private String readCancellable(String prompt) {
        String input = readLine(prompt);
        if (input.trim().equalsIgnoreCase(cancelWord)) {
            throw new InputCancelledException();
        }
        return input;
    }

    /**
     * Keeps asking until the user types a whole number. Letters, decimals,
     * blank input and numbers too large for an int are all rejected.
     *
     * @param prompt the text to show
     * @return the whole number entered
     */
    private int readInt(String prompt) {
        while (true) {
            String input = readCancellable(prompt).trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("\"" + shorten(input) + "\" is not a whole number. Please type digits only, e.g. 1004.");
            }
        }
    }

    /**
     * Keeps asking until the user types a plain dollar amount such as 28 or
     * 28.50. "NaN", "Infinity", "1e3" and negative numbers are rejected.
     *
     * @param prompt the text to show
     * @return the amount entered
     */
    private double readDouble(String prompt) {
        while (true) {
            String input = readCancellable(prompt).trim();
            if (input.startsWith("$")) {
                input = input.substring(1).trim();
            }
            if (input.matches("\\d{1,7}(\\.\\d{1,2})?")) {
                return Double.parseDouble(input);
            }
            System.out.println("\"" + shorten(input) + "\" is not a dollar amount. Please type a number like 28 or 28.50.");
        }
    }

    /**
     * Keeps asking until the user types a real date.
     *
     * @param prompt the text to show
     * @return the date entered
     */
    private LocalDate readDate(String prompt) {
        while (true) {
            String input = readCancellable(prompt).trim();
            Optional<LocalDate> date = validator.parseDate(input);
            if (date.isPresent()) {
                return date.get();
            }
            System.out.println("\"" + shorten(input) + "\" is not a real date. Use YYYY-MM-DD (2027-02-13) or MM/DD/YYYY (2/13/2027).");
        }
    }

    // ===============================================================
    // FORMATTING HELPERS
    // ===============================================================

    /**
     * Lists every field of an order with the number used in the update menu.
     *
     * @param order the order to show
     * @return the order as numbered, multi-line text
     */
    private String formatOrderDetails(Order order) {
        String nl = System.lineSeparator();
        return "Order #" + order.getOrderId() + nl
                + "  1. Order ID:             " + order.getOrderId() + nl
                + "  2. Customer name:        " + order.getCustomerName() + nl
                + "  3. Phone number:         " + order.getPhoneNumber() + nl
                + "  4. Cake type:            " + order.getCakeType() + nl
                + "  5. Quantity:             " + order.getQuantity() + nl
                + "  6. Unit price:           " + String.format("$%.2f", order.getUnitPrice()) + nl
                + "  7. Pickup date:          " + order.getPickupDate() + nl
                + "  8. Picked up:            " + (order.isPickedUp() ? "Yes" : "No") + nl
                + "  9. Special instructions: "
                + (order.getSpecialInstructions().isEmpty() ? "(none)" : order.getSpecialInstructions()) + nl
                + "     Order total (incl. " + percentText() + " tax): "
                + String.format("$%,.2f", manager.calculateTotal(order));
    }

    /**
     * Turns a list of messages into indented bullet lines.
     *
     * @param messages the messages to list
     * @return one "  - message" line per message
     */
    private String bulletList(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append("  - ").append(message);
        }
        return sb.toString();
    }

    /** @return the tax rate as text, e.g. "7%" */
    private String percentText() {
        return String.format("%.0f%%", manager.getTaxRate() * 100);
    }

    /**
     * Shortens what the user typed so error messages stay readable.
     *
     * @param text the text to shorten
     * @return the text cut to 30 characters
     */
    private String shorten(String text) {
        return shorten(text, 30);
    }

    /**
     * Shortens text to a maximum length, adding "..." if it was cut.
     *
     * @param text      the text to shorten
     * @param maxLength the longest allowed length
     * @return the shortened text
     */
    private String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}
