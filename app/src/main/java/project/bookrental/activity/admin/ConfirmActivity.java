package project.bookrental.activity.admin;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import project.bookrental.R;
import project.bookrental.models.BookModel;
import project.bookrental.models.BorrowedBookModel;
import project.bookrental.models.ConfirmationBookModel;
import project.bookrental.models.ConfirmationType;
import project.bookrental.models.UserModel;

/**
 * @author Mateusz Wieczorek
 */
public class ConfirmActivity extends AppCompatActivity {

    private ListView listView;
    private final List<BookModel> books = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();
    private final List<ConfirmationBookModel> confirmations = new ArrayList<>();
    private final List<BookView> confirmationsBooks = new ArrayList<>();

    private boolean isConfirmationStored = false;
    private boolean isBooksStored = false;
    private boolean isUsersStored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmations);
        listView = (ListView) findViewById(R.id.ConfirmationBorrowBookListView);
        getAllBooksFromDatabase();
    }

    private void filterList() {
        if (!isBooksStored || !isConfirmationStored || !isUsersStored) {
            return;
        }
        confirmationsBooks.clear();
        for (ConfirmationBookModel confirmationBook : confirmations) {
            BookView bookView = new BookView();
            for (BookModel bookModel : books) {
                if (bookModel.getId().equals(confirmationBook.getBookId())) {
                    bookView.setBookModel(bookModel);
                    break;
                }
            }
            for (UserModel userModel : users) {
                if (confirmationBook.getUserId().equalsIgnoreCase(userModel.getUId())) {
                    bookView.setUser(userModel);
                    break;
                }
            }
            confirmationsBooks.add(bookView);
        }
        ConfirmBookAdapter borrowBookAdapter = new ConfirmBookAdapter(ConfirmActivity.this, confirmationsBooks);
        listView.setAdapter(borrowBookAdapter);
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
                isBooksStored = true;
                filterList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });

        final DatabaseReference confirmationsReference = FirebaseDatabase.getInstance().getReference("confirmations");
        confirmationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                confirmations.clear();
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
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
                isConfirmationStored = true;
                filterList();
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
                isUsersStored = true;
                filterList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }


    private class ConfirmBookAdapter extends ArrayAdapter<BookView> {

        ConfirmBookAdapter(Context context, List<BookView> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final BookView book = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_confirmations_layout, parent, false);
            }
            if (!isBooksStored || !isConfirmationStored || !isUsersStored) {
                return convertView;
            }
            TextView textView = convertView.findViewById(R.id.confirmBorrowBookListText);
            Button button = convertView.findViewById(R.id.confirmBorrowBookButton);
            textView.setText("Book: " + (book.getBookModel() != null ? book.getBookModel().toString() : ""));
            textView.setText(textView.getText() + "\n" + "User: " + book.getUser().getEmail());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference borrowedBooksRef = FirebaseDatabase.getInstance().getReference("borrowed_books");
                    borrowedBooksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            borrowedBooksRef.removeEventListener(this);
                            BorrowedBookModel borrowedBookModel = new BorrowedBookModel();
                            borrowedBookModel.setBookId(book.getBookModel().getId());
                            borrowedBookModel.setBorrowDate(new Date());
                            borrowedBookModel.setUserId(book.getUser().getUId());
                            borrowedBooksRef.child(book.getBookModel().getId().toString()).setValue(borrowedBookModel);
                            Toast.makeText(getApplicationContext(), "Book borrowed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:BorrowBook:183", databaseError.getMessage());
                        }
                    });

                    final DatabaseReference confirmationsRef = FirebaseDatabase.getInstance().getReference("confirmations");
                    confirmationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            confirmationsRef.removeEventListener(this);
                            confirmationsRef.child(book.getBookModel().getId().toString()).removeValue();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:BorrowBook:183", databaseError.getMessage());
                        }
                    });
                    getAllBooksFromDatabase();
                }
            });
            return convertView;
        }
    }

    private class BookView {

        BookView() {}

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
    }
}
