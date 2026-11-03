/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 1, 2026 (Phase 3)
 * Class:   OrderTableModel
 *
 * Purpose: Connects the list of orders to the JTable on screen. The table
 *          asks this model how many rows there are and what goes in each
 *          cell. The table is read-only: all changes go through the form so
 *          they are always validated by OrderManager first.
 */
package bakerydms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.table.AbstractTableModel;

/**
 * Supplies order data to the JTable.
 */
@SuppressWarnings("serial") // the table model is never saved to disk
public class OrderTableModel extends AbstractTableModel {

    /** Column titles, in the order they appear. */
    private final String[] columns = {
        "ID", "Customer", "Phone", "Cake", "Qty", "Price", "Pickup", "Status", "Total", "Special Instructions"
    };

    /** The orders currently shown (copies, sorted by ID). */
    private final List<Order> rows = new ArrayList<>();

    /** Used to work out each order's total with tax. */
    private final OrderManager manager;

    /**
     * Creates the model.
     *
     * @param manager the order manager, used to calculate totals
     */
    public OrderTableModel(OrderManager manager) {
        this.manager = manager;
    }

    /**
     * Reloads every order from the manager and redraws the table.
     *
     * @return the number of orders now shown
     */
    public int refresh() {
        rows.clear();
        rows.addAll(manager.getAllOrders());
        fireTableDataChanged();
        return rows.size();
    }

    /**
     * Gets the order shown in a row.
     *
     * @param modelRow the row number in the model (not the sorted view)
     * @return the order, or empty if the row number is out of range
     */
    public Optional<Order> getOrderAt(int modelRow) {
        if (modelRow < 0 || modelRow >= rows.size()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(modelRow));
    }

    /**
     * Finds which row shows an order.
     *
     * @param orderId the order number
     * @return the model row, or -1 if that order is not shown
     */
    public int rowOf(int orderId) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getOrderId() == orderId) {
                return i;
            }
        }
        return -1;
    }

    /** @return the number of rows */
    @Override
    public int getRowCount() {
        return rows.size();
    }

    /** @return the number of columns */
    @Override
    public int getColumnCount() {
        return columns.length;
    }

    /**
     * @param column column number
     * @return the column's title
     */
    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    /**
     * Tells the table each column's data type, so numbers sort as numbers.
     *
     * @param column column number
     * @return the class of the values in that column
     */
    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
            case 4:
                return Integer.class;
            default:
                return String.class;
        }
    }

    /**
     * Gives the value to show in one cell. Money is formatted with a $ sign.
     *
     * @param row    row number
     * @param column column number
     * @return the cell's value
     */
    @Override
    public Object getValueAt(int row, int column) {
        Order order = rows.get(row);
        switch (column) {
            case 0: return order.getOrderId();
            case 1: return order.getCustomerName();
            case 2: return order.getPhoneNumber();
            case 3: return order.getCakeType();
            case 4: return order.getQuantity();
            case 5: return String.format("$%.2f", order.getUnitPrice());
            case 6: return order.getPickupDate().toString();
            case 7: return order.getStatus();
            case 8: return String.format("$%,.2f", manager.calculateTotal(order));
            default: return order.getSpecialInstructions().isEmpty() ? "-" : order.getSpecialInstructions();
        }
    }
}
