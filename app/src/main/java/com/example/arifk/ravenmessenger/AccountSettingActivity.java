package com.example.arifk.ravenmessenger;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

public class AccountSettingActivity extends AppCompatActivity {

    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;
    private static final int PICK_BACKGROUND_IMAGE_REQUEST = 2;
    private Uri mProfileImageUri, mBackgroundImageUri;
    private boolean profilePicSelected = false;
    private boolean backgroundPicSelected = false;

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DocumentReference userRef, messageRef;

    private String user_ID;

    private static final String KEY_COLLECTION_FRIENDS = "Friends";
    private static final String KEY_COLLECTION_FRIEND_REQUESTS_RECIEVED = "Friend Requests Received";
    private static final String KEY_COLLECTION_MESSAGES = "Messages";

    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR_IMAGE = "avatar";
    private static final String KEY_BACKGROUND_IMAGE = "background_image";

    private LinearLayout linearLayout;
    private TextView displayName, mTxtView_Username, emailTxt, mTxtView_messages, mTxtView_friends, mTxtView_requests;
    private ImageView avatar, backgroundImage;
    private Button btnUpdateChanges;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        Toolbar mToolbar = findViewById(R.id.account_settings_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user_ID = firebaseAuth.getCurrentUser().getUid();
        userRef = db.collection("Users").document(user_ID);
        messageRef = db.collection(KEY_COLLECTION_MESSAGES).document(user_ID);

        linearLayout = findViewById(R.id.linear_layout_root);
        displayName = findViewById(R.id.txt_displayname);
        mTxtView_Username = findViewById(R.id.txt_username);
        avatar = findViewById(R.id.img_Avatar);
        backgroundImage = findViewById(R.id.backgroundImage);
        emailTxt = findViewById(R.id.txt_email);
        btnUpdateChanges = findViewById(R.id.btn_update_changes);
        mTxtView_messages = findViewById(R.id.txt_messages);
        mTxtView_friends = findViewById(R.id.txt_friends);
        mTxtView_requests = findViewById(R.id.txt_requests);

        setUpdateBtnState(false);

        if (!user_ID.isEmpty() && messageRef != null && userRef != null)
            loadData();

    }

    private void loadData() {
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            String name = documentSnapshot.getString(KEY_NAME);
                            String username = documentSnapshot.getString(KEY_USERNAME);
                            String image = documentSnapshot.getString(KEY_AVATAR_IMAGE);
                            String bgImage = documentSnapshot.getString(KEY_BACKGROUND_IMAGE);


                            displayName.setText(name);
                            mTxtView_Username.setText(username);


                            if (!image.equals("default")) {
                                mProfileImageUri = Uri.parse(image);
                                Picasso.get().load(image).into(avatar);
                            }

                            if (!bgImage.equals("default")) {
                                mBackgroundImageUri = Uri.parse(bgImage);
                                Picasso.get().load(bgImage).into(backgroundImage);
                            }

                            emailTxt.setText(firebaseAuth.getCurrentUser().getEmail());

                        } else {
                            Toast.makeText(AccountSettingActivity.this, "Document does not exist", Toast.LENGTH_SHORT).show();

                            Picasso.get().load(R.drawable.avatar_default).into(avatar);
                            Picasso.get().load(R.drawable.avatar_default).into(backgroundImage);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AccountSettingActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });

        getMessagesData();
        getRequestsData();
        getFriendsData();
    }

    private void getMessagesData() {
        messageRef.collection("Conversations").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty())
                    mTxtView_messages.setText(String.valueOf(queryDocumentSnapshots.size()));
                else
                    mTxtView_messages.setText("0");

            }
        });
    }

    private void getRequestsData() {

        userRef.collection(KEY_COLLECTION_FRIEND_REQUESTS_RECIEVED).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {
                    mTxtView_requests.setText(String.valueOf(documentSnapshots.size()));
                } else {
                    mTxtView_requests.setText("0");

                }

            }
        });
    }

    private void getFriendsData() {

        userRef.collection(KEY_COLLECTION_FRIENDS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {
                    mTxtView_friends.setText(String.valueOf(documentSnapshots.size()));
                } else {
                    mTxtView_friends.setText("0");

                }

            }
        });
    }

    public void selectProfileImage(View view) {

        showImageOption("Profile Image", mProfileImageUri, PICK_PROFILE_IMAGE_REQUEST);
    }

    public void selectBackgroundImage(View view) {

        showImageOption("Background Image", mBackgroundImageUri, PICK_BACKGROUND_IMAGE_REQUEST);
    }


    public void updateChange(View view) {

        if (mProfileImageUri != null && mBackgroundImageUri != null) {
            uploadImage();
            uploadBackgroundImage();

            setSnackMessage("Profile Images Updated");

            setUpdateBtnState(false);
        } else if (mProfileImageUri != null) {

            uploadImage();

            setSnackMessage("Profile Picture Updated");

            setUpdateBtnState(false);
        } else if (mBackgroundImageUri != null) {

            uploadBackgroundImage();

            setSnackMessage("Background Image Updated");

            setUpdateBtnState(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PROFILE_IMAGE_REQUEST) {

                cropImage(data.getData(), 1, 1);

                profilePicSelected = true;
                backgroundPicSelected = false;

            } else if (requestCode == PICK_BACKGROUND_IMAGE_REQUEST) {

                cropImage(data.getData(), backgroundImage.getWidth(), backgroundImage.getHeight());

                profilePicSelected = false;
                backgroundPicSelected = true;
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri mCroppedImageUri = result.getUri();

                croppedImageUri(mCroppedImageUri);

                setUpdateBtnState(true);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void croppedImageUri(Uri mCroppedImageUri) {
        if (profilePicSelected) {
            Picasso.get().load(mCroppedImageUri).into(avatar);
            mProfileImageUri = mCroppedImageUri;
        }

        if (backgroundPicSelected) {
            Picasso.get().load(mCroppedImageUri).into(backgroundImage);
            mBackgroundImageUri = mCroppedImageUri;
        }
    }

    private void openFileChooser(final int PICK_IMAGE_REQUEST) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void cropImage(Uri imageUri, int aspectRatioX, int aspectRatioY) {

        CropImage.activity(imageUri)
                .setAspectRatio(aspectRatioX, aspectRatioY)
                .setMinCropWindowSize(500, 500)
                .start(this);

    }

    private void uploadImage() {

        final StorageReference image_path = storageReference.child("profile_images").child(user_ID + ".jpg");


        image_path.putFile(mProfileImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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

                    userRef.update(KEY_AVATAR_IMAGE, downloadUri.toString());

                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    private void uploadBackgroundImage() {

        final StorageReference image_path = storageReference.child("background_images").child(user_ID + ".jpg");

        image_path.putFile(mBackgroundImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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

                    userRef.update(KEY_BACKGROUND_IMAGE, downloadUri.toString());

                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }


    public void editDisplayName(View view) {
        final EditText mEditTxtDisplayName = new EditText(this);
        mEditTxtDisplayName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add a new display name")
                .setMessage("Users will see your display name")
                .setView(mEditTxtDisplayName)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String mTxt_Input = String.valueOf(mEditTxtDisplayName.getText());

                        if (!mTxt_Input.trim().isEmpty())
                            userRef.update(KEY_NAME, mTxt_Input);
                        else
                            Toast.makeText(AccountSettingActivity.this, "Input Invalid", Toast.LENGTH_LONG).show();


                        loadData();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void changeEmail(View view) {
        View mView = getLayoutInflater().inflate(R.layout.dialog_change_email, null);
        final EditText editText_NewEmail = mView.findViewById(R.id.etxt_new_email);
        final EditText editText_ConfirmPassword = mView.findViewById(R.id.etxt_confirm_password);

        Button mUpdateEmail = mView.findViewById(R.id.btn_update_email);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        mUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText_NewEmail.getText().toString().isEmpty() && !editText_ConfirmPassword.getText().toString().isEmpty()) {

                    updateEmail(editText_NewEmail.getText().toString(), editText_ConfirmPassword.getText().toString(), dialog);
                    loadData();

                } else {

                    Toast.makeText(AccountSettingActivity.this, "Error: Missing Fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void changePassword(View view) {

        View mView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        final EditText editText_CurrentPassword = mView.findViewById(R.id.etxt_current_password);
        final EditText editText_NewPassword = mView.findViewById(R.id.etxt_new_password);
        final EditText editText_ConfirmNewPassword = mView.findViewById(R.id.etxt_confirm_new_password);

        Button mUpdatePassword = mView.findViewById(R.id.btn_update_password);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        mUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText_CurrentPassword.getText().toString().isEmpty() && !editText_NewPassword.getText().toString().isEmpty() && !editText_ConfirmNewPassword.getText().toString().isEmpty()) {

                    if (editText_NewPassword.getText().toString().equals(editText_ConfirmNewPassword.getText().toString())) {

                        updatePassword(editText_CurrentPassword.getText().toString(), editText_NewPassword.getText().toString(), dialog);
                        loadData();

                    } else {
                        Toast.makeText(AccountSettingActivity.this, "Error: Passwords do not match", Toast.LENGTH_LONG).show();
                    }

                } else {

                    Toast.makeText(AccountSettingActivity.this, "Error: Missing Fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateEmail(final String newEmail, String password, final AlertDialog alertDialog) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();

        // Get auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider.getCredential(email, password); // Current Login Credentials \\

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                loadData();

                                alertDialog.dismiss();

                                setSnackMessage("Email Updated");

                            } else {
                                Toast.makeText(AccountSettingActivity.this, "Error: Email Not Updated", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(AccountSettingActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    private void updatePassword(String mCurrentPass, final String newPassword, final AlertDialog alertDialog) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();

        AuthCredential credential = EmailAuthProvider.getCredential(email, mCurrentPass);

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                loadData();

                                alertDialog.dismiss();

                                setSnackMessage("Password Updated");


                            } else {
                                Toast.makeText(AccountSettingActivity.this, "Error Updating Password", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {

                    Toast.makeText(AccountSettingActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void setUpdateBtnState(boolean state) {
        if (state) {
            btnUpdateChanges.setEnabled(true);
            btnUpdateChanges.setAlpha(1.0f);
        } else {
            btnUpdateChanges.setEnabled(false);
            btnUpdateChanges.setAlpha(.5f);
        }
    }

    private void showImageOption(String name, final Uri imageUri, final int PICK_IMAGE_REQUEST) {
        final String[] options = {"View", "Upload"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(name);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    Intent fullScreenIntent = new Intent(AccountSettingActivity.this, FullScreenImageActivity.class);
                    fullScreenIntent.setData(imageUri);
                    fullScreenIntent.putExtra("title", displayName.getText().toString());
                    startActivity(fullScreenIntent);
                } else if (which == 1) {
                    openFileChooser(PICK_IMAGE_REQUEST);
                }

            }
        });
        builder.show();
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

    public void messagesPage(View view) {

        Intent intent = new Intent(AccountSettingActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void requestsPage(View view) {

        Intent intent = new Intent(AccountSettingActivity.this, FriendRequestSentActivity.class);
        startActivity(intent);
    }

    public void friendListPage(View view) {

        Intent intent = new Intent(AccountSettingActivity.this, FriendListActivity.class);
        startActivity(intent);
    }
}
