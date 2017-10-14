package project.bookrental;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addOnClickListenersOnButtons();
    }

    private void addOnClickListenersOnButtons() {
        Integer[] buttonNames = new Integer[] {R.id.addBookButton,R.id.removeBookButton, R.id.borrowBookButton, R.id.returnBookButton, R.id.aboutButton, R.id.exitButton};
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
                            break;
                        case R.id.exitButton:
                            finishAffinity();
                    }
                }
            });
        }
    }
}
