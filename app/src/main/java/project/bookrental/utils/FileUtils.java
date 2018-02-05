package project.bookrental.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mateusz Wieczorek
 */
public class FileUtils {

    public List<Book> readBookFromFile() throws IOException {
        final List<Book> books = new ArrayList<>(100);
        final File[] list = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
        File file = null;
        for (int i = 0; i < list.length; ++i) {
            if (list[i].getName().contains("database.csv")) {
                file = list[i];
                break;
            }
        }
        List<String> lines = org.apache.commons.io.FileUtils.readLines(file, "UTF-8");
        for (String line : lines) {
            String[] data = line.split(";");
            books.add(new Book(data[0], data[1], data[2]));
        }
        return books;
    }

    public class Book {
        private final String title;
        private final String author;
        private final String year;

        Book(String title, String author, String year) {
            this.title = title;
            this.author = author;
            this.year = year;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getYear() {
            return year;
        }
    }
}
