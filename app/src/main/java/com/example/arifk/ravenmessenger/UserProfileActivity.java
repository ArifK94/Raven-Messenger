package com.example.arifk.ravenmessenger;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private DocumentReference currentUserRef, profileUserRef;

    private static final String KEY_COLLECTION_FRIENDS = "Friends";
    private static final String KEY_COLLECTION_FRIEND_REQUESTS_SENT = "Friend Requests Sent";
    private static final String KEY_COLLECTION_FRIEND_REQUESTS_RECEIVED = "Friend Requests Received";

    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR_IMAGE = "avatar";
    private static final String KEY_BACKGROUND_IMAGE = "background_image";

    private String mCurrentState = "";
    private static final String KEY_SEND_REQUEST = "Send Friend Request";
    private static final String KEY_DELETE_FRIEND_REQUEST = "Delete Request";
    private static final String KEY_ACCEPT_FRIEND_REQUEST = "Accept Friend Request";
    private static final String KEY_REMOVE_FRIEND = "Remove Friend";

    private String profile_UserID, name, username, image, bgImage;
    private String currentUserID, currentUser_Name, currentUser_Username, currentUser_Image;

    private LinearLayout linearLayout;
    private TextView txtView_DisplayName, mTxtView_Username;
    private ImageView avatar, backgroundImage;
    private Button btn_Friend_State;
    private CardView cardview_friendInvite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar mToolbar = findViewById(R.id.user_profile_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("User Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        profile_UserID = getIntent().getStringExtra("user_id");
        profileUserRef = db.collection("Users").document(profile_UserID);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserRef = db.collection("Users").document(currentUserID);

        linearLayout = findViewById(R.id.linear_layout_root);
        txtView_DisplayName = findViewById(R.id.txt_displayname);
        mTxtView_Username = findViewById(R.id.txt_username);
        avatar = findViewById(R.id.img_Avatar);
        backgroundImage = findViewById(R.id.backgroundImage);
        btn_Friend_State = findViewById(R.id.btn_friend_state);
        cardview_friendInvite = findViewById(R.id.cardview_friend_invite);

        cardview_friendInvite.setVisibility(View.GONE);

        checkIfSameUser();


        if (!currentUserID.isEmpty() && !profile_UserID.isEmpty())
        {
            loadData();
            getCurrentUserDetails();
            checkState();
            UpdateState();
        }
    }


    private void checkIfSameUser()
    {
        if (currentUserID.equals(profile_UserID))
            btn_Friend_State.setEnabled(false);
        else
            btn_Friend_State.setEnabled(true);
    }


    private void loadData() {
        profileUserRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            name = documentSnapshot.getString(KEY_NAME);
                            username = documentSnapshot.getString(KEY_USERNAME);
                            image = documentSnapshot.getString(KEY_AVATAR_IMAGE);
                            bgImage = documentSnapshot.getString(KEY_BACKGROUND_IMAGE);

                            txtView_DisplayName.setText(name);
                            mTxtView_Username.setText(username);

                            Picasso.get().load(image).into(avatar);

                            if (!bgImage.equals("default"))
                                Picasso.get().load(bgImage).into(backgroundImage);

                        } else {
                            Toast.makeText(UserProfileActivity.this, "Could not retrieve data", Toast.LENGTH_SHORT).show();

                            Picasso.get().load(R.drawable.avatar_default).into(avatar);
                            Picasso.get().load(R.drawable.avatar_default).into(backgroundImage);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UpdateState() {
        Thread t = new Thread() {
            @Override
            public void run() {

                while (!isInterrupted()) {

                    try {
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                if (mCurrentState.equals(KEY_ACCEPT_FRIEND_REQUEST))
                                    stateAcceptFriendRequest();
                                else if (mCurrentState.equals(KEY_DELETE_FRIEND_REQUEST))
                                    stateRemoveFriendRequest();
                                else if (mCurrentState.equals(KEY_REMOVE_FRIEND))
                                    stateRemoveFriend();
                                else
                                    stateSendFriendRequest();

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

    private void checkState() {
        currentUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_RECEIVED).document(profile_UserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    mCurrentState = KEY_ACCEPT_FRIEND_REQUEST;
                }
            }
        });

        currentUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_SENT).document(profile_UserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    mCurrentState = KEY_DELETE_FRIEND_REQUEST;
                }
            }
        });

        currentUserRef.collection(KEY_COLLECTION_FRIENDS).document(profile_UserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    mCurrentState = KEY_REMOVE_FRIEND;
                }
            }
        });

        if (!mCurrentState.equals(KEY_ACCEPT_FRIEND_REQUEST) || !mCurrentState.equals(KEY_DELETE_FRIEND_REQUEST) || !mCurrentState.equals(KEY_REMOVE_FRIEND)) {
            mCurrentState = KEY_SEND_REQUEST;
        }
    }

    /**
     * ------------------------------------------------- States -------------------------------------------------
     */

    private void stateSendFriendRequest() {

        btn_Friend_State.setText(KEY_SEND_REQUEST);
        btn_Friend_State.setVisibility(View.VISIBLE);
        cardview_friendInvite.setVisibility(View.GONE);
    }

    private void stateAcceptFriendRequest() {

        btn_Friend_State.setVisibility(View.GONE);
        cardview_friendInvite.setVisibility(View.VISIBLE);
    }

    private void stateRemoveFriendRequest() {

        btn_Friend_State.setText(KEY_DELETE_FRIEND_REQUEST);
        btn_Friend_State.setVisibility(View.VISIBLE);
        cardview_friendInvite.setVisibility(View.GONE);
    }

    private void stateRemoveFriend() {

        btn_Friend_State.setText(KEY_REMOVE_FRIEND);
        btn_Friend_State.setVisibility(View.VISIBLE);
        cardview_friendInvite.setVisibility(View.GONE);
    }

    /**
     * ------------------------------------------------- Profile User Data -------------------------------------------------
     */

    private void getCurrentUserDetails() {
        currentUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                currentUser_Name = documentSnapshot.getString(KEY_NAME);
                currentUser_Username = documentSnapshot.getString(KEY_USERNAME);
                currentUser_Image = documentSnapshot.getString(KEY_AVATAR_IMAGE);
            }
        });
    }

    private void request() {

        Map<String, Object> userReceiverMap = new HashMap<>();
        userReceiverMap.put(KEY_NAME, currentUser_Name);
        userReceiverMap.put(KEY_USERNAME, currentUser_Username);
        userReceiverMap.put(KEY_AVATAR_IMAGE, currentUser_Image);

        profileUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_RECEIVED).document(currentUserID).set(userReceiverMap);
    }

    private void acceptRequest() {
        Map<String, Object> userReceiverMap = new HashMap<>();
        userReceiverMap.put(KEY_NAME, currentUser_Name);
        userReceiverMap.put(KEY_USERNAME, currentUser_Username);
        userReceiverMap.put(KEY_AVATAR_IMAGE, currentUser_Image);

        profileUserRef.collection(KEY_COLLECTION_FRIENDS).document(currentUserID).set(userReceiverMap);
    }

    /**
     * ------------------------------------------------- Current User Data -------------------------------------------------
     */


    private void sendFriendRequest() {
        Map<String, Object> userRequestMap = new HashMap<>();
        userRequestMap.put(KEY_NAME, name);
        userRequestMap.put(KEY_USERNAME, username);
        userRequestMap.put(KEY_AVATAR_IMAGE, image);

        currentUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_SENT).document(profile_UserID).set(userRequestMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                request();
                setSnackMessage("Friend Request Sent");

            }
        });
    }

    private void acceptFriendRequest() {
        Map<String, Object> userRequestMap = new HashMap<>();
        userRequestMap.put(KEY_NAME, name);
        userRequestMap.put(KEY_USERNAME, username);
        userRequestMap.put(KEY_AVATAR_IMAGE, image);

        currentUserRef.collection(KEY_COLLECTION_FRIENDS).document(profile_UserID).set(userRequestMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mCurrentState = KEY_REMOVE_FRIEND;

                acceptRequest();
                deleteFriendRequest();

                setSnackMessage("Friend Request Accepted");
            }
        });
    }

    /**
     * Remove both sent and received friend request
     */
    private void deleteFriendRequest() {

        // delete current user's sent request
        currentUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_SENT).document(profile_UserID).delete();

        // delete current user's received request
        currentUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_RECEIVED).document(profile_UserID).delete();



        // delete user profile's received request
        profileUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_RECEIVED).document(currentUserID).delete();

        // delete user profile's sent request
        profileUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_SENT).document(currentUserID).delete();

    }

    private void declineRequest() {

        // delete current user's sent request
        currentUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_RECEIVED).document(profile_UserID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                // delete user profile's received request
                profileUserRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_SENT).document(currentUserID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mCurrentState = KEY_SEND_REQUEST;

                    }
                });
            }
        });
    }

    private void removeFriend() {

        // delete from current user's list
        currentUserRef.collection(KEY_COLLECTION_FRIENDS).document(profile_UserID).delete();

        // delete from user profile's list
        profileUserRef.collection(KEY_COLLECTION_FRIENDS).document(currentUserID).delete();

        mCurrentState = KEY_SEND_REQUEST;

        setSnackMessage("Friend Removed");

    }


    public void setState(View view) {

        if (mCurrentState.equals(KEY_SEND_REQUEST))
            sendFriendRequest();
        else if (mCurrentState.equals(KEY_REMOVE_FRIEND))
            removeFriend();
        else if (mCurrentState.equals(KEY_DELETE_FRIEND_REQUEST))
        {
            deleteFriendRequest();
            mCurrentState = KEY_SEND_REQUEST;
        }

    }

    public void acceptFriendRequest(View view) {
        acceptFriendRequest();
    }

    public void declineFriendRequest(View view) {
        declineRequest();
    }

    private void setSnackMessage(String message) {
        Snackbar snackbar = Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT);

        View view = snackbar.getView();
        TextView txtv = view.findViewById(android.support.design.R.id.snackbar_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            txtv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            txtv.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        snackbar.show();

    }

    public void messageUser(View view) {

        Intent profileIntent = new Intent(UserProfileActivity.this, ChatRoomActivity.class);
        profileIntent.putExtra("profileUser_ID", profile_UserID);
        profileIntent.putExtra("profileUser_Name", name);

        startActivity(profileIntent);
    }

    public void selectProfileImage(View view) {

        navigateToFullScreenImage(image);

    }

    public void selectBackgroundImage(View view) {

        navigateToFullScreenImage(bgImage);

    }

    private void navigateToFullScreenImage(String image)
    {
        Intent fullScreenIntent = new Intent(UserProfileActivity.this, FullScreenImageActivity.class);
        fullScreenIntent.setData(Uri.parse(image));
        fullScreenIntent.putExtra("title", name);
        startActivity(fullScreenIntent);
    }
}
