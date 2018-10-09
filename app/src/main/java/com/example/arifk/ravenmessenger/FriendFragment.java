package com.example.arifk.ravenmessenger;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class FriendFragment extends Fragment {

    private View mView;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private CollectionReference userRef;

    private UserAdapater userAdapater;

    private static final String KEY_COLLECTION_FRIENDS = "Friends";

    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_friend, container, false);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null)
        {
            userRef = db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).collection(KEY_COLLECTION_FRIENDS);

            setupRecyclerView();
        }


        return mView;
    }

    private void setupRecyclerView() {
        Query query = userRef.orderBy("name", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        userAdapater = new UserAdapater(options);

        RecyclerView mUsersList = mView.findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsersList.setAdapter(userAdapater);
    }

    @Override
    public void onStart() {

        super.onStart();

        if (firebaseAuth.getCurrentUser() != null)
            userAdapater.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (firebaseAuth.getCurrentUser() != null)
            userAdapater.stopListening();
    }
}
