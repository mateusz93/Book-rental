package project.bookrental.activity.student;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import project.bookrental.R;
import project.bookrental.activity.common.LoginActivity;
import project.bookrental.models.BookModel;
import project.bookrental.models.BorrowedBookModel;
import project.bookrental.models.ConfirmationBookModel;
import project.bookrental.models.ConfirmationType;
import project.bookrental.utils.DataStoreUtils;

/**
 * @author Mateusz Wieczorek
 */
public class BorrowBookActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText authorEditText, titleEditText, yearEditText;
    private ListView listView;
    private ProgressBar progressBar;
    private final List<BookModel> books = new ArrayList<>();
    private final List<BookModel> filteredBorrowBook = new ArrayList<>();
    private final List<ConfirmationBookModel> confirmationBookModels = new ArrayList<>();
    private final List<BorrowedBookModel> borrowedBooks = new ArrayList<>();
    private final List<Long> confirmationBookIds = new ArrayList<>();

    private boolean isConfirmationStored = false;
    private boolean isBooksStored = false;
    private boolean isBorrowedBooksStored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(BorrowBookActivity.this, LoginActivity.class));
            finish();
        }

        setContentView(R.layout.activity_student_borrow_books);
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
    }

    private void setTitleEditTextListener() {
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
    }

    private void setAuthorEditTestListener() {
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
    }

    private void filterList(String author, String title, String year) {
        if (!isBooksStored || !isConfirmationStored || !isBorrowedBooksStored) {
            return;
        }
        filteredBorrowBook.clear();
        filteredBorrowBook.addAll(books);
        Iterator<BookModel> it = filteredBorrowBook.iterator();
        while (it.hasNext()) {
            final BookModel model = it.next();
            if (!model.getAuthor().contains(author) || !model.getTitle().contains(title) && !model.getYear().toString().contains(year)) {
                it.remove();
                continue;
            }
            boolean isConfirm = false;
            for (ConfirmationBookModel confirmationBookModel : confirmationBookModels) {
                if (confirmationBookModel.getBookId().equals(model.getId())) {
                    if (!confirmationBookModel.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) isConfirm = true;
                    break;
                }
            }
            if (isConfirm) {
                it.remove();
                continue;
            }
            boolean isBorrowed = false;
            for (BorrowedBookModel borrowedBookModel : borrowedBooks) {
                if (borrowedBookModel.getBookId().equals(model.getId())) {
                    isBorrowed = true;
                    break;
                }
            }
            if (isBorrowed) {
                it.remove();
            }
        }
        BorrowBookAdapter borrowBookAdapter = new BorrowBookAdapter(BorrowBookActivity.this, filteredBorrowBook);
        listView.setAdapter(borrowBookAdapter);
    }

    void getAllBooksFromDatabase() {
        final DatabaseReference booksRepository = FirebaseDatabase.getInstance().getReference("books");
        booksRepository.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                books.clear();
                books.addAll(DataStoreUtils.readBooks(list));
                isBooksStored = true;
                filterList(authorEditText.getText().toString(), titleEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:borrowedBooks:", databaseError.toException());
            }
        });

        final DatabaseReference confirmations = FirebaseDatabase.getInstance().getReference("confirmations");
        confirmations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    isConfirmationStored = true;
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                confirmationBookModels.clear();
                confirmationBookModels.addAll(DataStoreUtils.readConfirmations(list));
                confirmationBookIds.clear();
                for (ConfirmationBookModel conf : confirmationBookModels) {
                    confirmationBookIds.add(conf.getBookId());
                }
                isConfirmationStored = true;
                filterList(authorEditText.getText().toString(), titleEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });

        final DatabaseReference borrowedBooksRepository = FirebaseDatabase.getInstance().getReference("borrowed_books");
        borrowedBooksRepository.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    isBorrowedBooksStored = true;
                    return;
                }
                List<Object> list = Arrays.asList((((HashMap) dataSnapshot.getValue()).values().toArray()));
                borrowedBooks.clear();
                borrowedBooks.addAll(DataStoreUtils.readBorrowedBooks(list));
                isBorrowedBooksStored = true;
                filterList(authorEditText.getText().toString(), titleEditText.getText().toString(), yearEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_student_borrow_books_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.borrowBookListText);
            final Button button = convertView.findViewById(R.id.reserveBookButton);
            textView.setText(book != null ? book.toString() : "");
            if (!isBooksStored || !isConfirmationStored || !isBorrowedBooksStored) {
                return convertView;
            }
            if (confirmationBookIds.contains(book.getId()))
                button.setText(R.string.ReserveBookCancel);
            else button.setText(R.string.ReserveBookSubmit);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final ConfirmationBookModel model = new ConfirmationBookModel();
                    model.setUserId(userId);
                    model.setBookId(book.getId());
                    model.setType(ConfirmationType.BORROW);
                    Date now = new Date();
                    if (!confirmationBookIds.contains(book.getId()))
                        now.setTime(now.getTime() + 24 * 60 * 60 * 1000);
                    model.setDatetime(now);
                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("confirmations");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myRef.removeEventListener(this);
                            myRef.child(String.valueOf(model.getBookId())).setValue(model);
                            if (!confirmationBookIds.contains(book.getId())) {
                                confirmationBookIds.add(book.getId());
                                button.setText(R.string.ReserveBookCancel);
                                Toast.makeText(getApplicationContext(), "Book reserved! It will expire after 24 hours! Get book from library!", Toast.LENGTH_SHORT).show();
                            } else {
                                confirmationBookIds.remove(book.getId());
                                button.setText(R.string.ReserveBookSubmit);
                                Toast.makeText(getApplicationContext(), "Book reservation cancelled!", Toast.LENGTH_SHORT).show();
                            }
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