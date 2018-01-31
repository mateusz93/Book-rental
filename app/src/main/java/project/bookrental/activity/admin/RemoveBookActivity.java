package project.bookrental.activity.admin;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import project.bookrental.R;
import project.bookrental.models.BookModel;
import project.bookrental.utils.DataStoreUtils;


/**
 * @author Marcin Korycki
 */
public class RemoveBookActivity extends AppCompatActivity {

    private EditText authorEditText, titleEditText, yearEditText;
    private ListView listView;
    private ProgressBar progressBar;
    private final List<BookModel> listOfBooks = new ArrayList<>();
    private final List<BookModel> filteredListOfBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_remove_book);
        authorEditText = (EditText) findViewById(R.id.RemoveBookAuthorEditText);
        authorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString(), titleEditText.getText().toString(), yearEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        titleEditText = (EditText) findViewById(R.id.RemoveBookTitleEditText);
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(authorEditText.getText().toString(), s.toString(), yearEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        yearEditText = (EditText) findViewById(R.id.RemoveBookYearEditText);
        yearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(authorEditText.getText().toString(), titleEditText.getText().toString(), s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        listView = (ListView) findViewById(R.id.RemoveBookListView);
        progressBar = (ProgressBar) findViewById(R.id.RemoveBookProgressBar);
        getAllBooksFromDatabase();

    }

    private void filterList(String author, String title, String year) {
        filteredListOfBooks.clear();
        for (BookModel book : listOfBooks) {
            if (book.getAuthor().contains(author) && book.getTitle().contains(title) && book.getYear().toString().contains(year)) {
                filteredListOfBooks.add(book);
            }
        }
        BookAdapter bookAdapter = new BookAdapter(RemoveBookActivity.this, filteredListOfBooks);
        listView.setAdapter(bookAdapter);
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
                listOfBooks.clear();
                listOfBooks.addAll(DataStoreUtils.readBooks(list));
                filterList(authorEditText.getText().toString(), titleEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }

    private class BookAdapter extends ArrayAdapter<BookModel> {
        BookAdapter(Context context, List<BookModel> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final BookModel book = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_admin_remove_row_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.listText);
            Button button = convertView.findViewById(R.id.removeBookButton);
            textView.setText(book != null ? book.toString() : "");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");
                    booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            booksRef.removeEventListener(this);
                            booksRef.child(String.valueOf(book.getId())).removeValue();
                            Toast.makeText(getApplicationContext(), "Book removed from database!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RemBookListene:183", databaseError.getMessage());
                        }
                    });

                    final DatabaseReference confirmationsRef = FirebaseDatabase.getInstance().getReference("confirmations");
                    confirmationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            confirmationsRef.removeEventListener(this);
                            confirmationsRef.child(String.valueOf(book.getId())).removeValue();
                            Toast.makeText(getApplicationContext(), "Book removed from database!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RemBookListene:183", databaseError.getMessage());
                        }
                    });

                    final DatabaseReference borrowedBooksRef = FirebaseDatabase.getInstance().getReference("borrowed_books");
                    borrowedBooksRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            borrowedBooksRef.removeEventListener(this);
                            borrowedBooksRef.child(String.valueOf(book.getId())).removeValue();
                            Toast.makeText(getApplicationContext(), "Book removed from database!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("Err:listofbooks:", databaseError.toException());
                        }
                    });
                }
            });
            return convertView;
        }
    }
};