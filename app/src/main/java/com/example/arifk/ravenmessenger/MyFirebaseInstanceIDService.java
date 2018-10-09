package com.example.arifk.ravenmessenger;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {


    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        // Get updated InstanceID token.
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {

                String deviceToken = instanceIdResult.getToken();

                Log.d(TAG, "Refreshed token: " + deviceToken);
            }
        });


    }
}
