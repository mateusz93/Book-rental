package project.bookrental.models;

/**
 * @author Mateusz Wieczorek
 */
public class RequestBookModel {

    private Long id;
    private String author;
    private String title;
    private Integer year;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public RequestBookModel(String author, String title, Integer year) {
        this.author = author;
        this.title = title;
        this.year = year;
    }

    public RequestBookModel(Long id, String author, String title, Integer year) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.year = year;
    }

    @Override
    public String toString() {
        return author + " \"" + title + "\" " + year;
    }
}
