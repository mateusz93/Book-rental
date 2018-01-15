package project.bookrental;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import project.bookrental.models.BookModel;

/**
 *
 * @author Mateusz Wieczorek
 *
 */
public class ReturnBookActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ListView listView;
    ProgressBar progressBar;
    List<BookModel> borrowedBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_book);
        listView = (ListView) findViewById(R.id.ReturnBookListView);
        setListViewItemClickListener();
        progressBar = (ProgressBar) findViewById(R.id.ReturnBookProgressBar);
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

    void getAllBooksFromDatabase() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference booksRepository = database.getReference("books");
        final DatabaseReference borrowedBooksRepository = database.getReference("borrowed_books");
        booksRepository.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap)dataSnapshot.getValue()).values().toArray()));
                borrowedBooks.clear();
                if (CollectionUtils.isNotEmpty(list)) {
                    for(Object field : list){
                        HashMap<String, Object> fields = (HashMap<String, Object>) field;
                        Long id = (Long) fields.get("id");
                        String author = (String) fields.get("author");
                        Integer year = ((Long) fields.get("year")).intValue();
                        String title = (String) fields.get("title");
                        borrowedBooks.add(new BookModel(id, author, title, year));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:borrowedBooks:", databaseError.toException());
            }
        });

        borrowedBooksRepository.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null) {
                    startActivity(new Intent(ReturnBookActivity.this, LoginActivity.class));
                    finish();
                }
                final String userId = auth.getCurrentUser().getUid();
                List<Object> list = Arrays.asList((((HashMap)dataSnapshot.getValue()).values().toArray()));
                if (CollectionUtils.isNotEmpty(list)) {
                    final List<Long> borrowedBookIds = new ArrayList<>();
                    for(Object field : list) {
                        HashMap<String, Object> fields = (HashMap<String, Object>) field;
                        if (userId.equalsIgnoreCase((String) fields.get("userId"))) {
                            borrowedBookIds.add((Long) fields.get("bookId"));
                        }
                    }

                    Iterator<BookModel> it = borrowedBooks.iterator();
                    while(it.hasNext()) {
                        final BookModel model = it.next();
                        boolean found = false;
                        for (Long bookId : borrowedBookIds) {
                            if (Objects.equals(model.getId(), bookId)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            it.remove();
                        }
                    }
                }
                ReturnBookAdapter borrowBookAdapter = new ReturnBookAdapter(ReturnBookActivity.this, borrowedBooks);
                listView.setAdapter(borrowBookAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:borrowedBooks:", databaseError.toException());
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_return_book_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.returnBookListText);
            Button button = convertView.findViewById(R.id.returnBookButton);
            textView.setText(book != null ? book.toString() : "");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("borrowed_books");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myRef.removeEventListener(this);
                            myRef.child(String.valueOf(book.getId())).removeValue();
                            //myRef.child(String.valueOf(book.getId())).setValue(null);
                            Toast.makeText(getApplicationContext(), "Book returned", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:BorrowBook:183", databaseError.getMessage());
                        }
                    });
                }
            });
            return convertView;
        }
    }
}