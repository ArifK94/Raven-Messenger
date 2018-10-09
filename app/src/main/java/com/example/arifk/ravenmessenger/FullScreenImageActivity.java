package com.example.arifk.ravenmessenger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ImageView fullScreenImageView = findViewById(R.id.img_fullScreen);

        Intent callingActivityIntent = getIntent();
        if (callingActivityIntent != null) {
            Uri mImageUri = callingActivityIntent.getData();
            if (mImageUri != null) {

                Picasso.get().load(mImageUri).into(fullScreenImageView);

            }
        }

        setupToolbar();
    }

    private void setupToolbar()
    {
        String name = null;

        if (getIntent().hasExtra("title"))
            name = getIntent().getStringExtra("title");


        Toolbar mToolbar = findViewById(R.id.full_screen_image_app_bar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle(name);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
