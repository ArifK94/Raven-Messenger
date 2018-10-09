package com.example.arifk.ravenmessenger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference userRef;

    private UserAdapater userAdapater;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = FirebaseFirestore.getInstance();
        userRef = db.collection("Users");

        setupRecyclerView();

        getToolbarSettings();

    }

    private void getToolbarSettings()
    {
        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView()
    {
        Query query = userRef.orderBy("name", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        userAdapater = new UserAdapater(options);

        RecyclerView mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mUsersList.setAdapter(userAdapater);
    }

    @Override
    protected void onStart() {

        super.onStart();

        userAdapater.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        userAdapater.stopListening();
    }
}
