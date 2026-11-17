/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026 (Phase 1); updated November 13, 2026 (Phase 4); documented November 16, 2026 (Module 11)
 * Class:   OrderManager
 *
 * Purpose: The business logic layer of the DMS. It checks every order and
 *          performs every operation on the stored orders:
 *            - Create: add an order
 *            - Read:   return all orders, or find one by ID
 *            - Update: change any field of an order
 *            - Delete: remove an order by ID
 *            - Custom action: generate the Daily Production Report
 *          In Phase 4 the orders are stored in a MySQL database. The manager
 *          does not know that: it talks to an OrderRepository, so the same
 *          rules apply no matter where the data lives. Every order is
 *          validated BEFORE it is sent to the database, so the database can
 *          only ever contain good data. Methods return results (lists of
 *          error messages, booleans, objects) instead of printing.
 *
 *          If no database is connected yet, every data method throws a
 *          DataAccessException that tells the user to connect first.
 */
package bakerydms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Carries out every action on orders (the business logic layer).
 *
 * <p>The window calls this class for each CRUD operation and for the custom
 * action. OrderManager:
 * <ul>
 *   <li>checks every value with {@link OrderValidator} before anything is stored,</li>
 *   <li>stores and reads orders through an {@link OrderRepository} (the MySQL database
 *       in the real program, or an in-memory copy in the unit tests),</li>
 *   <li>calculates order totals with tax, and</li>
 *   <li>builds the {@link DailyReport} for the Daily Production Report.</li>
 * </ul>
 * Methods that change data return a list of problems; an empty list means
 * the change was saved. Until {@link #connectTo(OrderRepository)} is called,
 * data methods throw a {@link DataAccessException} saying no database is connected.
 *
 * <p>Usage:
 * <pre>{@code
 * OrderManager manager = new OrderManager(new OrderValidator(cakeTypes), 0.07);
 * manager.connectTo(connector.connect("localhost", "3306", "bakery_dms", "root", password));
 * List<String> problems = manager.addOrder(1026, "Dana Lee", "4075559876", "Red Velvet",
 *         2, 28.00, LocalDate.of(2027, 2, 13), "Write Happy Birthday, Dana!");
 * DailyReport report = manager.generateDailyReport(LocalDate.of(2027, 2, 13));
 * }</pre>
 *
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 * @see OrderRepository
 * @see BakeryOrderWindow
 */
public class OrderManager {

    /** Where orders are stored (the MySQL database), or null until the user connects. */
    private OrderRepository repository;

    /** Checks every order before it is stored. */
    private final OrderValidator validator;

    /** Sales tax rate, e.g. 0.07 for 7%. */
    private final double taxRate;

    /** Source of "today". Passing a Clock in lets tests use a fixed date. */
    private final Clock clock;

    /**
     * Creates a manager that uses the computer's clock for today's date.
     *
     * @param validator checks order data
     * @param taxRate   sales tax rate from 0 to 1 (0.07 = 7%)
     */
    public OrderManager(OrderValidator validator, double taxRate) {
        this(validator, taxRate, Clock.systemDefaultZone());
    }

    /**
     * Creates a manager with a specific clock (used for testing with a fixed date).
     *
     * @param validator checks order data
     * @param taxRate   sales tax rate from 0 to 1 (0.07 = 7%)
     * @param clock     source of today's date
     * @throws IllegalArgumentException if any argument is missing or the tax rate is out of range
     */
    public OrderManager(OrderValidator validator, double taxRate, Clock clock) {
        if (validator == null || clock == null) {
            throw new IllegalArgumentException("Validator and clock are required.");
        }
        if (!Double.isFinite(taxRate) || taxRate < 0 || taxRate > 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1.");
        }
        this.validator = validator;
        this.taxRate = taxRate;
        this.clock = clock;
    }

    /**
     * Returns today's date according to the manager's clock.
     *
     * @return today's date according to the manager's clock
     */
    public LocalDate getToday() {
        return LocalDate.now(clock);
    }

    /**
     * Returns the sales tax rate (0.07 = 7%).
     *
     * @return the sales tax rate (0.07 = 7%)
     */
    public double getTaxRate() {
        return taxRate;
    }

    /**
     * Returns the validator, so the user interface can show the same rules.
     *
     * @return the validator, so the user interface can show the same rules
     */
    public OrderValidator getValidator() {
        return validator;
    }

    // ===============================================================
    // CONNECTING TO STORAGE
    // ===============================================================

    /**
     * Starts using a storage (the MySQL database). If another database was
     * connected before, its connection is closed first.
     *
     * @param newRepository the connected storage
     * @return the number of orders in it
     * @throws IllegalArgumentException if the repository is null
     */
    public int connectTo(OrderRepository newRepository) {
        if (newRepository == null) {
            throw new IllegalArgumentException("A repository is required.");
        }
        if (repository != null && repository != newRepository) {
            repository.close();
        }
        repository = newRepository;
        return repository.count();
    }

    /**
     * Returns true once a database has been connected.
     *
     * @return true once a database has been connected
     */
    public boolean isConnected() {
        return repository != null;
    }

    /**
     * Returns where the data is stored, or "not connected".
     *
     * @return where the data is stored, or "not connected"
     */
    public String describeStorage() {
        return repository == null ? "not connected" : repository.describe();
    }

    /**
     * Closes the database connection (used when the program exits).
     *
     * @return true if a connection was closed
     */
    public boolean disconnect() {
        if (repository == null) {
            return false;
        }
        boolean closed = repository.close();
        repository = null;
        return closed;
    }

    /**
     * Gets the storage, or stops with a clear message if none is connected.
     *
     * @return the connected repository
     * @throws DataAccessException if no database is connected yet
     */
    private OrderRepository storage() {
        if (repository == null) {
            throw new DataAccessException("Not connected to a database yet. Enter your MySQL details at the top and click Connect.");
        }
        return repository;
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
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
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
        if (errors.isEmpty() && !storage().insert(cleaned)) {
            errors.add("The database did not save order #" + cleaned.getOrderId() + ".");
        }
        return errors;
    }

    /**
     * Creates an order from individual values entered by the user and adds it.
     * New orders always start as "not picked up". This version exists so the
     * user interface (and unit tests) can pass plain values instead of building
     * an Order object first.
     *
     * @param orderId             unique 4-digit order number
     * @param customerName        customer's full name
     * @param phoneNumber         customer's phone number
     * @param cakeType            type of cake
     * @param quantity            number of cakes
     * @param unitPrice           price of one cake
     * @param pickupDate          date the order will be collected
     * @param specialInstructions notes for the baker (may be empty)
     * @return a list of problems; an EMPTY list means the order was added
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
     */
    public List<String> addOrder(int orderId, String customerName, String phoneNumber, String cakeType,
                                 int quantity, double unitPrice, LocalDate pickupDate,
                                 String specialInstructions) {
        Order order = new Order(orderId, customerName, phoneNumber, cakeType, quantity,
                unitPrice, pickupDate, false, specialInstructions);
        return addOrder(order);
    }

    // ===============================================================
    // READ
    // ===============================================================

    /**
     * Reads every order from the database, sorted by order ID. The orders
     * returned are copies, so the stored data only changes through
     * addOrder(), updateOrder() and removeOrder().
     *
     * @return all orders in the system
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
     */
    public List<Order> getAllOrders() {
        List<Order> all = storage().findAll();
        all.sort(Comparator.comparingInt(Order::getOrderId));
        return all;
    }

    /**
     * Finds an order by its ID.
     *
     * @param orderId the order number to look for
     * @return a copy of the order, or an empty Optional if no order has that ID
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
     */
    public Optional<Order> findOrderById(int orderId) {
        return storage().findById(orderId);
    }

    /**
     * Checks whether an order ID is already in use, so a new order cannot reuse it.
     *
     * @param orderId the order number to check
     * @return true if an order with this ID is already stored
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
     */
    public boolean isIdTaken(int orderId) {
        return findOrderById(orderId).isPresent();
    }

    /**
     * Returns how many orders are stored.
     *
     * @return how many orders are stored
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
     */
    public int getOrderCount() {
        return storage().count();
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
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
     */
    public List<String> updateOrder(int orderId, Order updated) {
        if (updated == null) {
            return List.of("No order was provided.");
        }
        Optional<Order> found = findOrderById(orderId);
        if (found.isEmpty()) {
            return List.of("Order #" + orderId + " was not found.");
        }
        Order original = found.get();
        Order cleaned = validator.normalize(updated);
        LocalDate today = getToday();
        // An old pickup date that hasn't changed is still allowed; a new date must be today or later.
        LocalDate earliest = original.getPickupDate().isBefore(today) ? original.getPickupDate() : today;
        List<String> errors = validator.validate(cleaned, earliest, today);
        boolean idChanged = cleaned.getOrderId() != orderId;
        if (idChanged && validator.isValidOrderId(cleaned.getOrderId()) && isIdTaken(cleaned.getOrderId())) {
            errors.add("Order #" + cleaned.getOrderId() + " already exists. Order IDs must be unique.");
        }
        if (errors.isEmpty() && !storage().update(orderId, cleaned)) {
            errors.add("The database did not update order #" + orderId + ".");
        }
        return errors;
    }

    /**
     * Changes ONE field of an order using exactly what the user typed.
     * The text is converted to the right data type here, then the whole
     * order is checked by {@link #updateOrder(int, Order)} before anything is
     * saved, so a bad value never changes the stored order. The unit tests use
     * this method to prove every one of the nine fields can be updated.
     *
     * <p>Field numbers:
     * <ol>
     *   <li>Order ID</li><li>Customer name</li><li>Phone number</li>
     *   <li>Cake type</li><li>Quantity</li><li>Unit price</li>
     *   <li>Pickup date</li><li>Picked up (yes/no)</li><li>Special instructions</li>
     * </ol>
     *
     * @param orderId     the ID of the order to change
     * @param fieldNumber which field to change (1-9)
     * @param newValue    the new value exactly as the user typed it
     * @return a list of problems; an EMPTY list means the field was updated
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
     */
    public List<String> updateField(int orderId, int fieldNumber, String newValue) {
        Optional<Order> found = findOrderById(orderId);
        if (found.isEmpty()) {
            return List.of("Order #" + orderId + " was not found.");
        }
        if (fieldNumber < 1 || fieldNumber > 9) {
            return List.of("Field number must be from 1 to 9.");
        }
        String value = (newValue == null) ? "" : newValue.trim();
        Order edited = found.get(); // findOrderById returns a copy, so editing it is safe

        // Convert the text to the field's data type. If it can't be converted, stop here.
        switch (fieldNumber) {
            case 1: {
                Optional<Integer> id = validator.parseWholeNumber(value);
                if (id.isEmpty()) {
                    return List.of(validator.ruleFor("orderId"));
                }
                edited.setOrderId(id.get());
                break;
            }
            case 2:
                edited.setCustomerName(value);
                break;
            case 3:
                edited.setPhoneNumber(value);
                break;
            case 4: {
                // Accept the cake's position in the list (1-4) or its name.
                Optional<String> cake = validator.matchCakeChoice(value);
                if (cake.isEmpty()) {
                    return List.of(validator.ruleFor("cakeType"));
                }
                edited.setCakeType(cake.get());
                break;
            }
            case 5: {
                Optional<Integer> quantity = validator.parseWholeNumber(value);
                if (quantity.isEmpty()) {
                    return List.of(validator.ruleFor("quantity"));
                }
                edited.setQuantity(quantity.get());
                break;
            }
            case 6: {
                Optional<Double> price = validator.parseMoney(value);
                if (price.isEmpty()) {
                    return List.of("Unit price must be a dollar amount such as 28 or 28.50.");
                }
                edited.setUnitPrice(price.get());
                break;
            }
            case 7: {
                Optional<LocalDate> date = validator.parseDate(value);
                if (date.isEmpty()) {
                    return List.of(validator.ruleFor("date"));
                }
                edited.setPickupDate(date.get());
                break;
            }
            case 8: {
                Optional<Boolean> pickedUp = validator.parseYesNo(value);
                if (pickedUp.isEmpty()) {
                    return List.of("Picked up must be Y or N (yes/no or true/false).");
                }
                edited.setPickedUp(pickedUp.get());
                break;
            }
            default:
                edited.setSpecialInstructions(value);
                break;
        }
        // Check every rule (range, uniqueness, etc.) before saving.
        return updateOrder(orderId, edited);
    }

    /**
     * Gives the name of a field number used by {@link #updateField(int, int, String)}.
     *
     * @param fieldNumber 1-9
     * @return the field's name, or "Unknown field" for any other number
     */
    public String describeField(int fieldNumber) {
        switch (fieldNumber) {
            case 1: return "Order ID";
            case 2: return "Customer name";
            case 3: return "Phone number";
            case 4: return "Cake type";
            case 5: return "Quantity";
            case 6: return "Unit price";
            case 7: return "Pickup date";
            case 8: return "Picked up";
            case 9: return "Special instructions";
            default: return "Unknown field";
        }
    }

    // ===============================================================
    // DELETE
    // ===============================================================

    /**
     * Removes the order with the given ID.
     *
     * @param orderId the order number to remove
     * @return true if an order was removed, false if no order had that ID
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
     */
    public boolean removeOrder(int orderId) {
        return storage().delete(orderId);
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
     * that date AND have not been picked up yet are counted; the database
     * query selects exactly those orders, and the totals are calculated here.
     *
     * @param date the pickup date to report on
     * @return the finished report (with zero orders if nothing is due)
     * @throws IllegalArgumentException if the date is null
     * @throws DataAccessException if no database is connected yet, or the database cannot be reached
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
        for (Order order : storage().findOpenOrdersDueOn(date)) {
            // Double-check the rule in case the storage returned extra rows.
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
}
