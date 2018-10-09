package com.example.arifk.ravenmessenger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FriendListActivity extends AppCompatActivity {

    private CollectionReference userRef;

    private UserAdapater userAdapater;

    private Toolbar mToolbar;

    private static final String KEY_COLLECTION_FRIENDS = "Friends";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        userRef = db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).collection(KEY_COLLECTION_FRIENDS);

        setupRecyclerView();

        getToolbarSettings();
    }

    private void getToolbarSettings()
    {
        mToolbar = findViewById(R.id.friendList_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView()
    {
        Query query = userRef.orderBy("name", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        userAdapater = new UserAdapater(options);

        RecyclerView mUsersList = findViewById(R.id.recyclerView_friendList);
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
