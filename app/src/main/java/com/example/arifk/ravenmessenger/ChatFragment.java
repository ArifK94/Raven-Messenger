package com.example.arifk.ravenmessenger;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class ChatFragment extends Fragment {

    private View mView;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private DocumentReference messageRef;

    private ConversationAdapater chatAdapater;

    private static final String KEY_COLLECTION_MESSAGES = "Messages";

    private RecyclerView mChatRecyclerView;


    private String currentUserID;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_chat, container, false);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null)
        {
            currentUserID = firebaseAuth.getCurrentUser().getUid();
            messageRef = db.collection(KEY_COLLECTION_MESSAGES).document(currentUserID);

            setupRecyclerView();
        }

        return mView;
    }

    private void setupRecyclerView() {
        final Query query =  messageRef.collection("Conversations")
                .orderBy("time", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class)
                .build();

        chatAdapater = new ConversationAdapater(options, currentUserID);

        mChatRecyclerView = mView.findViewById(R.id.chat_list);
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mChatRecyclerView.setAdapter(chatAdapater);

//        swipeToDeleteChat();
    }

    private void swipeToDeleteChat()
    {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                chatAdapater.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(mChatRecyclerView);
    }

    @Override
    public void onStart() {

        super.onStart();

        if (firebaseAuth.getCurrentUser() != null)
            chatAdapater.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (firebaseAuth.getCurrentUser() != null)
            chatAdapater.stopListening();
    }

}
