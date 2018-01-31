package project.bookrental.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import project.bookrental.models.BookModel;
import project.bookrental.models.BorrowedBookModel;
import project.bookrental.models.ConfirmationBookModel;
import project.bookrental.models.ConfirmationType;
import project.bookrental.models.RequestBookModel;
import project.bookrental.models.ReserveBookModel;
import project.bookrental.models.UserModel;

/**
 * @author Mateusz Wieczorek
 */
public class DataStoreUtils {

    public static List<BookModel> readBooks(List<Object> list) {
        List<BookModel> books = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Object field : list) {
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                Long id = (Long) fields.get("id");
                String author = (String) fields.get("author");
                Integer year = ((Long) fields.get("year")).intValue();
                String title = (String) fields.get("title");
                books.add(new BookModel(id, author, title, year));
            }
        }
        return books;
    }

    public static List<BorrowedBookModel> readBorrowedBooks(List<Object> list) {
        List<BorrowedBookModel> listOfBorrowedBooks = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Object field : list) {
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                Long bookId = (Long) fields.get("bookId");
                String userId = (String) fields.get("userId");
                Date datetime = new Date((Long) ((HashMap) fields.get("borrowDate")).get("time"));
                listOfBorrowedBooks.add(new BorrowedBookModel(bookId, userId, datetime));
            }
        }
        return listOfBorrowedBooks;
    }

    public static List<RequestBookModel> readRequest(List<Object> list) {
        List<RequestBookModel> requests = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Object field : list) {
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                Long id = (Long) fields.get("id");
                String author = (String) fields.get("author");
                Integer year = ((Long) fields.get("year")).intValue();
                String title = (String) fields.get("title");
                requests.add(new RequestBookModel(id, author, title, year));
            }
        }
        return requests;
    }

    public static List<ConfirmationBookModel> readConfirmations(List<Object> list) {
        List<ConfirmationBookModel> confirmations = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Object field : list) {
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                Long bookId = (Long) fields.get("bookId");
                String userId = (String) fields.get("userId");
                ConfirmationType type = ConfirmationType.valueOf((String) fields.get("type"));
                Date datetime = new Date((Long) ((HashMap) fields.get("datetime")).get("time"));
                confirmations.add(new ConfirmationBookModel(bookId, userId, type, datetime));
            }
        }
        return confirmations;
    }

    public static List<UserModel> readUsers(List<Object> list) {
        List<UserModel> users = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Object field : list) {
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                String uId = (String) fields.get("uid");
                String email = (String) fields.get("email");
                String displayName = (String) fields.get("displayName");
                String phoneNumber = (String) fields.get("phoneNumber");
                users.add(new UserModel(uId, email, displayName, phoneNumber));
            }
        }
        return users;
    }
}
