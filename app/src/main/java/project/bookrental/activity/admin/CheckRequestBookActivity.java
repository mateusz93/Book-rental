package project.bookrental.activity.admin;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import project.bookrental.models.RequestBookModel;

/**
 * @author Mateusz Wieczorek
 */
public class CheckRequestBookActivity extends AppCompatActivity {

    ListView listView;
    List<RequestBookModel> listOfRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_request_book);
        listView = (ListView) findViewById(R.id.CheckRequestBookListView);
        getAllBooksFromDatabase();
    }

    void getAllBooksFromDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("request_books");
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
                listOfRequests.clear();
                for (Map<String, Object> fields : books) {
                    if (fields == null) continue;
                    Long id = (Long) fields.get("id");
                    String author = (String) fields.get("author");
                    Integer year = ((Long) fields.get("year")).intValue();
                    String title = (String) fields.get("title");
                    listOfRequests.add(new RequestBookModel(id, author, title, year));
                }
                RequestBookAdapter requestBookAdapter = new RequestBookAdapter(CheckRequestBookActivity.this, listOfRequests);
                listView.setAdapter(requestBookAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Err:listofbooks:", databaseError.toException());
            }
        });
    }

    private class RequestBookAdapter extends ArrayAdapter<RequestBookModel> {

        RequestBookAdapter(Context context, List<RequestBookModel> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final RequestBookModel book = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_check_request_book_layout, parent, false);
            }
            TextView textView = convertView.findViewById(R.id.listText);
            Button button = convertView.findViewById(R.id.checkRequestBookButton);
            textView.setText(book != null ? book.toString() : "");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("request_books");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            myRef.removeEventListener(this);
                            myRef.child(String.valueOf(book.getId())).removeValue();
                            Toast.makeText(getApplicationContext(), "Request removed!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("err:RemBookListene:183", databaseError.getMessage());
                        }
                    });
                }
            });
            return convertView;
        }
    }
}