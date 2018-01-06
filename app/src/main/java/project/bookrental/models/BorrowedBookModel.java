package project.bookrental.models;

import java.time.LocalDateTime;

/**
 * Created by Mateusz on 05.01.2018.
 */

public class BorrowedBookModel {

    private Long bookId;
    private Long userId;
    private LocalDateTime borrowDate;

    public BorrowedBookModel() {
    }

    public BorrowedBookModel(Long bookId, Long userId, LocalDateTime borrowDate) {
        this.bookId = bookId;
        this.userId = userId;
        this.borrowDate = borrowDate;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }
}

