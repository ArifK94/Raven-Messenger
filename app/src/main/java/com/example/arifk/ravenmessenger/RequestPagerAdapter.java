package com.example.arifk.ravenmessenger;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class RequestPagerAdapter extends FragmentPagerAdapter {

    private int currentPosition = 0;

    public int getCurrentPosition() {
        return currentPosition;
    }


    public RequestPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:

                FriendRequestReceivedFragment friendRequestReceivedFragment = new FriendRequestReceivedFragment();
                currentPosition = 0;
                return friendRequestReceivedFragment;

            case 1:

                FriendRequestSentFragment friendSentFragment = new FriendRequestSentFragment();
                currentPosition = 1;
                return friendSentFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:

                return "Received";

            case 1:
                return "Sent";

            default:
                return null;
        }
    }
}
