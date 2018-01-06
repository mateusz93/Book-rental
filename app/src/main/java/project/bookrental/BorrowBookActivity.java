package project.bookrental;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.HashMap;
import java.util.List;

import project.bookrental.models.BookModel;

public class BorrowBookActivity extends AppCompatActivity {

    EditText authorEditText, titleEditText, yearEditText;
    ListView listView;
    ProgressBar progressBar;
    List<BookModel> borrowedBooks = new ArrayList<>();
    List<BookModel> filteredBorrowBook = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_boos);
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
        List<String> listForAdapter = new ArrayList<>();
        for(BookModel book : filteredBorrowBook){
            listForAdapter.add(book.toString());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(BorrowBookActivity.this, R.layout.activity_row_layout, R.id.listText, listForAdapter);
        listView.setAdapter(arrayAdapter);
    }

    void getAllBooksFromDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("books");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                filteredBorrowBook.addAll(borrowedBooks);
                List<String> listForAdapter = new ArrayList<>();
                for(BookModel book : filteredBorrowBook){
                    listForAdapter.add(book.toString());
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(BorrowBookActivity.this, R.layout.activity_row_layout, R.id.listText, listForAdapter);
                listView.setAdapter(arrayAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:borrowedBooks:", databaseError.toException());
            }
        });
    }
};