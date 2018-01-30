package project.bookrental.activity.student;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import project.bookrental.R;
import project.bookrental.models.BookModel;

/**
 * @author Mateusz Wieczorek
 */
public class RequestBookActivity extends AppCompatActivity {

    EditText authorEditText, titleEditText, yearEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_new_book);
        authorEditText = (EditText) findViewById(R.id.RequestBookAuthorEditText);
        titleEditText = (EditText) findViewById(R.id.RequestBookTitleEditText);
        yearEditText = (EditText) findViewById(R.id.RequestBookYearEditText);
    }

    public void requestListener(View view) {
        Map<String,String> validationErrors = new HashMap<>();
        String author = authorEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String rawYear = yearEditText.getText().toString();
        if(author.isEmpty()){
            validationErrors.put("author","Author field cannot be empty!");
        } else if(author.split(" ").length != 2 || author.matches(".*\\d+.*")) {
            validationErrors.put("author","Invalid format on author field! Expected \"[name] [surname(s)]\"");
        }
        if(title.isEmpty()) {
            validationErrors.put("title","Title field cannot be empty!");
        }
        if(rawYear.isEmpty()) {
            validationErrors.put("year", "Year field cannot be empty!");
        } else {
            try {
                Integer year = Integer.parseInt(rawYear);
                int currentYear = new Date().getYear() + 1900;
                if (year < 1000 || year > currentYear) {
                    validationErrors.put("year", "Expected year from range 1000 <= year <= " + currentYear);
                } else if (validationErrors.size() == 0) {
                    final BookModel book = new BookModel(author, title, year);
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("request_books");
                    final DatabaseReference counter = database.getReference("counter");
                    counter.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            counter.removeEventListener(this);
                            Long id = dataSnapshot.getValue(Long.class);
                            book.setId(id);
                            myRef.child(String.valueOf(id)).setValue(book);
                            Toast.makeText(getApplicationContext(), "Request added", Toast.LENGTH_SHORT).show();
                            counter.setValue(++id);
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:AddBookListene:93", databaseError.getMessage());
                        }
                    });
                }
            } catch (NumberFormatException nfe) {
                validationErrors.put("year", "Invalid format of year!");
            }
        }
        if(validationErrors.size()!=0){
            for(Map.Entry<String,String> entry : validationErrors.entrySet()){
                Toast.makeText(getApplicationContext(), entry.getValue(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
