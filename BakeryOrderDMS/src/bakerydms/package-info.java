/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 16, 2026 (Module 11)
 * Class:   package-info (documentation for the bakerydms package)
 *
 * Purpose: Describes the whole package for the generated Javadoc.
 */

/**
 * The Bakery Order Management System: a Swing program that manages custom cake
 * orders for a small bakery and stores them in a MySQL database.
 *
 * <p>The program has three layers. Each layer only talks to the one below it:
 * <table class="striped">
 *   <caption>Layers and their classes</caption>
 *   <thead>
 *     <tr><th scope="col">Layer</th><th scope="col">Classes</th><th scope="col">Job</th></tr>
 *   </thead>
 *   <tbody>
 *     <tr><th scope="row">Presentation</th>
 *         <td>{@link bakerydms.BakeryApp}, {@link bakerydms.BakeryOrderWindow},
 *             {@link bakerydms.OrderTableModel}, {@link bakerydms.Theme}, {@link bakerydms.CakeLogo}</td>
 *         <td>Starts the program, shows the window and reads what the user types</td></tr>
 *     <tr><th scope="row">Business logic</th>
 *         <td>{@link bakerydms.OrderManager}, {@link bakerydms.OrderValidator},
 *             {@link bakerydms.Order}, {@link bakerydms.DailyReport}</td>
 *         <td>Checks every value, runs add / update / delete, and builds the
 *             Daily Production Report</td></tr>
 *     <tr><th scope="row">Data</th>
 *         <td>{@link bakerydms.OrderRepository}, {@link bakerydms.MySqlOrderRepository},
 *             {@link bakerydms.DatabaseConnector}, {@link bakerydms.DataAccessException}</td>
 *         <td>Connects to MySQL with the details the user enters and runs the SQL</td></tr>
 *   </tbody>
 * </table>
 *
 * <p><b>How an action flows:</b> the user clicks Add Order, then
 * {@link bakerydms.BakeryOrderWindow} reads the form, then
 * {@link bakerydms.OrderManager} validates it with {@link bakerydms.OrderValidator},
 * then {@link bakerydms.MySqlOrderRepository} runs {@code INSERT INTO orders},
 * and the window refreshes the table and shows the result in the status bar.
 *
 * <p><b>Setup:</b> run the {@code bakery_dms.sql} script to create the
 * {@code bakery_dms} database with 25 sample orders. Then start
 * {@link bakerydms.BakeryApp} and enter the MySQL server, port, database,
 * username and password in the window.
 *
 * @author Kayla Ewing
 * @version 4.0 (Phase 4, Module 11)
 */
package bakerydms;
