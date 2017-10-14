package project.bookrental;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by marcin on 14.10.17.
 */

public class AddBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // TODO: add separate views for every Activity
        workOnDatabase();
    }

    void workOnDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Log.d("MESSAGE:", value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("MESSAGE:", databaseError.toException());
            }
        });
        Log.d("BEFORE MESSAGE","zapisujemy!");
        myRef.setValue("Hello, World!");
    }
};