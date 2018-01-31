package project.bookrental.activity.admin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import project.bookrental.R;
import project.bookrental.models.BookModel;
import project.bookrental.models.BorrowedBookModel;
import project.bookrental.models.UserModel;
import project.bookrental.utils.DataStoreUtils;

/**
 * @author Mateusz Wieczorek
 */
public class BorrowBookActivity extends AppCompatActivity {

    private EditText titleEditText, emailEditText;
    private ListView listView;
    private final List<BookModel> books = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();
    private final List<BorrowedBookModel> listOfBorrowedBooks = new ArrayList<>();

    private boolean isBooksStored = false;
    private boolean isBorrowedBooksStored = false;
    private boolean isUsersStored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_borrowed_books);
        titleEditText = (EditText) findViewById(R.id.adminBorrowTitleEditText);
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString(), emailEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        emailEditText = (EditText) findViewById(R.id.adminBorrowEmailEditText);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(titleEditText.getText().toString(), s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        listView = (ListView) findViewById(R.id.AdminListOfBorrowedBooksListView);
        getAllBooksFromDatabase();
    }

    private void filterList(String title, String email) {
        if (!isBooksStored || !isBorrowedBooksStored || !isUsersStored) {
            return;
        }

        List<String> bookViews = new ArrayList<>();
        for (BorrowedBookModel borrowedBookModel : listOfBorrowedBooks) {
            UserModel userModel = null;
            BookModel bookModel = null;
            for (UserModel user : users) {
                if (borrowedBookModel.getUserId().equalsIgnoreCase(user.getUId())) {
                    userModel = user;
                    break;
                }
            }
            for (BookModel book : books) {
                if (Objects.equals(borrowedBookModel.getBookId(), book.getId())) {
                    bookModel = book;
                    break;
                }
            }
            if (bookModel != null && bookModel.getTitle().contains(title)
                    && userModel != null && userModel.getEmail().contains(email)) {
                bookViews.add(new BookView(bookModel, userModel).toString());
            }
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(BorrowBookActivity.this,
                R.layout.activity_admin_borrowed_books_layout,
                R.id.adminBorrowListText,
                bookViews);
        listView.setAdapter(arrayAdapter);
    }

    void getAllBooksFromDatabase() {
        final DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                books.clear();
                books.addAll(DataStoreUtils.readBooks(list));
                isBooksStored = true;
                filterList(titleEditText.getText().toString(), emailEditText.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });

        final DatabaseReference borrowedBooksRef = FirebaseDatabase.getInstance().getReference("borrowed_books");
        borrowedBooksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                listOfBorrowedBooks.clear();
                listOfBorrowedBooks.addAll(DataStoreUtils.readBorrowedBooks(list));
                isBorrowedBooksStored = true;
                filterList(titleEditText.getText().toString(), emailEditText.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });

        final DatabaseReference usersdRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                users.clear();
                users.addAll(DataStoreUtils.readUsers(list));
                isUsersStored = true;
                filterList(titleEditText.getText().toString(), emailEditText.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }

    private class BookView {

        BookView(BookModel bookModel, UserModel user) {
            this.bookModel = bookModel;
            this.user = user;
        }

        BookModel bookModel;
        UserModel user;

        BookModel getBookModel() {
            return bookModel;
        }

        void setBookModel(BookModel bookModel) {
            this.bookModel = bookModel;
        }

        UserModel getUser() {
            return user;
        }

        void setUser(UserModel user) {
            this.user = user;
        }

        @Override
        public String toString() {
            return bookModel.getTitle() + ", " + user.getEmail();
        }
    }


}