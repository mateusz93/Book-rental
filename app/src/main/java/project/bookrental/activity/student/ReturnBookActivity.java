package project.bookrental.activity.student;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import project.bookrental.R;
import project.bookrental.models.BookModel;
import project.bookrental.models.BorrowedBookModel;
import project.bookrental.models.ConfirmationBookModel;
import project.bookrental.models.ConfirmationType;
import project.bookrental.models.UserModel;

/**
 * @author Mateusz Wieczorek
 */
public class ReturnBookActivity extends AppCompatActivity {

    private ListView listView;

    private final List<BookModel> books = new ArrayList<>();
    private final List<BookModel> filteredBooks = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();
    private final List<ConfirmationBookModel> confirmations = new ArrayList<>();
    private final List<BorrowedBookModel> borrowedBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_return_book);
        listView = (ListView) findViewById(R.id.ReturnBookListView);
        setListViewItemClickListener();
        getAllBooksFromDatabase();
    }

    private void setListViewItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Hello!", "Y u no see me?");
            }
        });
    }

    private void filterList() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        filteredBooks.clear();
        filteredBooks.addAll(books);
        Iterator<BookModel> it = filteredBooks.iterator();
        while (it.hasNext()) {
            final BookModel model = it.next();
            boolean found = false;
            for (ConfirmationBookModel confirmationBookModel : confirmations) {
                if (Objects.equals(confirmationBookModel.getUserId(), currentUser.getUid()) &&
                        Objects.equals(confirmationBookModel.getBookId(), model.getId())) {
                    found = true;
                    break;
                }
            }
            for (BorrowedBookModel borrowedBookModel : borrowedBooks) {
                if (Objects.equals(borrowedBookModel.getUserId(), currentUser.getUid()) &&
                        Objects.equals(borrowedBookModel.getBookId(), model.getId())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                it.remove();
            }
        }

        ReturnBookAdapter borrowBookAdapter = new ReturnBookAdapter(ReturnBookActivity.this, filteredBooks);
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
                filterList();
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
                borrowedBooks.clear();
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Object field : list) {
                        HashMap<String, Object> fields = (HashMap<String, Object>) field;
                        Long bookId = (Long) fields.get("bookId");
                        String userId = (String) fields.get("userId");
                        Date datetime = new Date((Long) ((HashMap) fields.get("borrowDate")).get("time"));
                        borrowedBooks.add(new BorrowedBookModel(bookId, userId, datetime));
                    }
                }
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
                filterList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }

    private class ReturnBookAdapter extends ArrayAdapter<BookModel> {

        ReturnBookAdapter(Context context, List<BookModel> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final BookModel book = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_student_return_book_layout, parent, false);
            }
            final View view = convertView;
            TextView textView = view.findViewById(R.id.returnBookListText);
            textView.setText(book != null ? book.toString() : "");

            boolean notReceived = false;
            for (ConfirmationBookModel confirmationBookModel : confirmations) {
                if (book.getId().equals(confirmationBookModel.getBookId())) {
                    notReceived = true;
                }
            }
            if (notReceived) {
                view.findViewById(R.id.studentReceivedBookCheckbox).setVisibility(View.GONE);
                view.findViewById(R.id.notReceivedBookCheckbox);
            } else {
                view.findViewById(R.id.studentReceivedBookCheckbox);
                view.findViewById(R.id.notReceivedBookCheckbox).setVisibility(View.GONE);
            }

            return view;
        }
    }

}