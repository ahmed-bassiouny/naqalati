package com.bassiouny.naqalati.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bassiouny.naqalati.helper.SharedPref;
import com.bassiouny.naqalati.model.User;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.bassiouny.naqalati.R;
import com.bassiouny.naqalati.helper.Utils;
import com.bassiouny.naqalati.model.FirebaseRoot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.fabric.sdk.android.Fabric;

public class SigninActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private ProgressBar progress;
    private TextView tvNewUser;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_signin);
        findViewById();
        initObjects();
        onClick();

    }

    private void onClick() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPhone.getText().toString().trim().length() != 11) {
                    etPhone.setError(getString(R.string.enter_phone));
                } else if (etPassword.getText().toString().trim().length() < 6) {
                    etPassword.setError(getString(R.string.invalid_password));
                } else {
                    startLogin();
                    signIn(Utils.convertPhoneToEmail(etPhone.getText().toString()), etPassword.getText().toString());
                }
            }
        });
        tvNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SigninActivity.this, SignupActivity.class));
            }
        });
    }

    private void findViewById() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        progress = findViewById(R.id.progress);
        tvNewUser = findViewById(R.id.tv_new_user);
        btnSubmit = findViewById(R.id.btn_submit);
    }

    private void initObjects() {
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            startLogin();
            getInfoFromDB(currentUser.getUid());
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user.getDisplayName() != null && user.getDisplayName().equals(FirebaseRoot.DB_USER))
                                updateUI(user);
                            else {
                                stopLogin();
                                FirebaseAuth.getInstance().signOut();
                                Utils.showErrorDialog(SigninActivity.this, getString(R.string.user_not_found));
                            }
                        } else {
                            if (Utils.isNetworkConnected(SigninActivity.this)) {
                                stopLogin();
                                Utils.showErrorDialog(SigninActivity.this, getString(R.string.phone_password_invalid));
                            } else {
                                stopLogin();
                                Utils.showWarningDialog(SigninActivity.this, getString(R.string.check_internet));
                            }
                        }
                    }
                });
    }

    private void startLogin() {
        btnSubmit.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
    }

    private void stopLogin() {
        btnSubmit.setVisibility(View.VISIBLE);
        progress.setVisibility(View.INVISIBLE);
    }

    private void getInfoFromDB(String myId) {
        FirebaseDatabase.getInstance().getReference(FirebaseRoot.DB_USER).child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null && user.getUserPhone()!= null) {
                    if (user.getBlocked()) {
                        startActivity(new Intent(SigninActivity.this, ExpiredActivity.class));
                        finish();
                        stopLogin();
                    } else {
                        stopLogin();
                        startActivity(new Intent(SigninActivity.this, OptionActivity.class));
                        finish();
                    }
                } else {
                    Utils.ContactSuppot(SigninActivity.this);
                }
                stopLogin();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.ContactSuppot(SigninActivity.this);
                stopLogin();
            }
        });
    }
    @Override
    public void onBackPressed() {
        System.exit(0);
    }
}
