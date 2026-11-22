package com.student;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Gutenberg Book Explorer
 * Java 17+ and JSoup. Uses fixed-size arrays as required by the assignment.
 */
public class App {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String TOP_BOOKS_URL =
            "https://www.gutenberg.org/browse/scores/top";
    private static final String SEARCH_URL =
            "https://www.gutenberg.org/ebooks/search/?query=";

    // Fixed-size arrays: no ArrayList is used.
    private static final Book[] catalog = new Book[100];
    private static final Book[] readingList = new Book[20];
    private static int catalogCount = 0;
    private static int readingCount = 0;

    public static void main(String[] args) {
        System.out.println("Welcome to Gutenberg Book Explorer!");
        System.out.println("This application uses the Project Gutenberg website.");
        runMenu();
        scanner.close();
    }

    private static void runMenu() {
        int choice;
        do {
            printMenu();
            choice = readInt("Enter your choice: ");

            if (choice == 1) {
                exploreBooks();
            } else if (choice == 2) {
                System.out.print("Enter a book title: ");
                searchBook(scanner.nextLine().trim());
            } else if (choice == 3) {
                System.out.print("Enter an author name: ");
                searchBook(scanner.nextLine().trim(), true);
            } else if (choice == 4) {
                addBook();
            } else if (choice == 5) {
                viewReadingList();
            } else if (choice == 6) {
                removeBook();
            } else if (choice == 7) {
                displayBookDetails();
            } else if (choice == 0) {
                System.out.println("Thank you for using Gutenberg Book Explorer. Goodbye!");
            } else {
                System.out.println("Invalid choice. Please select a number from 0 to 7.");
            }
        } while (choice != 0);
    }

    private static void printMenu() {
        System.out.println("\n===== GUTENBERG BOOK EXPLORER =====");
        System.out.println("1. Explore Books");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author");
        System.out.println("4. Add Book to Reading List");
        System.out.println("5. View Reading List");
        System.out.println("6. Remove Book");
        System.out.println("7. Display Book Details");
        System.out.println("0. Exit");
    }

    // Part B: retrieve at least 10 books from the Project Gutenberg top-books page.
    private static void exploreBooks() {
        System.out.println("\nLoading books from Project Gutenberg...");
        Book[] found = fetchBooks(TOP_BOOKS_URL, 30);

        if (found.length == 0) {
            System.out.println("No books were retrieved. Check your internet connection and try again.");
            return;
        }

        catalogCount = 0;
        for (Book book : found) {
            addToCatalog(book);
        }
        System.out.println("\nBooks found: " + catalogCount);
        printBooks(catalog, catalogCount);
    }

    // Required overload 1: search by title.
    private static void searchBook(String title) {
        if (title.isBlank()) {
            System.out.println("Search text cannot be empty.");
            return;
        }
        searchAndDisplay(title, false);
    }

    // Required overload 2: search by author when true, otherwise by title.
    private static void searchBook(String keyword, boolean searchByAuthor) {
        if (keyword.isBlank()) {
            System.out.println("Search text cannot be empty.");
            return;
        }
        searchAndDisplay(keyword, searchByAuthor);
    }

    private static void searchAndDisplay(String keyword, boolean byAuthor) {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        System.out.println("\nSearching Project Gutenberg...");
        Book[] results = fetchBooks(SEARCH_URL + encoded, 50);
        catalogCount = 0;

        for (Book book : results) {
            boolean matches = byAuthor
                    ? book.author.toLowerCase().contains(keyword.toLowerCase())
                    : book.title.toLowerCase().contains(keyword.toLowerCase());

            // Gutenberg's search endpoint may match either title or author.
            // Apply the requested field filter locally for accurate results.
            if (matches) {
                addToCatalog(book);
            }
        }

        if (catalogCount == 0) {
            System.out.println("No matching books found. Try another keyword or use Explore Books.");
        } else {
            System.out.println("\nSearch results: " + catalogCount);
            printBooks(catalog, catalogCount);
        }
    }

    // Connects to Gutenberg, parses book links, and avoids duplicate/non-book links.
    private static Book[] fetchBooks(String url, int limit) {
        Book[] results = new Book[limit];
        int count = 0;

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; StudentBookExplorer/1.0)")
                    .timeout(15000)
                    .get();

            Elements links = doc.select("a[href^=/ebooks/]");
            for (Element link : links) {
                String href = link.attr("href");
                if (!href.matches("/ebooks/\\d+/?")) {
                    continue;
                }

                String title = link.select(".title").text();
                if (title.isBlank()) {
                    title = link.text().trim();
                }
                if (title.isBlank()) {
                    continue;
                }

                String id = href.replaceAll("/+$", "");
                boolean duplicate = false;
                for (int i = 0; i < count; i++) {
                    if (results[i].url.endsWith(id)) {
                        duplicate = true;
                        break;
                    }
                }
                if (duplicate) {
                    continue;
                }

                String author = extractAuthor(link);
                String fullUrl = "https://www.gutenberg.org" + id;
                results[count++] = new Book(title, author, fullUrl);

                if (count == limit) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Connection or parsing error: " + e.getMessage());
        }

        Book[] compact = new Book[count];
        for (int i = 0; i < count; i++) {
            compact[i] = results[i];
        }
        return compact;
    }

    private static String extractAuthor(Element link) {
        // Gutenberg search results commonly provide author information in subtitle/author spans.
        Element parent = link.parent();
        String author = link.select(".subtitle, .author, .authors").text();

        if (author.isBlank() && parent != null) {
            author = parent.select(".subtitle, .author, .authors").text();
        }
        if (author.isBlank() && parent != null && parent.tagName().equals("li")) {
            String parentText = parent.text();
            String linkText = link.text();
            if (parentText.startsWith(linkText)) {
                author = parentText.substring(linkText.length())
                        .replaceFirst("^\\s*[-(,:;]*\\s*", "")
                        .replaceFirst("\\s*[)]\\s*$", "")
                        .trim();
            }
        }
        return author.isBlank() ? "Author information not listed" : author;
    }

    private static void addToCatalog(Book book) {
        if (catalogCount >= catalog.length || containsBook(catalog, catalogCount, book.url)) {
            return;
        }
        catalog[catalogCount++] = book;
    }

    private static boolean containsBook(Book[] books, int count, String url) {
        for (int i = 0; i < count; i++) {
            if (books[i] != null && books[i].url.equals(url)) {
                return true;
            }
        }
        return false;
    }

    private static void printBooks(Book[] books, int count) {
        for (int i = 0; i < count; i++) {
            System.out.println((i + 1) + ". " + books[i].title);
            System.out.println("   Author: " + books[i].author);
            System.out.println("   URL: " + books[i].url);
            System.out.println("----------------------------------------");
        }
    }

    private static void addBook() {
        if (catalogCount == 0) {
            System.out.println("The catalog is empty. Choose Explore Books or Search first.");
            return;
        }
        if (readingCount == readingList.length) {
            System.out.println("Your reading list is full (maximum 20 books).");
            return;
        }

        printBooks(catalog, catalogCount);
        int number = readInt("Enter the book number to add: ");
        if (number < 1 || number > catalogCount) {
            System.out.println("Invalid book number.");
            return;
        }

        Book selected = catalog[number - 1];
        if (containsBook(readingList, readingCount, selected.url)) {
            System.out.println("This book is already in your reading list.");
        } else {
            readingList[readingCount++] = selected;
            System.out.println("Book added successfully: " + selected.title);
        }
    }

    private static void viewReadingList() {
        if (readingCount == 0) {
            System.out.println("Your reading list is empty.");
            return;
        }
        System.out.println("\n===== MY READING LIST =====");
        // Required for loop to display saved books.
        for (int i = 0; i < readingCount; i++) {
            System.out.println((i + 1) + ". " + readingList[i].title);
            System.out.println("   Author: " + readingList[i].author);
            System.out.println("   URL: " + readingList[i].url);
        }
    }

    private static void removeBook() {
        if (readingCount == 0) {
            System.out.println("Your reading list is empty. Nothing to remove.");
            return;
        }

        viewReadingList();
        int number = readInt("Enter the number of the book to remove: ");
        if (number < 1 || number > readingCount) {
            System.out.println("Invalid book number.");
            return;
        }

        String removedTitle = readingList[number - 1].title;
        for (int i = number - 1; i < readingCount - 1; i++) {
            readingList[i] = readingList[i + 1];
        }
        readingList[--readingCount] = null;
        System.out.println("Removed successfully: " + removedTitle);
    }

    private static void displayBookDetails() {
        if (catalogCount == 0) {
            System.out.println("No books are currently displayed. Explore or search first.");
            return;
        }

        printBooks(catalog, catalogCount);
        int number = readInt("Enter the book number for details: ");
        if (number < 1 || number > catalogCount) {
            System.out.println("Invalid book number.");
            return;
        }

        Book book = catalog[number - 1];
        System.out.println("\n===== BOOK DETAILS =====");
        System.out.println("Title : " + book.title);
        System.out.println("Author: " + book.author);
        System.out.println("URL   : " + book.url);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static class Book {
        private final String title;
        private final String author;
        private final String url;

        private Book(String title, String author, String url) {
            this.title = title;
            this.author = author;
            this.url = url;
        }
    }
}
