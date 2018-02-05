package project.bookrental.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

import project.bookrental.R;
import project.bookrental.models.BookModel;
import project.bookrental.utils.DataStoreUtils;
import project.bookrental.utils.FileUtils;


/**
 * @author Marcin Korycki
 */
public class ListOfBooksActivity extends AppCompatActivity {

    public final static String SOUL = "vMv48nFoBWvfREAFBjKVvQWAZkEIRhLV9TBYKS2A";

    private EditText authorEditText, titleEditText, yearEditText;
    private ListView listView;
    private ProgressBar progressBar;
    private final List<BookModel> listOfBooks = new ArrayList<>();
    private final List<BookModel> filteredListOfBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_list_of_books);
        authorEditText = (EditText) findViewById(R.id.ListOfBooksAuthorEditText);
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
        titleEditText = (EditText) findViewById(R.id.ListOfBooksTitleEditText);
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
        yearEditText = (EditText) findViewById(R.id.ListOfBooksYearEditText);
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
        listView = (ListView) findViewById(R.id.ListOfBooksListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookModel bookModel = filteredListOfBooks.get(position);
                Intent intent = new Intent(ListOfBooksActivity.this, BitMapActivity.class);
                intent.putExtra("key", SOUL + bookModel.getId());
                startActivity(intent);
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.ListOfBooksProgressBar);

//        final FileUtils fileUtils = new FileUtils();
//        final List<FileUtils.Book> books;
//        try {
//            books = fileUtils.readBookFromFile();
//            readDatabaseFromFileAndSaveInFirebase(books);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        getAllBooksFromDatabase();
    }

    private void filterList(String author, String title, String year) {
        filteredListOfBooks.clear();
        for (BookModel book : listOfBooks) {
            if (book.getAuthor().contains(author) && book.getTitle().contains(title) && book.getYear().toString().contains(year)) {
                filteredListOfBooks.add(book);
            }
        }
        List<String> listForAdapter = new ArrayList<>();
        for (BookModel book : filteredListOfBooks) {
            listForAdapter.add(book.toString());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListOfBooksActivity.this, R.layout.activity_common_list_of_books_layout, R.id.listText, listForAdapter);
        listView.setAdapter(arrayAdapter);
    }

    void getAllBooksFromDatabase() {
        final DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                listOfBooks.clear();
                listOfBooks.addAll(DataStoreUtils.readBooks(dataSnapshot.getValue()));
                filteredListOfBooks.addAll(listOfBooks);
                List<String> listForAdapter = new ArrayList<>();
                for (BookModel book : filteredListOfBooks) {
                    listForAdapter.add(book.toString());
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListOfBooksActivity.this, R.layout.activity_common_list_of_books_layout, R.id.listText, listForAdapter);
                listView.setAdapter(arrayAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }

    void readDatabaseFromFileAndSaveInFirebase(List<FileUtils.Book> listOfBooks) {
        final List<FileUtils.Book> books = listOfBooks;
        final DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                long couinter = 10;
                for (FileUtils.Book b : books) {
                    try {
                        final BookModel model = new BookModel(b.getAuthor(), b.getTitle(), Integer.parseInt(b.getYear()));
                        model.setId(couinter);
                        booksRef.child(String.valueOf(model.getId())).setValue(model);
                        couinter++;
                    } catch (RuntimeException e) {

                    }
                    if (couinter > 2000) {
                        break;
                    }
                }
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }
}