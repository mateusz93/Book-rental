package project.bookrental.models;

import java.util.Date;

/**
 * @author Mateusz Wieczorek
 */
public class ConfirmationBookModel {

    private Long bookId;
    private String userId;
    private ConfirmationType type;
    private Date datetime;

    public ConfirmationBookModel() {
    }

    public ConfirmationBookModel(Long bookId, String userId, ConfirmationType type, Date datetime) {
        this.bookId = bookId;
        this.userId = userId;
        this.type = type;
        this.datetime = datetime;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(ConfirmationType type) {
        this.type = type;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getUserId() {
        return userId;
    }

    public ConfirmationType getType() {
        return type;
    }

    public Date getDatetime() {
        return datetime;
    }
}
