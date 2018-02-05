package project.bookrental.activity.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import project.bookrental.R;
import project.bookrental.models.BorrowedBookModel;
import project.bookrental.models.ConfirmationBookModel;
import project.bookrental.models.ConfirmationType;
import project.bookrental.models.UserModel;


/**
 * Created by marcin on 04.02.18.
 */

public class CheckStateBookActivity extends AppCompatActivity {
    public final static String SOUL = "vMv48nFoBWvfREAFBjKVvQWAZkEIRhLV9TBYKS2A";
    public boolean isAdmin = false;
    public HashMap<String, UserModel> usersMap = new HashMap<>();
    public Activity activity;

    private IntentIntegrator qrScan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.acitivty_common_check_state_book);
            qrScan = new IntentIntegrator(this);
            checkIfLoggedUserIsAdmin();
            updateUsersMap();
            qrScan.initiateScan();
            activity = this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else if(result.getContents().startsWith(SOUL)){
                final String bookId = result.getContents().replace(SOUL,"");
                final DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("borrowed_books");
                booksRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        booksRef.removeEventListener(this);
                        if (dataSnapshot.getValue() == null) {
                            return;
                        }
                        HashMap<String,Object> mapEntries = (HashMap<String,Object>) dataSnapshot.getValue();
                        HashMap<String,Object> book = (HashMap<String,Object>) mapEntries.get(bookId);
                        if(book!=null){
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            Date date = dataSnapshot.child(bookId).getValue(BorrowedBookModel.class).getBorrowDate();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.add(Calendar.DAY_OF_YEAR,1);
                            Date untilDate = calendar.getTime();
                            builder.setTitle("Book already borrowed!")
                                    .setMessage("This book is already borrowed by" + usersMap.get(book.get("userId")).getEmail() +" until " + untilDate)
                                    .setPositiveButton("Continue scanning!", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            qrScan.initiateScan();
                                        }
                                    })
                                    .setNegativeButton("End scanning!", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).show();
                        } else {
                            final DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("confirmations");
                            booksRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    booksRef.removeEventListener(this);
                                    if (dataSnapshot.getValue() == null) {
                                        return;
                                    }
                                    HashMap<String,Object> mapEntries = (HashMap<String,Object>) dataSnapshot.getValue();
                                    HashMap<String,Object> book = (HashMap<String,Object>) mapEntries.get(bookId);
                                    if(book!=null){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                        Date date = dataSnapshot.child(bookId).getValue(ConfirmationBookModel.class).getDatetime();
                                        builder.setTitle("Book already reserved!")
                                                .setMessage("This book is already reserved by" + usersMap.get(book.get("userId")).getEmail() +" until " + date)
                                                .setPositiveButton("Continue scanning!", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        qrScan.initiateScan();
                                                    }
                                                })
                                                .setNegativeButton("End scanning!", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                }).show();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                        if(isAdmin) {
                                            builder.setTitle("Book is available!")
                                                    .setMessage("Book is available.")
                                                    .setPositiveButton("Continue scanning!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            qrScan.initiateScan();
                                                        }
                                                    })
                                                    .setNegativeButton("End scanning!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    }).show();
                                        } else {
                                            builder.setTitle("Book is available!")
                                                    .setMessage("Book is available. Do you want reserve this book??")
                                                    .setPositiveButton("Continue scanning!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            qrScan.initiateScan();
                                                        }
                                                    })
                                                    .setNeutralButton("Reserve!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            final ConfirmationBookModel confirmationBookModel = new ConfirmationBookModel();
                                                            confirmationBookModel.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                            confirmationBookModel.setBookId(Long.valueOf(bookId));
                                                            confirmationBookModel.setType(ConfirmationType.BORROW);
                                                            Date now = new Date();
                                                            now.setTime(now.getTime() + 24 * 60 * 60 * 1000);
                                                            confirmationBookModel.setDatetime(now);
                                                            booksRef.child(bookId).setValue(confirmationBookModel);
                                                            Toast.makeText(getApplicationContext(), "Book reserved", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    })
                                                    .setNegativeButton("End scanning!", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    }).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w("Err:listofbooks:", databaseError.toException());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Err:listofbooks:", databaseError.toException());
                    }
                });
            }
        } else {
            Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
        }
    }

    private void checkIfLoggedUserIsAdmin() {
        final DatabaseReference users = FirebaseDatabase.getInstance().getReference("admin_users");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.removeEventListener(this);
                if (dataSnapshot.getValue() == null) {
                    return;
                }
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
                Toast.makeText(getApplicationContext(),"isAdmin: "+isAdmin,Toast.LENGTH_SHORT);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("err:CheckState:184", databaseError.getMessage());
            }
        });
    }

    private void updateUsersMap() {
        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersRef.removeEventListener(this);
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    usersMap.put(user.getUId(), user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
