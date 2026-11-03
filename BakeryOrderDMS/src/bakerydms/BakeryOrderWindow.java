/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 1, 2026 (Phase 3)
 * Class:   BakeryOrderWindow
 *
 * Purpose: The graphical user interface (GUI) of the Bakery Order
 *          Management System. It replaces the Phase 1 command-line menu and
 *          follows the layout planned in Module 4:
 *
 *            1. Header        - logo, title and the Exit button
 *            2. File bar      - load orders from a text file
 *            3. Order form    - every field of an order, with Add, Update,
 *                               Delete and Clear buttons (left side)
 *            4. Order table   - every order in the system, with search
 *            5. Report bar    - the custom action: Daily Production Report
 *            6. Status bar    - a message after every action
 *
 *          The window only collects what the user types and shows results.
 *          All checking and storing is done by OrderManager, the same class
 *          the JUnit tests check, so every rule from Phase 1 and 2 still applies.
 *
 *          Crash protection: every button runs inside safely(), which catches
 *          any unexpected error and shows a message instead of stopping the
 *          program. The window only closes when the user confirms Exit.
 *
 *          Note on void methods: Swing calls our code through listener
 *          methods such as actionPerformed() and windowClosing(), which Java
 *          requires to be void. Those listeners are one-line lambdas that
 *          call this class's own methods, and every one of those returns a value.
 */
package bakerydms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * The main window of the bakery program.
 */
public class BakeryOrderWindow {

    /** Does all the work: storing, checking, calculating. */
    private final OrderManager manager;
    /** Shortcut to the validation rules. */
    private final OrderValidator validator;
    /** Colors, fonts and styled components. */
    private final Theme theme = new Theme();
    /** The window itself. */
    private final JFrame frame = new JFrame("Bakery Order Manager");

    // ---- Table ----
    private final OrderTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<OrderTableModel> sorter;
    private final JLabel countLabel = new JLabel();

    // ---- Toolbar ----
    private final JTextField pathField;
    private final JTextField searchField;

    // ---- Order form ----
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JComboBox<String> cakeBox;
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JTextField priceField;
    private final JTextField dateField;
    private final JCheckBox pickedUpBox = new JCheckBox("Customer has picked up this order");
    private final JTextField notesField;
    private final JLabel modeLabel = new JLabel();
    private final JLabel formMessage = new JLabel(" ");

    // ---- Report bar and status bar ----
    private final JTextField reportDateField;
    private final JLabel statusLabel = new JLabel();

    /** The ID of the order being edited, or -1 when the form is for a new order. */
    private int selectedId = -1;

    /**
     * Creates the window and all its parts (it is not shown until show() is called).
     *
     * @param manager the order manager that stores and checks orders
     */
    public BakeryOrderWindow(OrderManager manager) {
        this.manager = manager;
        this.validator = manager.getValidator();
        this.tableModel = new OrderTableModel(manager);
        this.table = new JTable(tableModel);
        this.sorter = new TableRowSorter<>(tableModel);

        pathField = theme.textField(30);
        searchField = theme.textField(12);
        idField = theme.textField(10);
        nameField = theme.textField(18);
        phoneField = theme.textField(14);
        cakeBox = new JComboBox<>(validator.getValidCakeTypes().toArray(new String[0]));
        priceField = theme.textField(8);
        dateField = theme.textField(10);
        notesField = theme.textField(18);
        reportDateField = theme.textField(10);
        reportDateField.setText(manager.getToday().toString());
    }

    /**
     * Builds the layout and shows the window on screen.
     *
     * @return true once the window is visible
     */
    public boolean show() {
        frame.setIconImage(new CakeLogo().createImage(64));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // Clicking the window's X asks for confirmation instead of closing straight away.
        // (windowClosing is a Swing method that must be void; it only calls confirmExit.)
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                safely(BakeryOrderWindow.this::confirmExit);
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(theme.getCream());
        root.add(buildHeader(), BorderLayout.NORTH);

        // Left: the order form, full height. Right: file bar, table, report bar.
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.add(buildToolbar(), BorderLayout.NORTH);
        right.add(buildTableCard(), BorderLayout.CENTER);
        right.add(buildReportBar(), BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));
        body.add(buildFormCard(), BorderLayout.WEST);
        body.add(right, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setMinimumSize(new Dimension(1150, 690));
        // Use 1360 x 820, or the whole screen if the screen is smaller than that.
        java.awt.Rectangle screen = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        frame.setSize(Math.min(1360, screen.width), Math.min(820, screen.height));
        frame.setLocationRelativeTo(null); // center on screen
        if (screen.height < 820) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        clearForm();
        refreshTable();
        setStatus("Welcome! Load a text file of orders or add an order using the form.", true);
        frame.setVisible(true);
        return true;
    }

    // ===============================================================
    // BUILDING THE LAYOUT
    // ===============================================================

    /**
     * Zone 1: the dark header with the cake logo, title and Exit button.
     *
     * @return the header panel
     */
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(theme.getInk());
        header.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brand.setOpaque(false);
        brand.add(new JLabel(new ImageIcon(new CakeLogo().createImage(50))));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Bakery Order Manager");
        title.setFont(theme.title(26));
        title.setForeground(theme.getCream());
        JLabel subtitle = new JLabel("Custom cake orders  \u00b7  Daily production planning");
        subtitle.setFont(theme.body(Font.PLAIN, 14));
        subtitle.setForeground(theme.getPink());
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);
        brand.add(titles);
        header.add(brand, BorderLayout.WEST);

        JButton exit = theme.headerButton("Exit");
        exit.setToolTipText("Close the program (you will be asked to confirm)");
        exit.addActionListener(e -> safely(this::confirmExit));
        JPanel right = new JPanel(new GridBagLayout()); // centers the button vertically
        right.setOpaque(false);
        right.add(exit);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * Zone 2: the file bar for loading orders from a text file.
     *
     * @return the file bar
     */
    private JComponent buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setBackground(theme.getCard());
        bar.setBorder(theme.cardBorder(10));
        bar.add(theme.fieldLabel("Load orders from a text file:"));
        pathField.setToolTipText("Type a file name such as orders.txt, a full path, or click Browse");
        pathField.addActionListener(e -> safely(this::loadFile)); // Enter key loads the file
        bar.add(pathField);
        JButton browse = theme.secondaryButton("Browse\u2026");
        browse.addActionListener(e -> safely(this::browseForFile));
        bar.add(browse);
        JButton loadButton = theme.primaryButton("Load File");
        loadButton.addActionListener(e -> safely(this::loadFile));
        bar.add(loadButton);
        return bar;
    }

    /**
     * Zone 3: the order form. Each field has a label, and a hint where
     * the format matters, so the user knows what to type before they type it.
     *
     * @return the form card
     */
    private JComponent buildFormCard() {
        JPanel cardPanel = new JPanel(new BorderLayout(0, 12));
        cardPanel.setBackground(theme.getCard());
        cardPanel.setBorder(theme.cardBorder(16));
        cardPanel.setPreferredSize(new Dimension(380, 0));

        // Title row: "Order Details" and a pill showing NEW ORDER or EDITING #1001
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(theme.heading("Order Details"), BorderLayout.WEST);
        modeLabel.setFont(theme.body(Font.BOLD, 12));
        modeLabel.setOpaque(true);
        modeLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        titleRow.add(modeLabel, BorderLayout.EAST);
        cardPanel.add(titleRow, BorderLayout.NORTH);

        // Make the quantity spinner and the combo box match the text fields.
        quantitySpinner.setFont(theme.body(Font.PLAIN, 14));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) quantitySpinner.getEditor();
        editor.getTextField().setColumns(4);
        editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
        cakeBox.setFont(theme.body(Font.PLAIN, 14));
        cakeBox.setBackground(Color.WHITE);
        pickedUpBox.setFont(theme.body(Font.PLAIN, 13));
        pickedUpBox.setOpaque(false);
        pickedUpBox.setForeground(theme.getInk());

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        int row = 0;
        row = addField(fields, row, "Order ID", idField, "1000\u20139999");
        row = addField(fields, row, "Customer name", nameField, "First and last");
        row = addField(fields, row, "Phone number", phoneField, "10 digits");
        row = addField(fields, row, "Cake type", cakeBox, null);
        row = addField(fields, row, "Quantity", quantitySpinner, "1\u201350 cakes");
        row = addField(fields, row, "Unit price ($)", priceField, "e.g. 28.00");
        row = addField(fields, row, "Pickup date", dateField, "YYYY-MM-DD");
        pickedUpBox.setText("Picked up by customer");
        row = addField(fields, row, "Status", pickedUpBox, null);
        row = addField(fields, row, "Instructions", notesField, "Optional");
        // Filler that pushes the fields to the top of the card
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row;
        filler.weighty = 1;
        fields.add(Box.createVerticalGlue(), filler);
        cardPanel.add(fields, BorderLayout.CENTER);

        // Buttons and a one-line message under the form
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        JPanel buttons = new JPanel(new java.awt.GridLayout(2, 2, 8, 8));
        buttons.setOpaque(false);
        JButton add = theme.primaryButton("Add Order");
        add.setToolTipText("Add the order in the form as a new order");
        add.addActionListener(e -> safely(this::addOrder));
        JButton update = theme.blueButton("Update Order");
        update.setToolTipText("Save changes to the order selected in the table");
        update.addActionListener(e -> safely(this::updateOrder));
        JButton delete = theme.dangerButton("Delete Order");
        delete.setToolTipText("Delete the order whose ID is in the Order ID box");
        delete.addActionListener(e -> safely(this::deleteOrder));
        JButton clear = theme.secondaryButton("Clear Form");
        clear.setToolTipText("Empty the form to start a new order");
        clear.addActionListener(e -> safely(this::clearForm));
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(clear);
        bottom.add(buttons, BorderLayout.NORTH);
        formMessage.setFont(theme.body(Font.BOLD, 12));
        bottom.add(formMessage, BorderLayout.CENTER);
        cardPanel.add(bottom, BorderLayout.SOUTH);
        return cardPanel;
    }

    /**
     * Adds one labeled field to the form. A short hint (such as the format
     * to use) is shown in small gray text under the label, so the user knows
     * what to type before they type it.
     *
     * @param panel the form panel (GridBagLayout)
     * @param row   the grid row to use
     * @param label the field's label text
     * @param field the input component
     * @param hint  small gray help text, or null for none
     * @return the next free grid row
     */
    private int addField(JPanel panel, int row, String label, JComponent field, String hint) {
        JPanel labelBox = new JPanel();
        labelBox.setOpaque(false);
        labelBox.setLayout(new BoxLayout(labelBox, BoxLayout.Y_AXIS));
        labelBox.add(theme.fieldLabel(label));
        if (hint != null) {
            labelBox.add(theme.hint(hint));
        }
        if (field.getToolTipText() == null && hint != null) {
            field.setToolTipText(label + ": " + hint);
        }

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 12);
        panel.add(labelBox, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        panel.add(field, c);
        return row + 1;
    }

    /**
     * Zone 4: the table that displays every order.
     *
     * @return the table card
     */
    private JComponent buildTableCard() {
        JPanel cardPanel = new JPanel(new BorderLayout(0, 10));
        cardPanel.setBackground(theme.getCard());
        cardPanel.setBorder(theme.cardBorder(16));

        JPanel titleRow = new JPanel(new BorderLayout(12, 0));
        titleRow.setOpaque(false);
        JPanel titleLeft = new JPanel();
        titleLeft.setOpaque(false);
        titleLeft.setLayout(new BoxLayout(titleLeft, BoxLayout.Y_AXIS));
        titleLeft.add(theme.heading("All Orders"));
        countLabel.setFont(theme.body(Font.PLAIN, 12));
        countLabel.setForeground(theme.getMuted());
        titleLeft.add(countLabel);
        titleRow.add(titleLeft, BorderLayout.WEST);

        // Search box: filters the table by order ID, customer name or cake type.
        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        search.setOpaque(false);
        searchField.setToolTipText("Order ID, customer name or cake type");
        searchField.addActionListener(e -> safely(() -> searchOrders() >= 0)); // Enter key searches
        search.add(searchField);
        JButton searchButton = theme.secondaryButton("Search");
        searchButton.addActionListener(e -> safely(() -> searchOrders() >= 0));
        search.add(searchButton);
        JButton showAll = theme.secondaryButton("Show All");
        showAll.setToolTipText("Clear the search and display every order");
        showAll.addActionListener(e -> safely(() -> showAllOrders() >= 0));
        search.add(showAll);
        titleRow.add(search, BorderLayout.EAST);
        cardPanel.add(titleRow, BorderLayout.NORTH);

        table.setRowSorter(sorter);
        // Money columns are shown as text ("$53.50"); sort them by their number instead.
        Comparator<String> byMoney = Comparator.comparingDouble(text ->
                validator.parseMoney(text.replace(",", "")).orElse(0.0));
        sorter.setComparator(5, byMoney);
        sorter.setComparator(8, byMoney);

        table.setFont(theme.body(Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setGridColor(theme.getCreamDark());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(theme.getCard());
        table.setDefaultRenderer(Object.class, new OrderCellRenderer());
        table.setDefaultRenderer(Integer.class, new OrderCellRenderer());
        table.setToolTipText("Click an order to load it into the form for updating or deleting");

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 32));
        header.setDefaultRenderer(new HeaderRenderer());

        // Column widths: the minimums stop IDs, phones and dates from being cut off;
        // Special Instructions takes whatever room is left.
        int[] widths = {56, 130, 112, 92, 46, 72, 98, 86, 82, 190};
        int[] minimums = {52, 90, 108, 84, 44, 68, 94, 82, 76, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(minimums[i]);
        }

        // Clicking a row loads that order into the form.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                safely(this::fillFormFromSelection);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(theme.getLine()));
        scroll.getViewport().setBackground(theme.getCard());
        cardPanel.add(scroll, BorderLayout.CENTER);
        return cardPanel;
    }

    /**
     * Zone 5: the custom action, the Daily Production Report.
     *
     * @return the report bar
     */
    private JComponent buildReportBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(theme.getPink());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.getRaspberry(), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = theme.heading("Daily Production Report");
        JLabel hint = theme.hint("Cakes to bake and expected revenue, incl. 7% tax");
        hint.setFont(theme.body(Font.PLAIN, 12));
        text.add(title);
        text.add(hint);
        bar.add(text, BorderLayout.CENTER); // CENTER shrinks instead of overlapping the controls

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        controls.setOpaque(false);
        controls.add(theme.fieldLabel("Pickup date:"));
        reportDateField.setToolTipText("YYYY-MM-DD or MM/DD/YYYY");
        reportDateField.addActionListener(e -> safely(this::generateReport)); // Enter key runs it
        controls.add(reportDateField);
        JButton generate = theme.primaryButton("Generate Report");
        generate.addActionListener(e -> safely(this::generateReport));
        controls.add(generate);
        bar.add(controls, BorderLayout.EAST);
        return bar;
    }

    /**
     * Zone 6: the status bar that reports the result of every action.
     *
     * @return the status bar
     */
    private JComponent buildStatusBar() {
        statusLabel.setFont(theme.body(Font.BOLD, 13));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return statusLabel;
    }

    // ===============================================================
    // ACTIONS (each returns true if it succeeded)
    // ===============================================================

    /**
     * Loads orders from the file named in the file box.
     * Blank names, missing files, folders and non-.txt files are reported, not crashed on.
     *
     * @return true if at least one order was loaded
     */
    public boolean loadFile() {
        String path = pathField.getText().trim();
        if (path.isEmpty()) {
            pathField.setBorder(theme.fieldBorder(true));
            pathField.requestFocusInWindow();
            setStatus("Please type a file name (for example orders.txt) or click Browse.", false);
            JOptionPane.showMessageDialog(frame,
                    "The file name is blank.\nType a file name such as orders.txt, or click Browse to pick a file.",
                    "No File Chosen", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        pathField.setBorder(theme.fieldBorder(false));
        int before = manager.getOrderCount();
        List<String> messages = manager.loadFromFile(path);
        refreshTable();
        int added = manager.getOrderCount() - before;
        String summary = messages.isEmpty() ? "" : messages.get(0);

        if (summary.startsWith("Could not load")) {
            pathField.setBorder(theme.fieldBorder(true));
            setStatus(summary, false);
            JOptionPane.showMessageDialog(frame, wrap(summary), "File Not Loaded", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        setStatus(summary, added > 0);
        if (messages.size() > 1) {
            // Some lines were skipped: list them in a scrollable box.
            JTextArea details = new JTextArea(String.join(System.lineSeparator(), messages));
            details.setEditable(false);
            details.setFont(theme.mono(12));
            details.setLineWrap(true);
            details.setWrapStyleWord(true);
            JScrollPane scroll = new JScrollPane(details);
            scroll.setPreferredSize(new Dimension(640, 260));
            JOptionPane.showMessageDialog(frame, scroll, "File Loaded With Skipped Lines", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, summary, "File Loaded", JOptionPane.INFORMATION_MESSAGE);
        }
        return added > 0;
    }

    /**
     * Opens a file picker that shows only .txt files and puts the chosen path in the file box.
     *
     * @return true if a file was chosen
     */
    public boolean browseForFile() {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle("Choose an order file");
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            setStatus("No file chosen.", true);
            return false;
        }
        pathField.setText(chooser.getSelectedFile().getAbsolutePath());
        pathField.setBorder(theme.fieldBorder(false));
        setStatus("File chosen. Click Load File to load its orders.", true);
        return true;
    }

    /**
     * Shows only the orders whose ID, customer name or cake type contains the search text.
     *
     * @return the number of orders shown
     */
    public int searchOrders() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            return showAllOrders();
        }
        // Pattern.quote makes symbols like "(" safe to search for; (?i) ignores upper/lower case.
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0, 1, 3));
        int shown = table.getRowCount();
        updateCountLabel();
        setStatus(shown == 0
                ? "No orders match \"" + shorten(text) + "\". Click Show All to see every order."
                : shown + " order(s) match \"" + shorten(text) + "\".", shown > 0);
        return shown;
    }

    /**
     * Removes the search filter so every order is displayed.
     *
     * @return the number of orders shown
     */
    public int showAllOrders() {
        searchField.setText("");
        sorter.setRowFilter(null);
        int shown = refreshTable();
        setStatus("Showing all " + shown + " order(s).", true);
        return shown;
    }

    /**
     * Adds the order in the form as a new order. New orders always start
     * as "not picked up".
     *
     * @return true if the order was added
     */
    public boolean addOrder() {
        List<String> problems = new ArrayList<>();
        Optional<Order> order = readForm(problems);
        if (order.isEmpty()) {
            return showProblems("The order was not added", problems);
        }
        Order o = order.get();
        List<String> errors = manager.addOrder(o.getOrderId(), o.getCustomerName(), o.getPhoneNumber(),
                o.getCakeType(), o.getQuantity(), o.getUnitPrice(), o.getPickupDate(), o.getSpecialInstructions());
        if (!errors.isEmpty()) {
            return showProblems("The order was not added", errors);
        }
        sorter.setRowFilter(null);
        searchField.setText("");
        refreshTable();
        selectOrder(o.getOrderId()); // highlights the new row and loads it into the form
        setStatus("Order #" + o.getOrderId() + " for " + o.getCustomerName() + " was added. Total: "
                + money(manager.calculateTotal(manager.findOrderById(o.getOrderId()).orElse(o))), true);
        showFormMessage("Order #" + o.getOrderId() + " added.", true);
        return true;
    }

    /**
     * Saves the form's values over the order selected in the table. Any
     * field can be changed, including the Order ID.
     *
     * @return true if the order was updated
     */
    public boolean updateOrder() {
        if (selectedId < 0) {
            return showProblems("Nothing to update",
                    List.of("Click an order in the table first, then change its fields and press Update Order."));
        }
        List<String> problems = new ArrayList<>();
        Optional<Order> order = readForm(problems);
        if (order.isEmpty()) {
            return showProblems("Order #" + selectedId + " was not updated", problems);
        }
        int oldId = selectedId;
        List<String> errors = manager.updateOrder(oldId, order.get());
        if (!errors.isEmpty()) {
            return showProblems("Order #" + oldId + " was not updated", errors);
        }
        int newId = order.get().getOrderId();
        refreshTable();
        selectOrder(newId);
        setStatus("Order #" + newId + (newId != oldId ? " (was #" + oldId + ")" : "") + " was updated.", true);
        showFormMessage("Changes saved.", true);
        return true;
    }

    /**
     * Deletes the order whose ID is in the Order ID box, after the user confirms.
     *
     * @return true if an order was deleted
     */
    public boolean deleteOrder() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            return showProblems("Nothing to delete",
                    List.of("Type an Order ID in the Order ID box, or click an order in the table."));
        }
        Optional<Integer> id = validator.parseWholeNumber(idText);
        if (id.isEmpty()) {
            return showProblems("Nothing was deleted", List.of("\"" + shorten(idText) + "\" is not an order ID. "
                    + validator.ruleFor("orderId")));
        }
        Optional<Order> found = manager.findOrderById(id.get());
        if (found.isEmpty()) {
            return showProblems("Nothing was deleted", List.of("No order #" + id.get() + " exists."));
        }
        Order o = found.get();
        int answer = JOptionPane.showConfirmDialog(frame,
                "Delete order #" + o.getOrderId() + "?\n\n"
                        + o.getCustomerName() + "  \u00b7  " + o.getQuantity() + " x " + o.getCakeType()
                        + "  \u00b7  pickup " + o.getPickupDate() + "\n\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            setStatus("Delete cancelled. Order #" + o.getOrderId() + " was kept.", true);
            return false;
        }
        boolean removed = manager.removeOrder(o.getOrderId());
        refreshTable();
        clearForm();
        setStatus(removed
                ? "Order #" + o.getOrderId() + " was deleted. " + manager.getOrderCount() + " order(s) remain."
                : "Order #" + o.getOrderId() + " could not be deleted.", removed);
        return removed;
    }

    /**
     * Empties the form and switches it to "new order" mode.
     *
     * @return true when done
     */
    public boolean clearForm() {
        selectedId = -1;
        table.clearSelection();
        idField.setText("");
        nameField.setText("");
        phoneField.setText("");
        cakeBox.setSelectedIndex(0);
        quantitySpinner.setValue(1);
        priceField.setText("");
        dateField.setText("");
        pickedUpBox.setSelected(false);
        pickedUpBox.setEnabled(false); // new orders always start as not picked up
        notesField.setText("");
        resetFieldBorders();
        showFormMessage(" ", true);
        setMode("NEW ORDER", theme.getCreamDark(), theme.getInk());
        idField.requestFocusInWindow();
        return true;
    }

    /**
     * Loads the order selected in the table into the form for editing.
     *
     * @return true if an order was loaded
     */
    public boolean fillFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return false;
        }
        Optional<Order> found = tableModel.getOrderAt(table.convertRowIndexToModel(viewRow));
        if (found.isEmpty()) {
            return false;
        }
        Order o = found.get();
        selectedId = o.getOrderId();
        idField.setText(String.valueOf(o.getOrderId()));
        nameField.setText(o.getCustomerName());
        phoneField.setText(o.getPhoneNumber());
        cakeBox.setSelectedItem(o.getCakeType());
        quantitySpinner.setValue(o.getQuantity());
        priceField.setText(String.format("%.2f", o.getUnitPrice()));
        dateField.setText(o.getPickupDate().toString());
        pickedUpBox.setEnabled(true);
        pickedUpBox.setSelected(o.isPickedUp());
        notesField.setText(o.getSpecialInstructions());
        resetFieldBorders();
        showFormMessage("Change any field, then click Update Order.", true);
        setMode("EDITING #" + o.getOrderId(), theme.getPink(), theme.getRaspberry());
        return true;
    }

    /**
     * CUSTOM ACTION: generates the Daily Production Report for the date in
     * the report box and shows the calculation in a window.
     *
     * @return true if a report was shown
     */
    public boolean generateReport() {
        String text = reportDateField.getText().trim();
        Optional<LocalDate> date = validator.parseDate(text);
        if (date.isEmpty()) {
            reportDateField.setBorder(theme.fieldBorder(true));
            setStatus("\"" + shorten(text) + "\" is not a real date. Use YYYY-MM-DD (2027-02-13) or MM/DD/YYYY.", false);
            JOptionPane.showMessageDialog(frame,
                    (text.isEmpty() ? "The report date is blank." : "\"" + shorten(text) + "\" is not a real date.")
                            + "\nPlease type a date like 2027-02-13 or 2/13/2027.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        reportDateField.setBorder(theme.fieldBorder(false));
        DailyReport report = manager.generateDailyReport(date.get());
        JOptionPane.showMessageDialog(frame, buildReportPanel(report),
                "Daily Production Report \u2013 " + report.getReportDate(), JOptionPane.PLAIN_MESSAGE);
        setStatus("Report for " + report.getReportDate() + ": " + report.getTotalCakes() + " cake(s) from "
                + report.getOrderCount() + " open order(s); expected revenue " + money(report.getExpectedRevenue()) + ".", true);
        return true;
    }

    /**
     * Asks the user to confirm, then closes the program.
     *
     * @return false if the user chose to stay (if they confirm, the program ends)
     */
    public boolean confirmExit() {
        int answer = JOptionPane.showConfirmDialog(frame,
                "Exit the Bakery Order Manager?\nOrders are kept in memory only, so they will be cleared.",
                "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            frame.dispose();
            System.exit(0);
        }
        setStatus("Exit cancelled.", true);
        return false;
    }

    // ===============================================================
    // HELPERS
    // ===============================================================

    /**
     * Reads every form field, converts it to the right data type and checks
     * it. Anything wrong (letters in the ID, a bad phone number, a date that
     * doesn't exist...) is added to the problems list and its field is
     * outlined in red. Rules that need the stored orders (unique ID, date not
     * in the past) are checked afterwards by OrderManager.
     *
     * @param problems a list that receives a message for each problem
     * @return the order built from the form, or empty if any field could not be read
     */
    private Optional<Order> readForm(List<String> problems) {
        resetFieldBorders();
        String idText = idField.getText().trim();
        Optional<Integer> id = validator.parseWholeNumber(idText);
        if (id.isEmpty()) {
            problems.add(idText.isEmpty() ? "Order ID is blank. " + validator.ruleFor("orderId")
                    : "Order ID \"" + shorten(idText) + "\" is not a whole number. " + validator.ruleFor("orderId"));
            markError(idField);
        }
        // Check the text fields here too, so the user sees every problem at once.
        if (id.isPresent() && !validator.isValidOrderId(id.get())) {
            problems.add(validator.ruleFor("orderId"));
            markError(idField);
        }
        if (!validator.isValidName(nameField.getText())) {
            problems.add(validator.ruleFor("name"));
            markError(nameField);
        }
        if (!validator.isValidPhone(phoneField.getText())) {
            problems.add(validator.ruleFor("phone"));
            markError(phoneField);
        }
        // Read what is typed in the quantity box, even if the spinner hasn't accepted it yet.
        String qtyText = ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField().getText().trim();
        Optional<Integer> quantity = validator.parseWholeNumber(qtyText);
        if (quantity.isEmpty() || !validator.isValidQuantity(quantity.get())) {
            problems.add(validator.ruleFor("quantity"));
            markError(quantitySpinner);
        }
        String priceText = priceField.getText().trim();
        Optional<Double> price = validator.parseMoney(priceText);
        if (price.isEmpty()) {
            problems.add(priceText.isEmpty() ? "Unit price is blank. Type a price like 28.00."
                    : "Unit price \"" + shorten(priceText) + "\" is not a dollar amount. Type a number like 28 or 28.50.");
            markError(priceField);
        } else if (!validator.isValidPrice(price.get())) {
            problems.add(validator.ruleFor("price"));
            markError(priceField);
        }
        String dateText = dateField.getText().trim();
        Optional<LocalDate> date = validator.parseDate(dateText);
        if (date.isEmpty()) {
            problems.add(dateText.isEmpty() ? "Pickup date is blank. Type a date like 2027-02-13."
                    : "Pickup date \"" + shorten(dateText) + "\" is not a real date. Use YYYY-MM-DD or MM/DD/YYYY.");
            markError(dateField);
        }
        if (!validator.isValidInstructions(notesField.getText())) {
            problems.add(validator.ruleFor("instructions"));
            markError(notesField);
        }
        if (!problems.isEmpty()) {
            return Optional.empty();
        }
        Object cake = cakeBox.getSelectedItem();
        return Optional.of(new Order(id.get(), nameField.getText(), phoneField.getText(),
                cake == null ? "" : cake.toString(), quantity.get(), price.get(), date.get(),
                pickedUpBox.isSelected(), notesField.getText()));
    }

    /**
     * Shows a list of problems in a message window and the status bar, and
     * outlines the matching fields in red.
     *
     * @param title    what failed, e.g. "The order was not added"
     * @param problems the reasons
     * @return always false, so callers can "return showProblems(...)"
     */
    private boolean showProblems(String title, List<String> problems) {
        highlightFields(problems);
        StringBuilder html = new StringBuilder("<html><div style='width:380px'><b>" + escape(title) + "</b><ul>");
        for (String p : problems) {
            html.append("<li style='margin-bottom:4px'>").append(escape(p)).append("</li>");
        }
        html.append("</ul>Fields with a problem are outlined in red.</div></html>");
        showFormMessage("Please fix the fields outlined in red.", false);
        setStatus(title + ": " + problems.get(0), false);
        JOptionPane.showMessageDialog(frame, html.toString(), "Please Check the Order", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    /**
     * Outlines in red every field that an error message is about.
     *
     * @param problems the error messages from the validator or manager
     * @return how many fields were marked
     */
    private int highlightFields(List<String> problems) {
        Map<String, JComponent> fieldFor = new LinkedHashMap<>();
        fieldFor.put("Order ID", idField);
        fieldFor.put("already exists", idField);
        fieldFor.put("Customer name", nameField);
        fieldFor.put("Phone", phoneField);
        fieldFor.put("Quantity", quantitySpinner);
        fieldFor.put("Unit price", priceField);
        fieldFor.put("Pickup date", dateField);
        fieldFor.put("Special instructions", notesField);
        int marked = 0;
        for (String problem : problems) {
            for (Map.Entry<String, JComponent> entry : fieldFor.entrySet()) {
                if (problem.contains(entry.getKey())) {
                    markError(entry.getValue());
                    marked++;
                }
            }
        }
        return marked;
    }

    /**
     * Outlines one field in red.
     *
     * @param field the field with a problem
     * @return the same field
     */
    private JComponent markError(JComponent field) {
        field.setBorder(theme.fieldBorder(true));
        return field;
    }

    /**
     * Puts every field's normal border back.
     *
     * @return the number of fields reset
     */
    private int resetFieldBorders() {
        JComponent[] fields = {idField, nameField, phoneField, quantitySpinner, priceField, dateField, notesField};
        for (JComponent field : fields) {
            field.setBorder(theme.fieldBorder(false));
        }
        return fields.length;
    }

    /**
     * Shows a short message under the form buttons.
     *
     * @param text    the message
     * @param success true for green, false for red
     * @return the message shown
     */
    private String showFormMessage(String text, boolean success) {
        formMessage.setForeground(success ? theme.getGreen() : theme.getError());
        formMessage.setText(text);
        return text;
    }

    /**
     * Updates the pill next to "Order Details".
     *
     * @param text       NEW ORDER or EDITING #id
     * @param background pill color
     * @param foreground text color
     * @return the text shown
     */
    private String setMode(String text, Color background, Color foreground) {
        modeLabel.setText(text);
        modeLabel.setBackground(background);
        modeLabel.setForeground(foreground);
        return text;
    }

    /**
     * Shows a message in the status bar: green for success, red for a problem.
     *
     * @param message the message
     * @param success true if the action worked
     * @return the message shown
     */
    private String setStatus(String message, boolean success) {
        statusLabel.setText((success ? "\u2714  " : "\u26a0  ") + message);
        statusLabel.setBackground(success ? new Color(0xE3EFE5) : new Color(0xF9E0DE));
        statusLabel.setForeground(success ? theme.getGreen() : theme.getError());
        return message;
    }

    /**
     * Reloads the table from the manager and updates the order count.
     *
     * @return the number of orders in the system
     */
    private int refreshTable() {
        int count = tableModel.refresh();
        updateCountLabel();
        return count;
    }

    /**
     * Updates the "Showing X of Y orders" label above the table.
     *
     * @return the label text
     */
    private String updateCountLabel() {
        int total = manager.getOrderCount();
        int shown = table.getRowCount();
        String text = (shown == total ? total + " order(s)" : "Showing " + shown + " of " + total + " orders")
                + "  \u00b7  click a row to edit it";
        countLabel.setText(text);
        return text;
    }

    /**
     * Selects an order's row in the table and scrolls to it.
     *
     * @param orderId the order to select
     * @return true if the order was found in the table
     */
    private boolean selectOrder(int orderId) {
        int modelRow = tableModel.rowOf(orderId);
        if (modelRow < 0) {
            return false;
        }
        int viewRow = table.convertRowIndexToView(modelRow);
        if (viewRow < 0) {
            return false; // hidden by the current search
        }
        table.setRowSelectionInterval(viewRow, viewRow);
        table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
        return true;
    }

    /**
     * Builds the contents of the report window: a bar for each cake type
     * and the money calculation underneath.
     *
     * @param report the calculated report
     * @return the panel to show
     */
    private JComponent buildReportPanel(DailyReport report) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(theme.getCard());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        String day = report.getReportDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
        JLabel title = new JLabel("<html><span style='font-size:15pt'><b>" + day + ", " + report.getReportDate()
                + "</b></span><br>" + report.getOrderCount() + " open order(s) due \u00b7 picked-up orders are not counted</html>");
        title.setFont(theme.body(Font.PLAIN, 13));
        title.setForeground(theme.getInk());
        panel.add(title, BorderLayout.NORTH);

        if (!report.hasOrders()) {
            JLabel none = new JLabel("No open orders are due on this date. Nothing to bake.");
            none.setFont(theme.body(Font.BOLD, 14));
            none.setForeground(theme.getMuted());
            panel.add(none, BorderLayout.CENTER);
            return panel;
        }

        // One row per cake type: name, a bar sized by the count, and the count.
        JPanel bars = new JPanel(new GridBagLayout());
        bars.setOpaque(false);
        int most = 1;
        for (int count : report.getCakeCounts().values()) {
            most = Math.max(most, count);
        }
        int row = 0;
        for (Map.Entry<String, Integer> entry : report.getCakeCounts().entrySet()) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = row++;
            c.insets = new Insets(4, 0, 4, 10);
            c.anchor = GridBagConstraints.WEST;
            c.gridx = 0;
            bars.add(theme.fieldLabel(entry.getKey()), c);
            c.gridx = 1;
            JPanel bar = new JPanel();
            bar.setBackground(entry.getValue() == 0 ? theme.getCreamDark() : theme.getRaspberry());
            bar.setPreferredSize(new Dimension(Math.max(4, 260 * entry.getValue() / most), 18));
            bars.add(bar, c);
            c.gridx = 2;
            JLabel count = new JLabel(entry.getValue() + (entry.getValue() == 1 ? " cake" : " cakes"));
            count.setFont(theme.body(Font.BOLD, 14));
            count.setForeground(theme.getInk());
            bars.add(count, c);
        }
        panel.add(bars, BorderLayout.CENTER);

        // The calculation: total cakes, subtotal, tax, expected revenue
        JPanel totals = new JPanel(new GridBagLayout());
        totals.setBackground(theme.getCream());
        totals.setBorder(theme.cardBorder(12));
        String taxLabel = String.format("Sales tax (%.0f%%)", manager.getTaxRate() * 100);
        String[][] lines = {
            {"Total cakes to bake", String.valueOf(report.getTotalCakes())},
            {"Subtotal", money(report.getSubtotal())},
            {taxLabel, money(report.getTax())},
            {"Expected revenue", money(report.getExpectedRevenue())},
        };
        for (int i = 0; i < lines.length; i++) {
            boolean last = i == lines.length - 1;
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = i;
            c.insets = new Insets(3, 0, 3, 0);
            c.gridx = 0;
            c.weightx = 1;
            c.anchor = GridBagConstraints.WEST;
            JLabel name = new JLabel(lines[i][0]);
            name.setFont(theme.body(last ? Font.BOLD : Font.PLAIN, last ? 16 : 14));
            name.setForeground(theme.getInk());
            totals.add(name, c);
            c.gridx = 1;
            c.weightx = 0;
            c.anchor = GridBagConstraints.EAST;
            JLabel value = new JLabel(lines[i][1]);
            value.setFont(theme.body(Font.BOLD, last ? 18 : 14));
            value.setForeground(last ? theme.getRaspberry() : theme.getInk());
            totals.add(value, c);
        }
        panel.add(totals, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(460, panel.getPreferredSize().height));
        return panel;
    }

    /**
     * Runs a button's action and catches any unexpected error, so the
     * program shows a message instead of crashing.
     *
     * @param action the action to run
     * @return the action's result, or false if it failed
     */
    private boolean safely(BooleanSupplier action) {
        try {
            return action.getAsBoolean();
        } catch (RuntimeException e) {
            setStatus("Something unexpected went wrong (" + e.getClass().getSimpleName() + "). No data was lost.", false);
            JOptionPane.showMessageDialog(frame,
                    "Something unexpected went wrong, but no data was lost.\nDetails: " + e.getClass().getSimpleName(),
                    "Unexpected Problem", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /** @param amount dollars @return the amount as text, e.g. "$1,234.50" */
    private String money(double amount) {
        return String.format("$%,.2f", amount);
    }

    /** @param text any text @return the text cut to 30 characters so messages stay short */
    private String shorten(String text) {
        return text.length() <= 30 ? text : text.substring(0, 27) + "...";
    }

    /** @param text a long message @return the message with line breaks so the dialog isn't too wide */
    private String wrap(String text) {
        return text.replace(". ", ".\n");
    }

    /**
     * Makes text safe to put inside the HTML used for labels (so a name like
     * "<b>" typed by a user is shown as text, not treated as formatting).
     *
     * @param text the text
     * @return the escaped text
     */
    private String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ===============================================================
    // TABLE RENDERERS (how cells and headers are drawn)
    // ===============================================================

    /**
     * Draws table cells: striped rows, pink selection, money aligned right,
     * and picked-up orders in gray italics so open orders stand out.
     */
    @SuppressWarnings("serial") // Swing renderers are never saved to disk
    private class OrderCellRenderer extends DefaultTableCellRenderer {

        /**
         * Styles one cell before it is drawn.
         *
         * @return the component Swing draws for this cell
         */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(t, value, selected, false, row, column);
            int modelRow = t.convertRowIndexToModel(row);
            boolean pickedUp = tableModel.getOrderAt(modelRow).map(Order::isPickedUp).orElse(false);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setFont(theme.body(pickedUp ? Font.ITALIC : Font.PLAIN, 13));
            if (selected) {
                setBackground(theme.getPink());
                setForeground(theme.getRaspberry());
            } else {
                setBackground(row % 2 == 0 ? theme.getCard() : theme.getCream());
                setForeground(pickedUp ? new Color(0x9A8B80) : theme.getInk());
            }
            int modelColumn = t.convertColumnIndexToModel(column);
            if (modelColumn == 5 || modelColumn == 8) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (modelColumn == 0 || modelColumn == 4) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            setToolTipText(value == null ? null : value.toString());
            return this;
        }
    }

    /**
     * Draws the table's column headers in the dark ink color.
     */
    @SuppressWarnings("serial") // Swing renderers are never saved to disk
    private class HeaderRenderer extends DefaultTableCellRenderer {

        /**
         * Styles one header cell before it is drawn.
         *
         * @return the component Swing draws for this header
         */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(t, value, false, false, row, column);
            setBackground(theme.getInk());
            setForeground(theme.getCream());
            setFont(theme.body(Font.BOLD, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            int modelColumn = t.convertColumnIndexToModel(column);
            setHorizontalAlignment(modelColumn == 5 || modelColumn == 8 ? SwingConstants.RIGHT
                    : modelColumn == 0 || modelColumn == 4 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return this;
        }
    }
}
