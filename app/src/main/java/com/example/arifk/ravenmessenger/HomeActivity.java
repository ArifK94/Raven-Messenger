package com.example.arifk.ravenmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void loginPage(View view) {

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void registerPage(View view) {

        Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
