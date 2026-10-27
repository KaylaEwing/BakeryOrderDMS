/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 26, 2026 (Phase 2)
 * Class:   TestHelper
 *
 * Purpose: Shared set-up code for the unit tests. It builds an OrderManager
 *          whose clock is FIXED at November 1, 2026, so date rules ("pickup
 *          date can't be in the past") give the same result no matter what
 *          day the tests are run. It also creates "dummy" orders so each test
 *          can start with exactly the data it needs.
 */
package bakerydms;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Builds the objects and sample data used by the unit tests.
 */
public class TestHelper {

    /** "Today" for every test. */
    private final LocalDate today = LocalDate.of(2026, 11, 1);

    /** The date used for the Daily Production Report tests (a Saturday). */
    private final LocalDate reportDate = LocalDate.of(2027, 2, 13);

    /** @return the fixed date the tests treat as today (2026-11-01) */
    public LocalDate getToday() {
        return today;
    }

    /** @return the pickup date used in report tests (2027-02-13) */
    public LocalDate getReportDate() {
        return reportDate;
    }

    /**
     * Creates a validator with the bakery's four cake types.
     *
     * @return a new OrderValidator
     */
    public OrderValidator newValidator() {
        return new OrderValidator(List.of("Chocolate", "Vanilla", "Red Velvet", "Carrot"));
    }

    /**
     * Creates an empty OrderManager whose "today" is always 2026-11-01
     * and whose sales tax rate is 7%.
     *
     * @return a new, empty OrderManager
     */
    public OrderManager newManager() {
        ZoneId zone = ZoneId.systemDefault();
        Clock fixedClock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone);
        OrderValidator validator = newValidator();
        return new OrderManager(validator, new OrderFileLoader(",", validator), 0.07, fixedClock);
    }

    /**
     * Creates a valid dummy order. Only the values that matter for a test
     * are parameters; the rest are fixed, valid sample values.
     *
     * @param orderId    the order number
     * @param cakeType   the cake type
     * @param quantity   number of cakes
     * @param unitPrice  price of one cake
     * @param pickupDate pickup date
     * @param pickedUp   whether the order has been collected
     * @return a new Order
     */
    public Order order(int orderId, String cakeType, int quantity, double unitPrice,
                       LocalDate pickupDate, boolean pickedUp) {
        return new Order(orderId, "Test Customer", "407-555-0100", cakeType, quantity,
                unitPrice, pickupDate, pickedUp, "");
    }

    /**
     * Creates a manager that already holds the five Valentine's orders from
     * orders.txt plus one order for the next day. Used by the update,
     * remove and report tests.
     *
     *   1001 Chocolate  x2 @ $25  2027-02-13  open
     *   1002 Vanilla    x1 @ $22  2027-02-13  open
     *   1003 Red Velvet x3 @ $28  2027-02-13  open
     *   1004 Chocolate  x1 @ $25  2027-02-13  open
     *   1005 Carrot     x2 @ $24  2027-02-13  PICKED UP (must not be counted in the report)
     *   1006 Red Velvet x1 @ $28  2027-02-14  open (different day)
     *
     * @return a manager holding the six sample orders
     */
    public OrderManager managerWithSampleOrders() {
        OrderManager manager = newManager();
        manager.addOrder(order(1001, "Chocolate", 2, 25.00, reportDate, false));
        manager.addOrder(order(1002, "Vanilla", 1, 22.00, reportDate, false));
        manager.addOrder(order(1003, "Red Velvet", 3, 28.00, reportDate, false));
        manager.addOrder(order(1004, "Chocolate", 1, 25.00, reportDate, false));
        manager.addOrder(order(1005, "Carrot", 2, 24.00, reportDate, true));
        manager.addOrder(order(1006, "Red Velvet", 1, 28.00, reportDate.plusDays(1), false));
        return manager;
    }
}
