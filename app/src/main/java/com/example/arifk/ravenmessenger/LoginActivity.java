package com.example.arifk.ravenmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jgabrielfreitas.core.BlurImageView;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText inputEmail, inputPassword;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        blurBackgroundImage();

        firebaseAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

        loadingBar = findViewById(R.id.loadingBar);


    }

    private void blurBackgroundImage() {
        BlurImageView blurImageView;

        blurImageView = findViewById(R.id.backgroundImage);
        blurImageView.setBlur(2);
    }

    public void registerPage(View view) {

        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void loginAccount(View view) {

        String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email))
            Toast.makeText(getApplicationContext(), "Enter Eamil", Toast.LENGTH_SHORT).show();

        if (TextUtils.isEmpty(password))
            Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_SHORT).show();


        loadingBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                loadingBar.setVisibility(View.GONE);

                if (task.isSuccessful())
                {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    String errorMessage = task.getException().toString();
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
