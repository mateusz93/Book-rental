package project.bookrental.util;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;

/**
 * @author Mateusz Wieczorek
 */
public final class AuthenticationUtils {

    private static boolean isAdmin;

    public static boolean isLoggedAsAdmin(final String currentUserEmail) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            return false;
        }
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference users = database.getReference("admin_users");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    isAdmin = false;
                    return;
                }
                isAdmin = false;
                if (CollectionUtils.isNotEmpty((List) dataSnapshot.getValue())) {
                    for(Object field : (List) dataSnapshot.getValue()){
                        if (field != null) {
                            final HashMap<String, Object> fields = (HashMap<String, Object>) field;
                            final String email = (String) fields.get("email");
                            if (currentUserEmail.equalsIgnoreCase(email)) {
                                isAdmin = true;
                                break;
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("err:AddBookListene:93", databaseError.getMessage());
            }
        });
        return isAdmin;
    }

}
