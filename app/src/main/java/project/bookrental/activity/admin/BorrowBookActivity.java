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

/**
 * @author Mateusz Wieczorek
 */
public class BorrowBookActivity extends AppCompatActivity {

    private EditText titleEditText, emailEditText;
    private ListView listView;
    private final List<BookModel> books = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();
    private final List<BorrowedBookModel> listOfBorrowedBooks = new ArrayList<>();

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
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("books");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                books.clear();
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
                filterList(titleEditText.getText().toString(), emailEditText.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });

        final DatabaseReference confirmations = FirebaseDatabase.getInstance().getReference("borrowed_books");
        confirmations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                listOfBorrowedBooks.clear();
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Object field : list) {
                        HashMap<String, Object> fields = (HashMap<String, Object>) field;
                        Long bookId = (Long) fields.get("bookId");
                        String userId = (String) fields.get("userId");
                        Date datetime = new Date((Long) ((HashMap) fields.get("borrowDate")).get("time"));
                        listOfBorrowedBooks.add(new BorrowedBookModel(bookId, userId, datetime));
                    }
                }
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