package project.bookrental.activity.common;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import project.bookrental.R;

/**
 * @author Mateusz Wieczorek
 */
public class AboutActivity extends AppCompatActivity {

    private EditText loggedAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_about);
        loggedAs = (EditText) findViewById(R.id.loggedAs);
        final String name = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";
        loggedAs.setText("Logged as: " + name);
    }

}