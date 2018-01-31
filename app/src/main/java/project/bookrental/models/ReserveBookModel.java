package project.bookrental.models;

import java.util.Date;

/**
 * Created by Krystian on 31.01.2018.
 */

public class ReserveBookModel {

    private Long bookId;
    private String userId;
    private Date borrowDate;

    public ReserveBookModel(Long bookId, String userId, Date borrowDate) {
        this.bookId = bookId;
        this.userId = userId;
        this.borrowDate = borrowDate;
    }

    public ReserveBookModel() {
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }
}
