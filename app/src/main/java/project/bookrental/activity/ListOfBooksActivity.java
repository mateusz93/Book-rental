package project.bookrental.activity;

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
import java.util.HashMap;
import java.util.List;

import project.bookrental.R;
import project.bookrental.models.BookModel;


/**
 * Created by marcin on 14.10.17.
 */

public class ListOfBooksActivity extends AppCompatActivity {

    EditText authorEditText, titleEditText, yearEditText;
    ListView listView;
    ProgressBar progressBar;
    List<BookModel> listOfBooks = new ArrayList<>();
    List<BookModel> filteredListOfBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_books); // TODO: add separate views for every Activity
        authorEditText = (EditText) findViewById(R.id.ListOfBooksAuthorEditText);
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
        titleEditText = (EditText) findViewById(R.id.ListOfBooksTitleEditText);
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
        yearEditText = (EditText) findViewById(R.id.ListOfBooksYearEditText);
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
        listView = (ListView) findViewById(R.id.ListOfBooksListView);
        progressBar = (ProgressBar) findViewById(R.id.ListOfBooksProgressBar);
        getAllBooksFromDatabase();

    }

    private void filterList(String author, String title, String year) {
        filteredListOfBooks.clear();
        for(BookModel book : listOfBooks){
            if(book.getAuthor().contains(author) && book.getTitle().contains(title) && book.getYear().toString().contains(year)){
                filteredListOfBooks.add(book);
            }
        }
        List<String> listForAdapter = new ArrayList<>();
        for(BookModel book : filteredListOfBooks){
            listForAdapter.add(book.toString());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListOfBooksActivity.this, R.layout.activity_row_layout, R.id.listText, listForAdapter);
        listView.setAdapter(arrayAdapter);
    }

    void getAllBooksFromDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("books");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap)dataSnapshot.getValue()).values().toArray()));
                listOfBooks.clear();
                if (CollectionUtils.isNotEmpty(list)) {
                    for(Object field : list){
                        HashMap<String, Object> fields = (HashMap<String, Object>) field;
                        Long id = (Long) fields.get("id");
                        String author = (String) fields.get("author");
                        Integer year = ((Long) fields.get("year")).intValue();
                        String title = (String) fields.get("title");
                        listOfBooks.add(new BookModel(id,author,title,year));
                    }
                }
                filteredListOfBooks.addAll(listOfBooks);
                List<String> listForAdapter = new ArrayList<>();
                for(BookModel book : filteredListOfBooks){
                    listForAdapter.add(book.toString());
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListOfBooksActivity.this, R.layout.activity_row_layout, R.id.listText, listForAdapter);
                listView.setAdapter(arrayAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }
};