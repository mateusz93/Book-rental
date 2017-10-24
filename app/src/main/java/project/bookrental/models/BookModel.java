package project.bookrental.models;

/**
 * Created by marcin on 23.10.17.
 */

public class BookModel {
    private String author;
    private String title;
    private Integer year;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BookModel(String author, String title, Integer year) {
        this.author = author;
        this.title = title;
        this.year = year;
    }
}
