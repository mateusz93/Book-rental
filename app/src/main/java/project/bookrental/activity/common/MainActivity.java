package project.bookrental.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;

import project.bookrental.R;
import project.bookrental.activity.admin.AddBookActivity;
import project.bookrental.activity.admin.CheckRequestBookActivity;
import project.bookrental.activity.admin.ConfirmActivity;
import project.bookrental.activity.admin.RemoveBookActivity;
import project.bookrental.activity.student.BorrowBookActivity;
import project.bookrental.activity.student.RequestBookActivity;
import project.bookrental.activity.student.ReturnBookActivity;

/**
 * @author Marcin Korycki
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_main);
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        setMenuVisibility();
        addOnClickListenersOnButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void addOnClickListenersOnButtons() {
        Integer[] buttonNames = new Integer[]{
                R.id.listOfBooksButton,
                R.id.addBookButton,
                R.id.removeBookButton,
                R.id.borrowBookButton,
                R.id.returnBookButton,
                R.id.requestBookButton,
                R.id.adminReturnBookButton,
                R.id.adminBorrowBookButton,
                R.id.adminConfirmBorrowBookButton,
                R.id.checkRequestBookButton,
                R.id.aboutButton,
                R.id.sign_out};
        for (int i = 0; i < buttonNames.length; i++) {
            Button button = (Button) findViewById(buttonNames[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.listOfBooksButton:
                            startActivity(new Intent(MainActivity.this, ListOfBooksActivity.class));
                            break;
                        case R.id.addBookButton:
                            startActivity(new Intent(MainActivity.this, AddBookActivity.class));
                            break;
                        case R.id.removeBookButton:
                            startActivity(new Intent(MainActivity.this, RemoveBookActivity.class));
                            break;
                        case R.id.borrowBookButton:
                            startActivity(new Intent(MainActivity.this, BorrowBookActivity.class));
                            break;
                        case R.id.returnBookButton:
                            startActivity(new Intent(MainActivity.this, ReturnBookActivity.class));
                            break;
                        case R.id.requestBookButton:
                            startActivity(new Intent(MainActivity.this, RequestBookActivity.class));
                            break;
                        case R.id.adminReturnBookButton:
                            startActivity(new Intent(MainActivity.this, project.bookrental.activity.admin.ReturnBookActivity.class));
                            break;
                        case R.id.adminConfirmBorrowBookButton:
                            startActivity(new Intent(MainActivity.this, ConfirmActivity.class));
                            break;
                        case R.id.adminBorrowBookButton:
                            startActivity(new Intent(MainActivity.this, project.bookrental.activity.admin.BorrowBookActivity.class));
                            break;
                        case R.id.checkRequestBookButton:
                            startActivity(new Intent(MainActivity.this, CheckRequestBookActivity.class));
                            break;
                        case R.id.aboutButton:
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        case R.id.sign_out:
                            findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    auth.signOut();
                                }
                            });
                    }
                }
            });
        }
    }

    private void setMenuVisibility() {
        final DatabaseReference users = FirebaseDatabase.getInstance().getReference("admin_users");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                boolean isAdmin = false;
                if (CollectionUtils.isNotEmpty((List) dataSnapshot.getValue())) {
                    for (Object field : (List) dataSnapshot.getValue()) {
                        if (field != null) {
                            final HashMap<String, Object> fields = (HashMap<String, Object>) field;
                            final String email = (String) fields.get("email");
                            if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(email)) {
                                isAdmin = true;
                                break;
                            }
                        }
                    }
                }
                LinearLayout menu = (LinearLayout) findViewById(R.id.menu);
                if (isAdmin) {
                    menu.removeView(findViewById(R.id.borrowBookButton));
                    menu.removeView(findViewById(R.id.returnBookButton));
                    menu.removeView(findViewById(R.id.requestBookButton));
                } else {
                    menu.removeView(findViewById(R.id.adminConfirmBorrowBookButton));
                    menu.removeView(findViewById(R.id.adminBorrowBookButton));
                    menu.removeView(findViewById(R.id.adminReturnBookButton));
                    menu.removeView(findViewById(R.id.addBookButton));
                    menu.removeView(findViewById(R.id.removeBookButton));
                    menu.removeView(findViewById(R.id.checkRequestBookButton));
                }
                menu.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("err:AddBookListene:93", databaseError.getMessage());
            }
        });
    }
}