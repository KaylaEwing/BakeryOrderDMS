/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026
 * Class:   OrderManager
 *
 * Purpose: The business logic layer of the DMS. It keeps the list of Order
 *          objects and performs every operation on them:
 *            - Create: add one order, or load many from a text file
 *            - Read:   return all orders, or find one by ID
 *            - Update: replace an order with an edited copy
 *            - Delete: remove an order by ID
 *            - Custom action: generate the Daily Production Report
 *          Every order is validated before it is stored, so the list can only
 *          ever contain good data. Methods return results (lists of error
 *          messages, booleans, objects) instead of printing, so they are easy
 *          to test and can be reused by the GUI in a later phase.
 */
package bakerydms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores orders and performs CRUD operations and the daily report on them.
 */
public class OrderManager {

    /** All orders currently in the system. Phase 1 keeps them in memory only. */
    private final List<Order> orders = new ArrayList<>();

    /** Checks every order before it is stored. */
    private final OrderValidator validator;

    /** Reads orders from text files. */
    private final OrderFileLoader loader;

    /** Sales tax rate, e.g. 0.07 for 7%. */
    private final double taxRate;

    /** Source of "today". Passing a Clock in lets tests use a fixed date. */
    private final Clock clock;

    /** At most this many skipped-line messages are listed after a file load. */
    private final int maxProblemsShown = 20;

    /**
     * Creates a manager that uses the computer's clock for today's date.
     *
     * @param validator checks order data
     * @param loader    reads order files
     * @param taxRate   sales tax rate from 0 to 1 (0.07 = 7%)
     */
    public OrderManager(OrderValidator validator, OrderFileLoader loader, double taxRate) {
        this(validator, loader, taxRate, Clock.systemDefaultZone());
    }

    /**
     * Creates a manager with a specific clock (used for testing with a fixed date).
     *
     * @param validator checks order data
     * @param loader    reads order files
     * @param taxRate   sales tax rate from 0 to 1 (0.07 = 7%)
     * @param clock     source of today's date
     * @throws IllegalArgumentException if any argument is missing or the tax rate is out of range
     */
    public OrderManager(OrderValidator validator, OrderFileLoader loader, double taxRate, Clock clock) {
        if (validator == null || loader == null || clock == null) {
            throw new IllegalArgumentException("Validator, loader and clock are required.");
        }
        if (!Double.isFinite(taxRate) || taxRate < 0 || taxRate > 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1.");
        }
        this.validator = validator;
        this.loader = loader;
        this.taxRate = taxRate;
        this.clock = clock;
    }

    /** @return today's date according to the manager's clock */
    public LocalDate getToday() {
        return LocalDate.now(clock);
    }

    /** @return the sales tax rate (0.07 = 7%) */
    public double getTaxRate() {
        return taxRate;
    }

    /** @return the validator, so the user interface can show the same rules */
    public OrderValidator getValidator() {
        return validator;
    }

    // ===============================================================
    // CREATE
    // ===============================================================

    /**
     * Validates an order and stores it if every rule passes and its ID is
     * not already used. New orders must have a pickup date from today on.
     *
     * @param order the order to add
     * @return a list of problems; an EMPTY list means the order was added
     */
    public List<String> addOrder(Order order) {
        if (order == null) {
            return List.of("No order was provided.");
        }
        Order cleaned = validator.normalize(order);
        LocalDate today = getToday();
        List<String> errors = validator.validate(cleaned, today, today);
        // Only check for a duplicate if the ID itself is valid (avoids two messages for one problem).
        if (validator.isValidOrderId(cleaned.getOrderId()) && isIdTaken(cleaned.getOrderId())) {
            errors.add("Order #" + cleaned.getOrderId() + " already exists. Order IDs must be unique.");
        }
        if (errors.isEmpty()) {
            orders.add(cleaned);
        }
        return errors;
    }

    /**
     * Loads orders from a text file. Each line is checked separately: good
     * lines are added and bad lines are skipped with a message explaining why,
     * so one bad line never stops the whole file from loading.
     *
     * @param filePath the file name or path typed by the user
     * @return messages for the user: a summary line followed by any skipped lines
     */
    public List<String> loadFromFile(String filePath) {
        List<String> messages = new ArrayList<>();
        List<String> lines;
        try {
            lines = loader.readLines(filePath);
        } catch (IllegalArgumentException | IOException e) {
            messages.add("Could not load the file. " + e.getMessage());
            return messages;
        }

        int added = 0;
        int dataLines = 0;
        List<String> problems = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (loader.isBlankOrComment(line)) {
                continue; // skip empty lines and # comments
            }
            dataLines++;
            int lineNumber = i + 1; // people count lines from 1, not 0
            try {
                Order order = loader.parseLine(line);
                List<String> errors = addOrder(order);
                if (errors.isEmpty()) {
                    added++;
                } else {
                    problems.add("Line " + lineNumber + " skipped: " + String.join(" ", errors));
                }
            } catch (IllegalArgumentException e) {
                problems.add("Line " + lineNumber + " skipped: " + e.getMessage());
            }
        }

        if (dataLines == 0) {
            messages.add("The file was opened, but it has no order lines to load.");
            return messages;
        }
        messages.add("Loaded " + added + " of " + dataLines + " orders. "
                + problems.size() + " line(s) skipped. The system now holds " + orders.size() + " orders.");
        // List the problems, but not thousands of them if the wrong file was chosen.
        for (int i = 0; i < problems.size() && i < maxProblemsShown; i++) {
            messages.add("  - " + problems.get(i));
        }
        if (problems.size() > maxProblemsShown) {
            messages.add("  ...and " + (problems.size() - maxProblemsShown) + " more skipped line(s).");
        }
        return messages;
    }

    // ===============================================================
    // READ
    // ===============================================================

    /**
     * Returns copies of every order, sorted by order ID. Copies are returned
     * so stored orders can only be changed through updateOrder().
     *
     * @return all orders in the system
     */
    public List<Order> getAllOrders() {
        List<Order> copies = new ArrayList<>();
        for (Order order : orders) {
            copies.add(order.copy());
        }
        copies.sort(Comparator.comparingInt(Order::getOrderId));
        return copies;
    }

    /**
     * Finds an order by its ID.
     *
     * @param orderId the order number to look for
     * @return a copy of the order, or an empty Optional if no order has that ID
     */
    public Optional<Order> findOrderById(int orderId) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                return Optional.of(order.copy());
            }
        }
        return Optional.empty();
    }

    /**
     * @param orderId the order number to check
     * @return true if an order with this ID is already stored
     */
    public boolean isIdTaken(int orderId) {
        return findOrderById(orderId).isPresent();
    }

    /** @return how many orders are stored */
    public int getOrderCount() {
        return orders.size();
    }

    // ===============================================================
    // UPDATE
    // ===============================================================

    /**
     * Replaces an order with an edited version after checking every field.
     * Any field can change, including the order ID (if the new ID is unused).
     * The pickup date may stay as it was even if that day has passed, but a
     * changed date must be from today on.
     *
     * @param orderId the ID of the order to change
     * @param updated the edited order
     * @return a list of problems; an EMPTY list means the order was updated
     */
    public List<String> updateOrder(int orderId, Order updated) {
        int index = indexOf(orderId);
        if (index < 0) {
            return List.of("Order #" + orderId + " was not found.");
        }
        if (updated == null) {
            return List.of("No order was provided.");
        }
        Order original = orders.get(index);
        Order cleaned = validator.normalize(updated);
        LocalDate today = getToday();
        // An old pickup date that hasn't changed is still allowed; a new date must be today or later.
        LocalDate earliest = original.getPickupDate().isBefore(today) ? original.getPickupDate() : today;
        List<String> errors = validator.validate(cleaned, earliest, today);
        boolean idChanged = cleaned.getOrderId() != orderId;
        if (idChanged && validator.isValidOrderId(cleaned.getOrderId()) && isIdTaken(cleaned.getOrderId())) {
            errors.add("Order #" + cleaned.getOrderId() + " already exists. Order IDs must be unique.");
        }
        if (errors.isEmpty()) {
            orders.set(index, cleaned);
        }
        return errors;
    }

    // ===============================================================
    // DELETE
    // ===============================================================

    /**
     * Removes the order with the given ID.
     *
     * @param orderId the order number to remove
     * @return true if an order was removed, false if no order had that ID
     */
    public boolean removeOrder(int orderId) {
        return orders.removeIf(order -> order.getOrderId() == orderId);
    }

    // ===============================================================
    // CALCULATIONS AND CUSTOM ACTION
    // ===============================================================

    /**
     * Calculates sales tax on an amount, rounded to the nearest cent.
     *
     * @param subtotal the amount before tax
     * @return the tax amount
     */
    public double calculateTax(double subtotal) {
        return BigDecimal.valueOf(subtotal)
                .multiply(BigDecimal.valueOf(taxRate))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Calculates what the customer pays for an order: subtotal plus sales tax.
     * Example: 2 Red Velvet cakes at $28.00 = $56.00 + $3.92 tax = $59.92.
     *
     * @param order the order to price
     * @return the order total in dollars
     */
    public double calculateTotal(Order order) {
        double subtotal = order.calculateSubtotal();
        return BigDecimal.valueOf(subtotal)
                .add(BigDecimal.valueOf(calculateTax(subtotal)))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * CUSTOM ACTION: Daily Production Report.
     * For the chosen pickup date, counts how many cakes of each type must be
     * baked and calculates the expected revenue. Only orders that are due on
     * that date AND have not been picked up yet are counted.
     *
     * @param date the pickup date to report on
     * @return the finished report (with zero orders if nothing is due)
     * @throws IllegalArgumentException if the date is null
     */
    public DailyReport generateDailyReport(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("A report date is required.");
        }
        // Start every cake type at 0 so the report always lists all four.
        Map<String, Integer> cakeCounts = new LinkedHashMap<>();
        for (String type : validator.getValidCakeTypes()) {
            cakeCounts.put(type, 0);
        }
        int orderCount = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getPickupDate().equals(date) && !order.isPickedUp()) {
                cakeCounts.merge(order.getCakeType(), order.getQuantity(), Integer::sum);
                subtotal = subtotal.add(BigDecimal.valueOf(order.calculateSubtotal()));
                orderCount++;
            }
        }
        double subtotalValue = subtotal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        double tax = calculateTax(subtotalValue);
        double revenue = BigDecimal.valueOf(subtotalValue).add(BigDecimal.valueOf(tax))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        return new DailyReport(date, cakeCounts, orderCount, subtotalValue, tax, revenue, taxRate);
    }

    /**
     * Finds where an order is stored in the list.
     *
     * @param orderId the order number to look for
     * @return the position in the list, or -1 if not found
     */
    private int indexOf(int orderId) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderId() == orderId) {
                return i;
            }
        }
        return -1;
    }
}
