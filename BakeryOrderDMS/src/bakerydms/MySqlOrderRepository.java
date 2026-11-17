/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4); documented November 16, 2026 (Module 11)
 * Class:   MySqlOrderRepository
 *
 * Purpose: The data layer of the DMS. Stores orders in the "orders" table
 *          of a MySQL database (created by the bakery_dms.sql script) and
 *          runs every CRUD operation as SQL:
 *            Create  -> INSERT INTO orders ...
 *            Read    -> SELECT ... FROM orders
 *            Update  -> UPDATE orders SET ... WHERE order_id = ?
 *            Delete  -> DELETE FROM orders WHERE order_id = ?
 *            Report  -> SELECT ... WHERE pickup_date = ? AND is_picked_up = FALSE
 *
 *          Every query uses a PreparedStatement with ? placeholders, so text
 *          typed by the user can never change the SQL (no SQL injection).
 *          Every SQLException is turned into a DataAccessException with a
 *          plain-English message, so the GUI can show it instead of crashing.
 *
 *          Table and column names are fixed, because the user runs the
 *          provided script to create the database. The server address,
 *          username and password are never stored in the code: they come
 *          from the user through DatabaseConnector.
 */
package bakerydms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores orders in the {@code orders} table of a MySQL database (the data layer).
 *
 * <p>This is the {@link OrderRepository} the real program uses. Each method runs
 * one SQL statement:
 * <ul>
 *   <li>Create - {@code INSERT INTO orders ...}</li>
 *   <li>Read - {@code SELECT ... FROM orders}</li>
 *   <li>Update - {@code UPDATE orders SET ... WHERE order_id = ?}</li>
 *   <li>Delete - {@code DELETE FROM orders WHERE order_id = ?}</li>
 *   <li>Report - {@code SELECT ... WHERE pickup_date = ? AND is_picked_up = FALSE}</li>
 * </ul>
 * Every query is a {@link java.sql.PreparedStatement} with {@code ?} placeholders,
 * so text typed by the user can never change the SQL (no SQL injection).
 * Changes are saved as soon as each statement runs, so they can be seen right away
 * in MySQL Workbench or the MySQL command line.
 *
 * <p>Objects of this class are created by {@link DatabaseConnector} and used only
 * through {@link OrderManager}. The table and column names are fixed because the
 * {@code bakery_dms.sql} script creates them. Every {@link java.sql.SQLException}
 * is turned into a {@link DataAccessException} with a plain-English message.
 *
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 */
public class MySqlOrderRepository implements OrderRepository {

    /** The open connection to the user's MySQL database. */
    private final Connection connection;

    /** Shown in status messages, e.g. "bakery_dms on localhost:3306". */
    private final String description;

    /** The columns read for every order, in the order mapRow() expects them. */
    private final String columns = "order_id, customer_name, phone_number, cake_type, quantity, "
            + "unit_price, pickup_date, is_picked_up, special_instructions";

    /**
     * Creates the repository for an open connection.
     *
     * @param connection  an open connection to the bakery database
     * @param description where the database is, for status messages
     * @throws IllegalArgumentException if the connection is null
     */
    public MySqlOrderRepository(Connection connection, String description) {
        if (connection == null) {
            throw new IllegalArgumentException("A database connection is required.");
        }
        this.connection = connection;
        this.description = description;
    }

    /**
     * READ: every order in the table, sorted by order ID.
     *
     * @return all orders
     * @throws DataAccessException if the database cannot be reached or rejects the statement (the message explains why)
     */
    @Override
    public List<Order> findAll() {
        String sql = "SELECT " + columns + " FROM orders ORDER BY order_id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            return readAll(rows);
        } catch (SQLException e) {
            throw failure("read the orders", e);
        }
    }

    /**
     * READ: one order by its ID.
     *
     * @param orderId the order number to look for
     * @return the order, or empty if there is no such order
     * @throws DataAccessException if the database cannot be reached or rejects the statement (the message explains why)
     */
    @Override
    public Optional<Order> findById(int orderId) {
        String sql = "SELECT " + columns + " FROM orders WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapRow(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw failure("look up order #" + orderId, e);
        }
    }

    /**
     * CUSTOM ACTION data: open (not picked up) orders due on a date.
     *
     * @param pickupDate the pickup date
     * @return the matching orders, sorted by order ID
     * @throws DataAccessException if the database cannot be reached or rejects the statement (the message explains why)
     */
    @Override
    public List<Order> findOpenOrdersDueOn(LocalDate pickupDate) {
        String sql = "SELECT " + columns + " FROM orders "
                + "WHERE pickup_date = ? AND is_picked_up = FALSE ORDER BY order_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, pickupDate);
            try (ResultSet rows = statement.executeQuery()) {
                return readAll(rows);
            }
        } catch (SQLException e) {
            throw failure("read the orders for the report", e);
        }
    }

    /**
     * CREATE: inserts a new row.
     *
     * @param order the validated order
     * @return true if one row was inserted
     * @throws DataAccessException if the order ID is already used, a value breaks a database rule, or the database cannot be reached
     */
    @Override
    public boolean insert(Order order) {
        String sql = "INSERT INTO orders (" + columns + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int next = fillOrderValues(statement, order, 1);
            if (next != 10) {
                throw new DataAccessException("Internal error: wrong number of values for INSERT.");
            }
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw failure("save order #" + order.getOrderId(), e);
        }
    }

    /**
     * UPDATE: replaces every column of one row (so any field, including the
     * order ID, can be changed).
     *
     * @param orderId the ID the order has now
     * @param order   the new values
     * @return true if one row was changed
     * @throws DataAccessException if the new order ID is already used, a value breaks a database rule, or the database cannot be reached
     */
    @Override
    public boolean update(int orderId, Order order) {
        String sql = "UPDATE orders SET order_id = ?, customer_name = ?, phone_number = ?, cake_type = ?, "
                + "quantity = ?, unit_price = ?, pickup_date = ?, is_picked_up = ?, special_instructions = ? "
                + "WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int next = fillOrderValues(statement, order, 1);
            statement.setInt(next, orderId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw failure("update order #" + orderId, e);
        }
    }

    /**
     * DELETE: removes one row.
     *
     * @param orderId the order to remove
     * @return true if a row was removed
     * @throws DataAccessException if the database cannot be reached or rejects the statement (the message explains why)
     */
    @Override
    public boolean delete(int orderId) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
            statement.setInt(1, orderId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw failure("delete order #" + orderId, e);
        }
    }

    /**
     * Counts the stored orders.
     *
     * @return the number of rows in the orders table
     * @throws DataAccessException if the orders table is missing or the database cannot be reached
     */
    @Override
    public int count() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM orders");
             ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getInt(1) : 0;
        } catch (SQLException e) {
            throw failure("count the orders", e);
        }
    }

    /**
     * Returns where the database is, e.g. "bakery_dms on localhost:3306".
     *
     * @return where the database is, e.g. "bakery_dms on localhost:3306"
     */
    @Override
    public String describe() {
        return description;
    }

    /**
     * Closes the database connection.
     *
     * @return true if it closed without an error
     */
    @Override
    public boolean close() {
        try {
            connection.close();
            return true;
        } catch (SQLException e) {
            return false; // the program is closing or reconnecting anyway
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Puts an order's 9 values into a statement's ? placeholders, in column order.
     *
     * @param statement the INSERT or UPDATE statement
     * @param order     the order
     * @param first     the number of the first placeholder to fill
     * @return the number of the next unused placeholder
     * @throws SQLException if a value cannot be set
     */
    private int fillOrderValues(PreparedStatement statement, Order order, int first) throws SQLException {
        int i = first;
        statement.setInt(i++, order.getOrderId());
        statement.setString(i++, order.getCustomerName());
        statement.setString(i++, order.getPhoneNumber());
        statement.setString(i++, order.getCakeType());
        statement.setInt(i++, order.getQuantity());
        statement.setBigDecimal(i++, BigDecimal.valueOf(order.getUnitPrice()));
        statement.setObject(i++, order.getPickupDate());
        statement.setBoolean(i++, order.isPickedUp());
        statement.setString(i++, order.getSpecialInstructions());
        return i;
    }

    /**
     * Reads every row of a result into Order objects.
     *
     * @param rows the query result
     * @return the orders
     * @throws SQLException if a row cannot be read
     */
    private List<Order> readAll(ResultSet rows) throws SQLException {
        List<Order> orders = new ArrayList<>();
        while (rows.next()) {
            orders.add(mapRow(rows));
        }
        return orders;
    }

    /**
     * Turns the current row into an Order object.
     *
     * @param row a result positioned on a row
     * @return the order
     * @throws SQLException if a column cannot be read
     */
    private Order mapRow(ResultSet row) throws SQLException {
        String instructions = row.getString("special_instructions");
        return new Order(
                row.getInt("order_id"),
                row.getString("customer_name"),
                row.getString("phone_number"),
                row.getString("cake_type"),
                row.getInt("quantity"),
                row.getBigDecimal("unit_price").doubleValue(),
                row.getObject("pickup_date", LocalDate.class),
                row.getBoolean("is_picked_up"),
                instructions == null ? "" : instructions);
    }

    /**
     * Turns a database error into a plain-English DataAccessException.
     *
     * @param action what the program was trying to do, e.g. "save order #1026"
     * @param e      the error from the driver
     * @return the exception to throw
     */
    private DataAccessException failure(String action, SQLException e) {
        String reason;
        switch (e.getErrorCode()) {
            case 1062:
                reason = "That order ID is already in the database.";
                break;
            case 3819:
                reason = "The database rejected a value that breaks one of its rules.";
                break;
            case 1146:
                reason = "The \"orders\" table was not found. Run the bakery_dms.sql script, then connect again.";
                break;
            default:
                // Error codes 0 / SQLState 08xxx mean the connection itself was lost.
                String state = e.getSQLState() == null ? "" : e.getSQLState();
                reason = state.startsWith("08")
                        ? "The connection to the database was lost. Check that MySQL is running, then click Connect again."
                        : "The database reported: " + e.getMessage();
        }
        return new DataAccessException("Could not " + action + ". " + reason, e);
    }
}
