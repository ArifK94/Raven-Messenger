package com.example.arifk.ravenmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSetupActivity extends AppCompatActivity {

    private StorageReference storageReference;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    private static final int PICK_IMAGE_REQUEST = 1;

    private static final String KEY_COLLECTION_USERS = "Users";
    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR_IMAGE = "avatar";
    private static final String KEY_BACKGROUND_IMAGE = "background_image";

    private String displayName, username;

    private EditText mEditTxtDisplayName, mEditTxtUsername;
    private RelativeLayout firstLayout, secondLayout;
    private CardView cardView_BackLayout, cardView_NextStep, cardView_FinishSetup;
    private CircleImageView profilePic;
    private TextView mTxtUsernameExists;
    private ProgressDialog mRegProgress;

    private String user_ID;
    private Uri avatarImageURI = null;
    private boolean doesUsernameExist = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user_ID = firebaseAuth.getCurrentUser().getUid();
        userRef = db.collection("Users").document(user_ID);

        mRegProgress = new ProgressDialog(this);

        mEditTxtDisplayName = findViewById(R.id.txt_DisplayName);
        mEditTxtUsername = findViewById(R.id.txt_username);
        firstLayout = findViewById(R.id.layout_FirstStep);
        secondLayout = findViewById(R.id.layout_SecondStep);
        cardView_NextStep = findViewById(R.id.btn_NextStep);
        cardView_BackLayout = findViewById(R.id.btn_Back);
        cardView_FinishSetup = findViewById(R.id.btn_FinishSetup);
        profilePic = findViewById(R.id.profilePic);
        mTxtUsernameExists = findViewById(R.id.txt_username_exists);

        firstLayout.setVisibility(View.VISIBLE);
        secondLayout.setVisibility(View.GONE);

        mTxtUsernameExists.setVisibility(View.GONE);

        avatarImageURI = Uri.parse("android.resource://com.example.arifk.ravenmessenger/" + R.drawable.avatar_default);
        profilePic.setImageURI(avatarImageURI);


        Thread t = new Thread() {
            @Override
            public void run() {

                while (!isInterrupted()) {

                    try {
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                authenticateUsername(mEditTxtUsername.getText().toString());
                                handleNextNavigation();
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

    private void uploadDetails() {
        displayName = mEditTxtDisplayName.getText().toString();
        username = mEditTxtUsername.getText().toString();


        if (avatarImageURI != null) {

            final StorageReference image_path = storageReference.child("profile_images").child(user_ID + ".jpg");

            image_path.putFile(avatarImageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(AccountSetupActivity.this, "Upload Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }

                    // Continue with the task to get the download URL
                    return image_path.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        showProcessDialog();

                        Uri downloadUri = task.getResult();

                        storeDetails(downloadUri, displayName, username);


                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(AccountSetupActivity.this, "Upload Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            storeDetails(null, displayName, username);
        }
    }

    private void storeDetails(Uri task, String name, String username) {


        Map<String, String> userMap = new HashMap<>();
        userMap.put(KEY_NAME, name);
        userMap.put(KEY_USERNAME, username);
        userMap.put(KEY_BACKGROUND_IMAGE, "default");

        if (task != null)
            userMap.put(KEY_AVATAR_IMAGE, task.toString());
        else
            userMap.put(KEY_AVATAR_IMAGE, "default");


        userRef.set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Intent mainIntent = new Intent(AccountSetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {

                    mRegProgress.hide();

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(AccountSetupActivity.this, "Error " + errorMessage, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void authenticateUsername(final String username) {
        CollectionReference userCollection = db.collection(KEY_COLLECTION_USERS);
        Query usernameQuery = userCollection.whereEqualTo(KEY_USERNAME, username);

        usernameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    if (username.isEmpty()) {

                        mTxtUsernameExists.setVisibility(View.GONE);

                    } else if (task.getResult().size() != 0) {

                        mTxtUsernameExists.setTextColor(Color.RED);
                        mTxtUsernameExists.setText("");
                        mTxtUsernameExists.append("Username Unavailable");
                        mTxtUsernameExists.setVisibility(View.VISIBLE);
                        doesUsernameExist = true;

                    } else if (task.getResult().size() == 0) {

                        mTxtUsernameExists.setTextColor(Color.GREEN);
                        mTxtUsernameExists.setText("");
                        mTxtUsernameExists.append("Username Available");
                        mTxtUsernameExists.setVisibility(View.VISIBLE);
                        doesUsernameExist = false;
                    }

                }
            }

        });
    }

    private void handleNextNavigation() {
        if ((mEditTxtDisplayName.getText().toString().isEmpty() || mEditTxtUsername.getText().toString().isEmpty()) || doesUsernameExist)
            cardView_NextStep.setVisibility(View.GONE);
        else
            cardView_NextStep.setVisibility(View.VISIBLE);
    }


    public void selectImage(View view) {

        openFileChooser();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            avatarImageURI = data.getData();

            cropImage();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                avatarImageURI = result.getUri();
                profilePic.setImageURI(avatarImageURI);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();

            }
        }

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void cropImage() {

        CropImage.activity(avatarImageURI)
                .setAspectRatio(1, 1)
                .setMinCropWindowSize(500, 500)
                .start(this);

    }

    public void finishSetup(View view) {
        uploadDetails();
    }

    public void nextStep(View view) {

        moveSecondLayoutIn();

    }

    public void backLayout(View view) {

        moveFirstLayoutIn();
    }

    private void moveFirstLayoutIn() {
        // First layout move in
        animSlideIn(firstLayout, R.anim.slide_in_left);
        animSlideIn(cardView_NextStep, R.anim.slide_in_left);

        // Second layout move out
        animSlideOut(secondLayout, R.anim.slide_out_right);
        animSlideOut(cardView_FinishSetup, R.anim.slide_out_right);
        animSlideOut(cardView_BackLayout, R.anim.slide_out_right);
    }

    private void moveSecondLayoutIn() {
        // First layout move out
        animSlideOut(firstLayout, R.anim.slide_out_left);
        animSlideOut(cardView_NextStep, R.anim.slide_out_left);

        // Second layout move in
        animSlideIn(secondLayout, R.anim.slide_in_right);
        animSlideIn(cardView_FinishSetup, R.anim.slide_in_right);
        animSlideIn(cardView_BackLayout, R.anim.slide_in_right);
    }

    private void animSlideIn(ViewGroup viewGroup, int anim) {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), anim);
        viewGroup.startAnimation(animation);
        viewGroup.setVisibility(View.VISIBLE);
    }

    private void animSlideOut(ViewGroup viewGroup, int anim) {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), anim);
        viewGroup.startAnimation(animation);
        viewGroup.setVisibility(View.GONE);
    }

    private void showProcessDialog() {
        mRegProgress.setTitle("Processing Setup");
        mRegProgress.setMessage("Please wait until setup is completed!");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();
    }


}
