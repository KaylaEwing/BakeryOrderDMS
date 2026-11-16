/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    November 13, 2026 (Phase 4)
 * Class:   DatabaseConnector
 *
 * Purpose: Connects the program to the user's MySQL database. Nothing about
 *          the database's location or login is written in the code: the
 *          user types the server address, port, database name, username and
 *          password in the GUI, and this class:
 *            1. checks every value (blank, wrong format, impossible port...)
 *            2. builds the JDBC connection string by concatenation:
 *               "jdbc:mysql://" + host + ":" + port + "/" + database + options
 *            3. opens the connection and checks that the "orders" table exists
 *            4. explains any failure in plain English (wrong password,
 *               server not running, database missing...)
 *
 *          It never throws SQLException to the caller, so a bad connection
 *          attempt can never crash the program.
 */
package bakerydms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Checks connection details and opens the MySQL database.
 */
public class DatabaseConnector {

    /** The port MySQL uses unless the user changed it during installation. */
    private final int defaultPort = 3306;

    /** How long to wait for the server before giving up, in milliseconds. */
    private final int connectTimeoutMillis = 5000;

    /**
     * Extra connection settings added to the end of the connection string:
     * time limits so the window never freezes for long, UTF-8 text, and the
     * settings MySQL 8 needs for a password login without SSL certificates.
     */
    private final String options = "?connectTimeout=" + connectTimeoutMillis
            + "&socketTimeout=30000&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";

    /** @return the standard MySQL port, 3306 */
    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * Checks the connection details the user typed, before trying to connect.
     *
     * @param host     server address, e.g. localhost or 127.0.0.1 (may include ":port")
     * @param portText port number as typed (blank means 3306)
     * @param database database name, e.g. bakery_dms
     * @param username MySQL username
     * @param password MySQL password (may be blank: some accounts have none)
     * @return a list of problems; an EMPTY list means the details look valid
     */
    public List<String> validate(String host, String portText, String database, String username, char[] password) {
        List<String> problems = new ArrayList<>();
        String cleanHost = hostPart(host);
        if (cleanHost.isEmpty()) {
            problems.add("Server address is blank. Type localhost if MySQL is on this computer.");
        } else if (!cleanHost.matches("[A-Za-z0-9][A-Za-z0-9.-]{0,252}")) {
            problems.add("Server address \"" + shorten(cleanHost)
                    + "\" is not valid. Use a name like localhost or an address like 127.0.0.1.");
        }
        if (parsePort(host, portText).isEmpty()) {
            problems.add("Port must be a whole number from 1 to 65535 (MySQL normally uses 3306).");
        }
        String db = database == null ? "" : database.trim();
        if (db.isEmpty()) {
            problems.add("Database name is blank. The provided script creates a database named bakery_dms.");
        } else if (!db.matches("[A-Za-z0-9_$]{1,64}")) {
            problems.add("Database name \"" + shorten(db) + "\" can only use letters, digits, _ and $.");
        }
        String user = username == null ? "" : username.trim();
        if (user.isEmpty()) {
            problems.add("Username is blank. Type your MySQL username (for example root).");
        } else if (user.length() > 32 || user.chars().anyMatch(Character::isISOControl)) {
            problems.add("Username must be 1-32 characters.");
        }
        if (password != null && password.length > 128) {
            problems.add("Password is too long (over 128 characters).");
        }
        return problems;
    }

    /**
     * Builds the JDBC connection string from the user's details, by
     * concatenation. Example: jdbc:mysql://localhost:3306/bakery_dms?connectTimeout=5000...
     *
     * @param host     server address
     * @param port     port number
     * @param database database name
     * @return the connection string
     */
    public String buildUrl(String host, int port, String database) {
        return "jdbc:mysql://" + host + ":" + port + "/" + database + options;
    }

    /**
     * Connects to the database and checks that the orders table is there.
     *
     * @param host     server address (may include ":port")
     * @param portText port number as typed (blank means 3306)
     * @param database database name
     * @param username MySQL username
     * @param password MySQL password
     * @return a repository ready to use
     * @throws DataAccessException with a plain-English message if anything is wrong
     */
    public OrderRepository connect(String host, String portText, String database, String username, char[] password) {
        List<String> problems = validate(host, portText, database, username, password);
        if (!problems.isEmpty()) {
            throw new DataAccessException(String.join(" ", problems));
        }
        String cleanHost = hostPart(host);
        int port = parsePort(host, portText).orElse(defaultPort);
        String db = database.trim();
        String url = buildUrl(cleanHost, port, db);
        String where = db + " on " + cleanHost + ":" + port;

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username.trim(),
                    password == null ? "" : new String(password));
            OrderRepository repository = new MySqlOrderRepository(connection, where);
            repository.count(); // proves the "orders" table exists and can be read
            return repository;
        } catch (SQLException e) {
            closeQuietly(connection);
            throw new DataAccessException(explain(e, cleanHost, port, db, username.trim()), e);
        } catch (DataAccessException e) {
            closeQuietly(connection); // connected, but the table check failed
            throw e;
        }
    }

    /**
     * Turns a connection error into advice the user can act on.
     *
     * @param e        the error from the driver
     * @param host     server address
     * @param port     port
     * @param database database name
     * @param username username
     * @return the message to show
     */
    public String explain(SQLException e, String host, int port, String database, String username) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        String text = allMessages(e);
        // Some driver versions wrap the server's real reason (wrong password,
        // missing database) inside a general "cannot connect" error, so the
        // reason is also looked for in the text of every cause.
        int code = e.getErrorCode();
        if (text.contains("Unknown database")) {
            code = 1049;
        } else if (text.contains("Access denied for user") && text.contains("to database")) {
            code = 1044;
        } else if (text.contains("Access denied for user")) {
            code = 1045;
        }
        switch (code) {
            case 1045:
                return "Access denied for user \"" + username + "\". The username or password is incorrect.";
            case 1049:
                return "The database \"" + database + "\" does not exist on this server. "
                        + "Run the bakery_dms.sql script first, or check the database name.";
            case 1044:
                return "User \"" + username + "\" is not allowed to use the database \"" + database + "\".";
            default:
                break;
        }
        if (text.contains("No suitable driver")) {
            return "The MySQL driver is missing. Make sure the mysql-connector-j library is in the project (see README).";
        }
        if (hasCause(e, java.net.UnknownHostException.class)) {
            return "The server address \"" + host + "\" could not be found. Check the spelling (try localhost).";
        }
        if (state.startsWith("08")) {
            return "Could not reach a MySQL server at " + host + ":" + port
                    + ". Check the address and port, and that MySQL is running.";
        }
        return "Could not connect: " + (e.getMessage() == null ? "unknown error" : e.getMessage());
    }

    /**
     * Joins the messages of an error and all of its causes.
     *
     * @param error the error
     * @return every message in the chain, separated by " | "
     */
    private String allMessages(Throwable error) {
        StringBuilder text = new StringBuilder();
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t.getMessage() != null) {
                text.append(t.getMessage()).append(" | ");
            }
        }
        return text.toString();
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Removes spaces and any ":port" the user typed after the host name.
     *
     * @param host the address as typed
     * @return just the host name or IP address
     */
    private String hostPart(String host) {
        String h = host == null ? "" : host.trim();
        int colon = h.indexOf(':');
        return colon >= 0 ? h.substring(0, colon).trim() : h;
    }

    /**
     * Works out the port: the Port box if filled in, otherwise a ":port"
     * typed after the host, otherwise 3306.
     *
     * @param host     the address as typed
     * @param portText the Port box as typed
     * @return the port, or empty if what was typed is not a valid port
     */
    private Optional<Integer> parsePort(String host, String portText) {
        String p = portText == null ? "" : portText.trim();
        if (p.isEmpty() && host != null && host.contains(":")) {
            p = host.substring(host.indexOf(':') + 1).trim();
        }
        if (p.isEmpty()) {
            return Optional.of(defaultPort);
        }
        if (!p.matches("\\d{1,5}")) {
            return Optional.empty();
        }
        int port = Integer.parseInt(p);
        return (port >= 1 && port <= 65535) ? Optional.of(port) : Optional.empty();
    }

    /**
     * Checks whether an error was caused by a particular kind of problem.
     *
     * @param error the error
     * @param type  the kind of cause to look for
     * @return true if that cause appears anywhere in the chain
     */
    private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Closes a connection, ignoring errors (used after a failed attempt).
     *
     * @param connection the connection, or null
     * @return true if there was a connection to close
     */
    private boolean closeQuietly(Connection connection) {
        if (connection == null) {
            return false;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
            // nothing more can be done; the attempt already failed
        }
        return true;
    }

    /** @param text any text @return the text cut to 30 characters */
    private String shorten(String text) {
        return text.length() <= 30 ? text : text.substring(0, 27) + "...";
    }
}
