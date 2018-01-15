package project.bookrental;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import project.bookrental.models.BookModel;
import project.bookrental.models.BorrowedBookModel;

/**
 *
 * @author Mateusz Wieczorek
 *
 */
public class BorrowBookActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText authorEditText, titleEditText, yearEditText;
    private ListView listView;
    private ProgressBar progressBar;
    private final List<BookModel> borrowedBooks = new ArrayList<>();
    private final List<BookModel> filteredBorrowBook = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(BorrowBookActivity.this, LoginActivity.class));
            finish();
        }

        setContentView(R.layout.activity_borrow_books);
        authorEditText = (EditText) findViewById(R.id.BorrowBookAuthorEditText);
        setAuthorEditTestListener();
        titleEditText = (EditText) findViewById(R.id.BorrowBookTitleEditText);
        setTitleEditTextListener();
        yearEditText = (EditText) findViewById(R.id.BorrowBookYearEditText);
        setYearEditTextListener();
        listView = (ListView) findViewById(R.id.BorrowBookListView);
        setListViewItemClickListener();
        progressBar = (ProgressBar) findViewById(R.id.BorrowBookProgressBar);
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

    private void setYearEditTextListener() {
        yearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(authorEditText.getText().toString(),titleEditText.getText().toString(),s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setTitleEditTextListener() {
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(authorEditText.getText().toString(),s.toString(),yearEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setAuthorEditTestListener() {
        authorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString(),titleEditText.getText().toString(),yearEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList(String author, String title, String year) {
        filteredBorrowBook.clear();
        for(BookModel book : borrowedBooks){
            if(book.getAuthor().contains(author) && book.getTitle().contains(title) && book.getYear().toString().contains(year)){
                filteredBorrowBook.add(book);
            }
        }
        BorrowBookAdapter borrowBookAdapter = new BorrowBookAdapter(BorrowBookActivity.this, filteredBorrowBook);
        listView.setAdapter(borrowBookAdapter);
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
                filterList(authorEditText.getText().toString(), titleEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:borrowedBooks:", databaseError.toException());
            }
        });

        borrowedBooksRepository.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap)dataSnapshot.getValue()).values().toArray()));
                if (CollectionUtils.isNotEmpty(list)) {
                    final List<Long> toRemove = new ArrayList<>();
                    for(Object field : list) {
                        HashMap<String, Object> fields = (HashMap<String, Object>) field;
                        toRemove.add((Long) fields.get("bookId"));
                    }
                    for (Long bookId : toRemove) {
                        Iterator<BookModel> it = filteredBorrowBook.iterator();
                        while(it.hasNext()) {
                            final BookModel model = it.next();
                            if (Objects.equals(model.getId(), bookId)) {
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:borrowedBooks:", databaseError.toException());
            }
        });
    }

    private class BorrowBookAdapter extends ArrayAdapter<BookModel> {

        BorrowBookAdapter(Context context, List<BookModel> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final BookModel book = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_borrow_books_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.borrowBookListText);
            Button button = convertView.findViewById(R.id.borrowBookButton);
            textView.setText(book != null ? book.toString() : "");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final BorrowedBookModel model = new BorrowedBookModel();
                    model.setUserId(userId);
                    model.setBookId(book.getId());
                    model.setBorrowDate(new Date());
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("borrowed_books");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myRef.removeEventListener(this);
                            myRef.child(String.valueOf(model.getBookId())).setValue(model);
                            Toast.makeText(getApplicationContext(), "Book borrowed!", Toast.LENGTH_SHORT).show();
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