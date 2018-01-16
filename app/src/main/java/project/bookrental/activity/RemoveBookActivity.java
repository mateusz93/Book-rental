package project.bookrental.activity;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.bookrental.R;
import project.bookrental.models.BookModel;


/**
 * Created by marcin on 14.10.17.
 */

public class RemoveBookActivity extends AppCompatActivity {

    EditText authorEditText, titleEditText, yearEditText;
    ListView listView;
    ProgressBar progressBar;
    List<BookModel> listOfBooks = new ArrayList<>();
    List<BookModel> filteredListOfBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_book);
        authorEditText = (EditText) findViewById(R.id.RemoveBookAuthorEditText);
        authorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString(),titleEditText.getText().toString(),yearEditText.getText().toString());
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
                filterList(authorEditText.getText().toString(),s.toString(),yearEditText.getText().toString());
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
                filterList(authorEditText.getText().toString(),titleEditText.getText().toString(),s.toString());
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
        for(BookModel book : listOfBooks){
            if(book.getAuthor().contains(author) && book.getTitle().contains(title) && book.getYear().toString().contains(year)){
                filteredListOfBooks.add(book);
            }
        }
        BookAdapter bookAdapter = new BookAdapter(RemoveBookActivity.this, filteredListOfBooks);
        listView.setAdapter(bookAdapter);
    }

    void getAllBooksFromDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("books");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();
                Collection<Map<String,Object>> books = new ArrayList<>();
                if(value instanceof HashMap) {
                    books = ((HashMap<String,Map<String,Object>>) value).values();
                } else if(value instanceof List){
                    books = (List<Map<String,Object>>)value;
                } else {
                    System.out.println("Error! RemoveBookActivity -> getAllBooksFromDatabase -> onDataChangeValue -> value = " + value);
                }
                listOfBooks.clear();
                for (Map<String, Object> fields : books) {
                    if (fields == null) continue;
                    Long id = (Long) fields.get("id");
                    String author = (String) fields.get("author");
                    Integer year = ((Long) fields.get("year")).intValue();
                    String title = (String) fields.get("title");
                    listOfBooks.add(new BookModel(id, author, title, year));
                }
                filterList(authorEditText.getText().toString(),titleEditText.getText().toString(),yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }

    private class BookAdapter extends ArrayAdapter<BookModel> {
        public BookAdapter(Context context, List<BookModel> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final BookModel book = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_remove_row_layout, parent, false);
            }
            // Lookup view for data population
            TextView textView = (TextView) convertView.findViewById(R.id.listText);
            Button button = (Button) convertView.findViewById(R.id.removeBookButton);
            // Populate the data into the template view using the data object
            textView.setText(book.toString());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("books");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myRef.removeEventListener(this);
                            myRef.child(String.valueOf(book.getId())).setValue(null);
                            Toast.makeText(getApplicationContext(), "Book removed from database!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RemBookListene:183", databaseError.getMessage());
                        }
                    });
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }
    }
};