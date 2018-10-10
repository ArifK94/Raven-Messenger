package com.example.arifk.ravenmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText passwordCheck, confirmedPass, email_ID;
    private CardView cardView_CreateAccount;
    private ProgressDialog mRegProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        blurBackgroundImage();

        firebaseAuth = FirebaseAuth.getInstance();

        mRegProgress = new ProgressDialog(this);

        passwordCheck = findViewById(R.id.txt_Password);
        confirmedPass = findViewById(R.id.txt_ConfirmedPass);
        email_ID = findViewById(R.id.txt_Email);
        cardView_CreateAccount = findViewById(R.id.btn_CreateAccount);

        setBtnState(false);
        update();

        cardView_CreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserAccount();
            }
        });
    }

    private void blurBackgroundImage() {
        BlurImageView blurImageView;

        blurImageView = findViewById(R.id.backgroundImage);
        blurImageView.setBlur(2);
    }

    private void update() {
        Thread t = new Thread() {
            @Override
            public void run() {

                while (!isInterrupted()) {

                    try {
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                if (!isEmailValid(email_ID) || passwordCheck.getText().toString().isEmpty() || confirmedPass.getText().toString().isEmpty())
                                    setBtnState(false);
                                else
                                    setBtnState(true);
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    public static boolean isEmailValid(EditText email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email.getText().toString());
        return matcher.matches();
    }


    private void createUserAccount() {

        String email = email_ID.getText().toString();
        String password = passwordCheck.getText().toString();
        String confirmedPassword = confirmedPass.getText().toString();

        if (password.equals(confirmedPassword)) {

            showProcessDialog();
            registerUser(email, password);

        } else
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();


    }

    private void registerUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    mRegProgress.dismiss();

                    Intent intent = new Intent(RegisterActivity.this, AccountSetupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    mRegProgress.hide();

                    String errorMessage = task.getException().toString();
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void loginPage(View view) {

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void showProcessDialog() {
        mRegProgress.setTitle("Register");
        mRegProgress.setMessage("Please wait until your account is registering!");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();
    }

    private void setBtnState(boolean state) {
        if (state) {
            cardView_CreateAccount.setEnabled(true);
            cardView_CreateAccount.setAlpha(1.0f);
        } else {
            cardView_CreateAccount.setEnabled(false);
            cardView_CreateAccount.setAlpha(.5f);
        }
    }
}
