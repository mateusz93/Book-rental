package project.bookrental;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

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
        Integer[] buttonNames = new Integer[] {R.id.addBookButton,R.id.removeBookButton, R.id.borrowBookButton, R.id.returnBookButton, R.id.aboutButton};
        for (int i = 0; i < buttonNames.length; i++) {
            Button button = (Button)findViewById(buttonNames[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(v.getId()){
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
                        case R.id.aboutButton:
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    }
                }
            });
        }
    }
}