package com.example.arifk.ravenmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getToolbarSettings();

        checkUserSession();

        manageTab();

    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(mAuthListner);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            final String currentUserID = firebaseAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Intent setupIntent = new Intent(MainActivity.this, AccountSetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        } else {
                            // Get updated InstanceID token.
                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {

                                    String deviceToken = instanceIdResult.getToken();

                                    firebaseFirestore.collection("Users").document(currentUserID).update("tokenId", deviceToken);
                                }
                            });
                        }
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void checkUserSession() {
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    private void getToolbarSettings() {
        Toolbar toolbar;
        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Raven Messenger");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.btn_menu_Logout) {
            firebaseAuth.signOut();
        } else if (item.getItemId() == R.id.btn_menu_Account) {
            Intent intent = new Intent(MainActivity.this, AccountSettingActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.btn_menu_all_users) {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private void manageTab() {
        ViewPager viewPager = findViewById(R.id.pager_Tab);
        SectionPagerAdapter sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(sectionPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_Header);
        tabLayout.setupWithViewPager(viewPager);
    }
}
