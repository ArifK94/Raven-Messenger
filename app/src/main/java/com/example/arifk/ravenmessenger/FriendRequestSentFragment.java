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


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendRequestSentFragment extends Fragment {

    private View mView;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private CollectionReference userRef;

    private UserAdapater userAdapater;

    private static final String KEY_COLLECTION_FRIEND_REQUESTS_SENT = "Friend Requests Sent";

    public FriendRequestSentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_friend_request_sent, container, false);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userRef = db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).collection(KEY_COLLECTION_FRIEND_REQUESTS_SENT);

        setupRecyclerView();

        return mView;
    }

    private void setupRecyclerView() {
        Query query = userRef.orderBy("name", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        userAdapater = new UserAdapater(options);

        userAdapater.setmBackgroundResourceImage(R.drawable.colour_item_friend_sent_bg);
        RecyclerView mUsersList = mView.findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsersList.setAdapter(userAdapater);
    }

    @Override
    public void onStart() {

        super.onStart();

        userAdapater.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        userAdapater.stopListening();
    }

}
