# Gutenberg Book Explorer

**Course:** Java Programming — Assignment 2  
**Lecturer:** KAMATE KATENDE TIMOTHEE

A Java console application that connects to Project Gutenberg with JSoup, explores books, searches by title or author, and manages a personal reading list.

## Requirements

- JDK 17 or later
- Apache Maven
- Internet connection

## Features

- Explore books from Project Gutenberg's top-books page
- Search books by title
- Search books by author
- Add a selected book to a personal reading list
- View and remove saved books
- Display title, author, and Gutenberg URL
- Input validation and connection-error handling

## Required concepts included

- Fixed-size arrays (100 catalog entries and 20 reading-list entries); no `ArrayList`
- `do-while` menu loop and `for` loops
- `if/else` conditions and input validation
- User-defined methods
- Overloaded `searchBook(String)` and `searchBook(String, boolean)` methods
- JSoup HTML retrieval and parsing
- `Scanner` for console input

## Run in NetBeans

1. Extract the ZIP file.
2. Open NetBeans.
3. Select **File → Open Project** and choose the `GutenbergBookExplorer` folder.
4. Wait for Maven to download JSoup.
5. Run `App.java`.

## Run in VS Code / Terminal

Open a terminal in this project folder and run:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.student.App"
```

If the `exec:java` command is unavailable, run the project from NetBeans, or add the Maven Exec Plugin to `pom.xml`.

## GitHub upload

1. Create a new GitHub repository named `GutenbergBookExplorer`.
2. Extract this ZIP file on your computer.
3. Upload the **contents** of the `GutenbergBookExplorer` folder (including `pom.xml`, `src`, and `README.md`) to the repository.
4. Commit the files.

## Notes

- The application needs an active internet connection to retrieve live book data.
- Project Gutenberg may change its HTML structure. If its page changes, the JSoup selectors in `App.java` may need updating.
- Author details are displayed when present in the returned HTML; otherwise the program shows `Author information not listed`.
