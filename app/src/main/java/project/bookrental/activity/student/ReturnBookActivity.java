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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import project.bookrental.R;
import project.bookrental.models.BookModel;
import project.bookrental.models.BorrowedBookModel;
import project.bookrental.models.ConfirmationBookModel;
import project.bookrental.models.UserModel;
import project.bookrental.utils.DataStoreUtils;

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

    private boolean isConfirmationStored = false;
    private boolean isBooksStored = false;
    private boolean isBorrowedBooksStored = false;
    private boolean isUsersStored = false;

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
        if (!isBorrowedBooksStored || !isBooksStored || !isConfirmationStored || !isUsersStored) {
            return;
        }
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
                books.addAll(DataStoreUtils.readBooks(list));
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
                confirmations.clear();
                confirmations.addAll(DataStoreUtils.readConfirmations(list));
                isConfirmationStored = true;
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
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                borrowedBooks.clear();
                borrowedBooks.addAll(DataStoreUtils.readBorrowedBooks(list));
                isBorrowedBooksStored = true;
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
                users.addAll(DataStoreUtils.readUsers(list));
                isUsersStored = true;
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

            if (!isBorrowedBooksStored || !isBooksStored || !isConfirmationStored || !isUsersStored) {
                return view;
            }
            for (ConfirmationBookModel confirmationBookModel : confirmations) {
                if (book.getId().equals(confirmationBookModel.getBookId())) {
                    view.findViewById(R.id.studentReceivedBookCheckbox).setVisibility(View.GONE);
                    break;
                }
            }

            return view;
        }
    }

}