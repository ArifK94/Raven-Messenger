package com.example.arifk.ravenmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText inputEmail, inputPassword;
    private CardView cardView_Login;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        blurBackgroundImage();

        firebaseAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        cardView_Login = findViewById(R.id.loginCardView);
        loadingBar = findViewById(R.id.loadingBar);

        setBtnState(false);
        update();
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

                                if (!isEmailValid(inputEmail) || inputPassword.getText().toString().isEmpty())
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


    public void loginAccount(View view) {

        String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

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

    private void setBtnState(boolean state) {
        if (state) {
            cardView_Login.setEnabled(true);
            cardView_Login.setAlpha(1.0f);
        } else {
            cardView_Login.setEnabled(false);
            cardView_Login.setAlpha(.5f);
        }
    }
}
