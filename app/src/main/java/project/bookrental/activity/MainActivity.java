package project.bookrental.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import project.bookrental.R;
import project.bookrental.util.AuthenticationUtils;

public class MainActivity extends AppCompatActivity {

    private LinearLayout menu;
    private Button signOut;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        signOut = (Button) findViewById(R.id.sign_out);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        addOnClickListenersOnButtons();
    }

    //sign out method
    public void signOut() {
        auth.signOut();
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
        Integer[] buttonNames = new Integer[] {
                R.id.listOfBooksButton,
                R.id.addBookButton,
                R.id.removeBookButton,
                R.id.borrowBookButton,
                R.id.returnBookButton,
                R.id.requestBookButton,
                R.id.checkRequestBookButton,
                R.id.aboutButton};
        for (int i = 0; i < buttonNames.length; i++) {
            Button button = (Button)findViewById(buttonNames[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(v.getId()){
                        case R.id.listOfBooksButton:
                            startActivity(new Intent(MainActivity.this, ListOfBooksActivity.class));
                            break;
                        case R.id.addBookButton:
                            if (AuthenticationUtils.isLoggedAsAdmin(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                startActivity(new Intent(MainActivity.this, AddBookActivity.class));
                            }
                            break;
                        case R.id.removeBookButton:
                            if (AuthenticationUtils.isLoggedAsAdmin(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                startActivity(new Intent(MainActivity.this, RemoveBookActivity.class));
                            }
                            break;
                        case R.id.borrowBookButton:
                            if (!AuthenticationUtils.isLoggedAsAdmin(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                startActivity(new Intent(MainActivity.this, BorrowBookActivity.class));
                            }
                            break;
                        case R.id.returnBookButton:
                            if (!AuthenticationUtils.isLoggedAsAdmin(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                startActivity(new Intent(MainActivity.this, ReturnBookActivity.class));
                            }
                            break;
                        case R.id.requestBookButton:
                            if (!AuthenticationUtils.isLoggedAsAdmin(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                startActivity(new Intent(MainActivity.this, RequestBookActivity.class));
                            }
                            break;
                        case R.id.checkRequestBookButton:
                            if (AuthenticationUtils.isLoggedAsAdmin(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                startActivity(new Intent(MainActivity.this, CheckRequestBookActivity.class));
                            }
                            break;
                        case R.id.aboutButton:
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    }
                }
            });
        }
        setButtonVisibility();
    }

    private void setButtonVisibility() {
        menu = (LinearLayout) findViewById(R.id.menu);
        if (AuthenticationUtils.isLoggedAsAdmin(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
            menu.removeView(findViewById(R.id.borrowBookButton));
            menu.removeView(findViewById(R.id.returnBookButton));
            menu.removeView(findViewById(R.id.requestBookButton));
        } else {
            menu.removeView(findViewById(R.id.addBookButton));
            menu.removeView(findViewById(R.id.removeBookButton));
            menu.removeView(findViewById(R.id.checkRequestBookButton));
        }
    }
}