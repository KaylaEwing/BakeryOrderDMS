/*
 * Name:    Kayla Ewing
 * Course:  CEN 3024C - Software Development I
 * Date:    October 19, 2026
 * Class:   OrderFileLoader
 *
 * Purpose: Reads orders from a text file (the "batch" way of creating data).
 *          It checks the file name, opens the file safely, and turns each line
 *          into an Order object. It only handles the file format; business
 *          rules such as "quantity 1-50" are checked later by OrderValidator.
 *
 *          File format: one order per line, 9 fields separated by commas:
 *            orderId,customerName,phoneNumber,cakeType,quantity,unitPrice,pickupDate,pickedUp,specialInstructions
 *          Example:
 *            1001,Mia Chen,407-555-0101,Chocolate,2,25.00,2027-02-13,false,Write Happy Birthday
 *          Blank lines and lines starting with # are ignored. The last field
 *          (special instructions) may be empty and may contain commas.
 */
package bakerydms;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Loads Order objects from a delimited text file.
 */
public class OrderFileLoader {

    /** The character(s) that separate fields on each line, e.g. ",". */
    private final String delimiter;

    /** A line must have 8 fields (no instructions) or 9 fields (with instructions). */
    private final int minFields = 8;
    private final int maxFields = 9;

    /** Files larger than 5 MB are refused so the program cannot run out of memory. */
    private final long maxFileBytes = 5L * 1024 * 1024;

    /** Dates in the file must be written as YYYY-MM-DD. STRICT rejects 2027-02-30. */
    private final DateTimeFormatter fileDateFormat =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Creates a loader that splits lines on the given delimiter.
     *
     * @param delimiter the field separator, e.g. ","
     * @throws IllegalArgumentException if the delimiter is null or empty
     */
    public OrderFileLoader(String delimiter) {
        if (delimiter == null || delimiter.isEmpty()) {
            throw new IllegalArgumentException("A delimiter is required.");
        }
        this.delimiter = delimiter;
    }

    /**
     * Turns the text the user typed into a file path, rejecting blank names,
     * names with illegal characters, and files that are not .txt files.
     * Quotes around the path (added by Windows "Copy as path") are removed.
     *
     * @param rawPath the file name or path as typed
     * @return the file path to open
     * @throws IllegalArgumentException with a friendly message if the name is not usable
     */
    public Path resolvePath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            throw new IllegalArgumentException("The file name cannot be blank.");
        }
        String cleaned = rawPath.trim();
        // Remove surrounding quotes, e.g. "C:\Users\me\orders.txt"
        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("The file name cannot be blank.");
        }
        if (!cleaned.toLowerCase().endsWith(".txt")) {
            throw new IllegalArgumentException("Please choose a text file whose name ends in .txt (you entered \""
                    + shorten(cleaned) + "\").");
        }
        try {
            return Path.of(cleaned);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("\"" + shorten(cleaned) + "\" is not a valid file name.");
        }
    }

    /**
     * Reads every line of a text file. The file is read as UTF-8; if it was
     * saved in an older Windows encoding, it is read again as ISO-8859-1 so
     * the program still works instead of failing.
     *
     * @param filePath the file name or path as typed by the user
     * @return every line in the file, in order
     * @throws IllegalArgumentException if the file name itself is not usable
     * @throws IOException              if the file is missing, a folder, unreadable or too large
     */
    public List<String> readLines(String filePath) throws IOException {
        Path path = resolvePath(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + describe(path)
                    + System.lineSeparator() + "   Check the spelling, or enter the full path to the file.");
        }
        if (Files.isDirectory(path)) {
            throw new IOException("That is a folder, not a file: " + describe(path));
        }
        if (!Files.isReadable(path)) {
            throw new IOException("That file cannot be opened (check its permissions): " + describe(path));
        }
        if (Files.size(path) > maxFileBytes) {
            throw new IOException("That file is too large to be an order list (over 5 MB).");
        }
        try {
            return readAllLines(path, StandardCharsets.UTF_8);
        } catch (CharacterCodingException notUtf8) {
            // Not UTF-8 (e.g., saved in Windows-1252); every byte is valid in ISO-8859-1.
            return readAllLines(path, StandardCharsets.ISO_8859_1);
        }
    }

    /**
     * Reads all lines of a file using the given character set. The file is
     * closed automatically by try-with-resources, even if an error occurs.
     *
     * @param path    the file to read
     * @param charset how the file's bytes are turned into text
     * @return every line in the file
     * @throws IOException if the file cannot be read in that character set
     */
    private List<String> readAllLines(Path path, Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }
        // Notepad sometimes adds an invisible "byte order mark" at the start of the file; remove it.
        // It reads as one character in UTF-8, or as the three characters "\u00EF\u00BB\u00BF" in ISO-8859-1.
        if (!lines.isEmpty()) {
            String first = lines.get(0);
            if (first.startsWith("\uFEFF")) {
                lines.set(0, first.substring(1));
            } else if (first.startsWith("\u00EF\u00BB\u00BF")) {
                lines.set(0, first.substring(3));
            }
        }
        return lines;
    }

    /**
     * Checks whether a line should be skipped because it is empty or a comment.
     *
     * @param line one line from the file
     * @return true if the line is blank or starts with #
     */
    public boolean isBlankOrComment(String line) {
        return line == null || line.trim().isEmpty() || line.trim().startsWith("#");
    }

    /**
     * Turns one line of the file into an Order. This checks the FORMAT of
     * each field (is the quantity a number? is the date a real date?).
     *
     * @param line one line from the file
     * @return the Order described by the line
     * @throws IllegalArgumentException with a message naming the bad field
     */
    public Order parseLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException("the line is empty.");
        }
        // The limit keeps any extra commas inside the last field (special instructions).
        String[] parts = line.split(Pattern.quote(delimiter), maxFields);
        if (parts.length < minFields) {
            throw new IllegalArgumentException("expected " + maxFields + " fields separated by \""
                    + delimiter + "\" but found " + parts.length + ".");
        }
        int orderId = parseWholeNumber(parts[0], "order ID");
        String customerName = parts[1].trim();
        String phoneNumber = parts[2].trim();
        String cakeType = parts[3].trim();
        int quantity = parseWholeNumber(parts[4], "quantity");
        double unitPrice = parseMoney(parts[5]);
        LocalDate pickupDate = parseFileDate(parts[6]);
        boolean pickedUp = parseYesNo(parts[7]);
        String instructions = (parts.length == maxFields) ? parts[8].trim() : "";
        return new Order(orderId, customerName, phoneNumber, cakeType, quantity,
                unitPrice, pickupDate, pickedUp, instructions);
    }

    /**
     * Converts a field to a whole number.
     *
     * @param text      the field text
     * @param fieldName the field's name, used in the error message
     * @return the whole number
     * @throws IllegalArgumentException if the text is not a whole number
     */
    private int parseWholeNumber(String text, String fieldName) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " \"" + shorten(text.trim()) + "\" is not a whole number.");
        }
    }

    /**
     * Converts a price field such as "28.00" or "$28" to a number.
     *
     * @param text the field text
     * @return the price in dollars
     * @throws IllegalArgumentException if the text is not a plain dollar amount
     */
    private double parseMoney(String text) {
        String cleaned = text.trim();
        if (cleaned.startsWith("$")) {
            cleaned = cleaned.substring(1);
        }
        // Only plain amounts like 28 or 28.50 are allowed (no "NaN", "1e3" or negatives).
        if (!cleaned.matches("\\d{1,7}(\\.\\d+)?")) {
            throw new IllegalArgumentException("unit price \"" + shorten(text.trim()) + "\" is not a dollar amount.");
        }
        return Double.parseDouble(cleaned);
    }

    /**
     * Converts a date field written as YYYY-MM-DD.
     *
     * @param text the field text
     * @return the date
     * @throws IllegalArgumentException if the text is not a real YYYY-MM-DD date
     */
    private LocalDate parseFileDate(String text) {
        try {
            return LocalDate.parse(text.trim(), fileDateFormat);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("pickup date \"" + shorten(text.trim())
                    + "\" is not a real date in YYYY-MM-DD format.");
        }
    }

    /**
     * Converts the picked-up field. Accepts true/false, yes/no or y/n in any case.
     *
     * @param text the field text
     * @return true if the order has been picked up
     * @throws IllegalArgumentException if the text is not one of the accepted words
     */
    private boolean parseYesNo(String text) {
        String cleaned = text.trim().toLowerCase();
        if (cleaned.equals("true") || cleaned.equals("yes") || cleaned.equals("y")) {
            return true;
        }
        if (cleaned.equals("false") || cleaned.equals("no") || cleaned.equals("n")) {
            return false;
        }
        throw new IllegalArgumentException("picked-up value \"" + shorten(text.trim()) + "\" must be true or false.");
    }

    /**
     * Shows where the program looked for a file (its full path), shortened
     * if it is very long so the message stays readable.
     *
     * @param path the file path
     * @return the full path as text, at most about 120 characters
     */
    private String describe(Path path) {
        String full = path.toAbsolutePath().toString();
        return full.length() <= 120 ? full : "..." + full.substring(full.length() - 117);
    }

    /**
     * Shortens long text so error messages stay readable.
     *
     * @param text the text to shorten
     * @return the text, cut to 40 characters with "..." if it was longer
     */
    private String shorten(String text) {
        return text.length() <= 40 ? text : text.substring(0, 37) + "...";
    }
}
