package com.example.arifk.ravenmessenger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private DocumentReference profileUserRef, notificationRef;
    private CollectionReference messageRef;
    private StorageReference storageReference;

    private static final String KEY_COLLECTION_MESSAGES = "Messages";

    private static final String KEY_AVATAR_IMAGE = "avatar";
    private static final String KEY_SENDER = "sender";
    private static final String KEY_RECIPIENT = "recipient";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_TIMESTAMP = "time";
    private static final String KEY_TYPE = "type";
    private static final String KEY_MESSAGE_STATUS = "message_status";

    private String currentUserID, profile_UserID;
    private String profileUser_Name;
    private String image;
    private String mMessage_State;

    private ChatAdapter messageAdapter;
    private RecyclerView mMessageList;
    private TextView mTxtVwMessage;
    private ImageView mImageMessage;
    private ImageButton mBtnSend;
    private ProgressBar mProgressUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        profile_UserID = getIntent().getStringExtra("profileUser_ID");
        profileUser_Name = getIntent().getStringExtra("profileUser_Name");
        mMessage_State = getIntent().getStringExtra("message_status");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profileUserRef = db.collection("Users").document(profile_UserID);
        notificationRef = db.collection("Notifications").document(profile_UserID);

        messageRef = db.collection(KEY_COLLECTION_MESSAGES);

        mMessageList = findViewById(R.id.messages_list);
        mTxtVwMessage = findViewById(R.id.txt_chat_message);
        mImageMessage = findViewById(R.id.chat_message_image);
        mBtnSend = findViewById(R.id.btn_chat_send);
        mProgressUpload = findViewById(R.id.progressBar_upload);

        mProgressUpload.setVisibility(View.GONE);

        updateSendBtn();
        loadData();
        updateMessageState();

        showChatMessages();
        setupSenderRecyclerView();

    }

    private void updateSendBtn() {
        Thread t = new Thread() {
            @Override
            public void run() {

                while (!isInterrupted()) {

                    try {
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                if (!mTxtVwMessage.getText().toString().isEmpty() && imageUri == null)
                                    mBtnSend.setVisibility(View.VISIBLE);
                                else if (mTxtVwMessage.getText().toString().isEmpty() && imageUri != null)
                                    mBtnSend.setVisibility(View.VISIBLE);
                                else
                                    mBtnSend.setVisibility(View.INVISIBLE);

                                mTxtVwMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {


                                    }
                                });

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

    public void sendMessage(View view) {

        if (!mTxtVwMessage.getText().toString().isEmpty())
            sendTextMessage();

        if (imageUri != null)
            uploadImage();
    }

    public void addImage(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void setMessage(String message, String type) {

        final Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(KEY_SENDER, currentUserID);
        messageMap.put(KEY_RECIPIENT, profile_UserID);
        messageMap.put(KEY_MESSAGE, message);
        messageMap.put(KEY_TYPE, type);
        messageMap.put(KEY_MESSAGE_STATUS, "Delivered");
        messageMap.put(KEY_TIMESTAMP, System.currentTimeMillis());

        // collection of all chats
        messageRef.document(currentUserID).collection("Conversations").document(profile_UserID).collection("OnetoOneChat").add(messageMap);
        messageRef.document(profile_UserID).collection("Conversations").document(currentUserID).collection("OnetoOneChat").add(messageMap);

        // update latest chat
        messageRef.document(currentUserID).collection("Conversations").document(profile_UserID).set(messageMap);
        messageRef.document(profile_UserID).collection("Conversations").document(currentUserID).set(messageMap);

        notificationRef.collection("AllNotifications").add(messageMap);
        notificationRef.set(messageMap);
    }


    public void loadData() {
        profileUserRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            image = documentSnapshot.getString(KEY_AVATAR_IMAGE);

                            getToolbarSettings();


                        } else {
                            Toast.makeText(ChatRoomActivity.this, "Could not retrieve data", Toast.LENGTH_SHORT).show();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatRoomActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateMessageState() {
        updateCollectionDoc(currentUserID, profile_UserID);
        updateCollectionDoc(profile_UserID, currentUserID);

        // update latest chat
        messageRef.document(currentUserID).collection("Conversations").document(profile_UserID).update(KEY_MESSAGE_STATUS, mMessage_State);
        messageRef.document(profile_UserID).collection("Conversations").document(currentUserID).update(KEY_MESSAGE_STATUS, mMessage_State);
    }

    /**
     * Update all documents contained messages
     *
     * @param firstUserID
     * @param secondUserID
     */
    private void updateCollectionDoc(final String firstUserID, final String secondUserID) {
        messageRef.document(firstUserID).collection("Conversations").document(secondUserID).collection("OnetoOneChat").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();

                    for (DocumentSnapshot documentSnapshot : myListOfDocuments)
                        documentSnapshot.getReference().update(KEY_MESSAGE_STATUS, mMessage_State);


                }
            }
        });
    }

    private void getToolbarSettings() {
        Toolbar mToolbar = findViewById(R.id.app_bar_chat);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.app_bar_chat, null);

        actionBar.setCustomView(action_bar_view);

        TextView mNameView = findViewById(R.id.app_bar_chat_name);
        CircleImageView mAvatarView = findViewById(R.id.app_bar_chat_avatar);

        mNameView.setText(profileUser_Name);
        Picasso.get().load(image).into(mAvatarView);

        mAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(ChatRoomActivity.this, UserProfileActivity.class);
                profileIntent.putExtra("user_id", profile_UserID);
                startActivity(profileIntent);
            }
        });
    }


    private void sendTextMessage() {

        String message = mTxtVwMessage.getText().toString();

        setMessage(message, "text");

        mTxtVwMessage.setText("");
    }

    private void setupSenderRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        mMessageList.setLayoutManager(manager);

    }

    private void showChatMessages() {
        getChats(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("ChatRoomActivity", "Listen failed.", e);
                    return;
                }

                List<Chat> messages = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    messages.add(
                            new Chat(
                                    doc.getString(KEY_SENDER),
                                    doc.getString(KEY_RECIPIENT),
                                    doc.getString(KEY_MESSAGE),
                                    doc.getString(KEY_TYPE),
                                    doc.getString(KEY_MESSAGE_STATUS),
                                    doc.getLong(KEY_TIMESTAMP)
                            )
                    );
                }

//                if (messages.size() > 0) {
//                    if (currentUserID.equals(messages.get(0).getRecipient())) {
//                        mMessage_State = "Read";
//                        updateMessageState();
//                    }
//                }


                messageAdapter = new ChatAdapter(messages, currentUserID);
                mMessageList.setAdapter(messageAdapter);
            }
        });
    }

    public void getChats(EventListener<QuerySnapshot> listener) {
        messageRef.document(currentUserID).collection("Conversations").document(profile_UserID).collection("OnetoOneChat")
                .orderBy(KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {

            imageUri = data.getData();

            mTxtVwMessage.setVisibility(View.GONE);
            mImageMessage.setVisibility(View.VISIBLE);

            mImageMessage.setImageURI(imageUri);
        }
    }

    private void uploadImage() {

        UUID uuid = UUID.randomUUID();
        final StorageReference image_path = storageReference.child("message_images").child(currentUserID).child(profile_UserID).child(uuid + ".jpg");

        UploadTask uploadTask = image_path.putFile(imageUri);


        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                mProgressUpload.setVisibility(View.VISIBLE);

                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());

                mProgressUpload.setProgress(progress);

                if (progress >= 100)
                    mProgressUpload.setVisibility(View.GONE);


            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return image_path.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {

                    Uri downloadUri = task.getResult();

                    setMessage(downloadUri.toString(), "image");

                    mTxtVwMessage.setVisibility(View.VISIBLE);
                    mImageMessage.setVisibility(View.GONE);

                    imageUri = null;


                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    public void deleteImageAttachment(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete image attachment?")
                .setCancelable(false)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAttachment();
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void deleteAttachment() {
        imageUri = null;

        mTxtVwMessage.setVisibility(View.VISIBLE);
        mImageMessage.setVisibility(View.GONE);
    }
}