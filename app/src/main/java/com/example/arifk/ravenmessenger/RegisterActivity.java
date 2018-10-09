package com.example.arifk.ravenmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jgabrielfreitas.core.BlurImageView;


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

    public void createAccount(View view) {
    }

    private void createUserAccount()
    {
        String email = email_ID.getText().toString();
        String password = passwordCheck.getText().toString();
        String confirmedPassword = confirmedPass.getText().toString();

        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password) || !TextUtils.isEmpty(confirmedPassword)) {
            if (password.equals(confirmedPassword))
            {

                showProcessDialog();
                registerUser(email, password);

            }
            else
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Missing fields", Toast.LENGTH_SHORT).show();

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

    private void showProcessDialog()
    {
        mRegProgress.setTitle("Register");
        mRegProgress.setMessage("Please wait until your account is registering!");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();
    }
}
