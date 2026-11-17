/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4); documented November 16, 2026 (Module 11)
 * Class:   InMemoryOrderRepository (test helper)
 *
 * Purpose: A stand-in for the MySQL database, used only by the unit tests.
 *          It stores orders in a list but behaves like the database: it
 *          hands out copies, so a test can only change data through
 *          OrderManager. Because OrderManager talks to the OrderRepository
 *          interface, the business-logic tests run on any computer without a
 *          MySQL server, and prove the same rules the real program uses.
 */
package bakerydms;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A stand-in for the MySQL database, used only by the unit tests.
 *
 * <p>It implements {@link OrderRepository} with a list, but behaves like the
 * database: it hands out copies, and it refuses a duplicate order ID like a
 * primary key does. Because {@link OrderManager} only talks to the interface,
 * the business-logic tests run on any computer without a MySQL server and
 * prove the same rules the real program uses.
 *
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 */
public class InMemoryOrderRepository implements OrderRepository {

    /** The stored orders. */
    private final List<Order> orders = new ArrayList<>();

    /**
     * Creates an empty in-memory store, like a database with an empty orders table.
     */
    public InMemoryOrderRepository() {
        // the list starts empty
    }

    /**
     * Returns copies of every order, sorted by ID.
     *
     * @return copies of every order, sorted by ID
     */
    @Override
    public List<Order> findAll() {
        List<Order> copies = new ArrayList<>();
        for (Order order : orders) {
            copies.add(order.copy());
        }
        copies.sort(Comparator.comparingInt(Order::getOrderId));
        return copies;
    }

    /**
     * Finds one stored order by its ID.
     *
     * @param orderId the ID
     * @return a copy of the order, or empty
     */
    @Override
    public Optional<Order> findById(int orderId) {
        return orders.stream().filter(o -> o.getOrderId() == orderId).findFirst().map(Order::copy);
    }

    /**
     * Finds the orders due on a date that have not been picked up yet.
     *
     * @param pickupDate the date
     * @return copies of the open orders due that day
     */
    @Override
    public List<Order> findOpenOrdersDueOn(LocalDate pickupDate) {
        List<Order> due = new ArrayList<>();
        for (Order order : orders) {
            if (order.getPickupDate().equals(pickupDate) && !order.isPickedUp()) {
                due.add(order.copy());
            }
        }
        return due;
    }

    /**
     * Stores a copy of a new order. Like the database primary key, a duplicate order ID is refused.
     *
     * @param order the order
     * @return true if stored (false if the ID is already used, like a primary key)
     */
    @Override
    public boolean insert(Order order) {
        if (findById(order.getOrderId()).isPresent()) {
            return false;
        }
        return orders.add(order.copy());
    }

    /**
     * Replaces a stored order with new values.
     *
     * @param orderId current ID
     * @param order new values
     * @return true if a stored order was replaced
     */
    @Override
    public boolean update(int orderId, Order order) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderId() == orderId) {
                orders.set(i, order.copy());
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the order with the given ID.
     *
     * @param orderId the ID
     * @return true if an order was removed
     */
    @Override
    public boolean delete(int orderId) {
        return orders.removeIf(o -> o.getOrderId() == orderId);
    }

    /**
     * Returns the number of stored orders.
     *
     * @return the number of stored orders
     */
    @Override
    public int count() {
        return orders.size();
    }

    /**
     * Returns a description for status messages.
     *
     * @return a description for status messages
     */
    @Override
    public String describe() {
        return "test storage (in memory)";
    }

    /**
     * Closes the storage. Memory needs no closing, so this always succeeds.
     *
     * @return true (nothing to close)
     */
    @Override
    public boolean close() {
        return true;
    }
}
