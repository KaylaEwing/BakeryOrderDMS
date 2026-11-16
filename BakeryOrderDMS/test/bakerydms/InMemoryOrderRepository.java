/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4)
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
 * Keeps orders in memory for testing.
 */
public class InMemoryOrderRepository implements OrderRepository {

    /** The stored orders. */
    private final List<Order> orders = new ArrayList<>();

    /** @return copies of every order, sorted by ID */
    @Override
    public List<Order> findAll() {
        List<Order> copies = new ArrayList<>();
        for (Order order : orders) {
            copies.add(order.copy());
        }
        copies.sort(Comparator.comparingInt(Order::getOrderId));
        return copies;
    }

    /** @param orderId the ID @return a copy of the order, or empty */
    @Override
    public Optional<Order> findById(int orderId) {
        return orders.stream().filter(o -> o.getOrderId() == orderId).findFirst().map(Order::copy);
    }

    /** @param pickupDate the date @return copies of the open orders due that day */
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

    /** @param order the order @return true if stored (false if the ID is already used, like a primary key) */
    @Override
    public boolean insert(Order order) {
        if (findById(order.getOrderId()).isPresent()) {
            return false;
        }
        return orders.add(order.copy());
    }

    /** @param orderId current ID @param order new values @return true if a stored order was replaced */
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

    /** @param orderId the ID @return true if an order was removed */
    @Override
    public boolean delete(int orderId) {
        return orders.removeIf(o -> o.getOrderId() == orderId);
    }

    /** @return the number of stored orders */
    @Override
    public int count() {
        return orders.size();
    }

    /** @return a description for status messages */
    @Override
    public String describe() {
        return "test storage (in memory)";
    }

    /** @return true (nothing to close) */
    @Override
    public boolean close() {
        return true;
    }
}
