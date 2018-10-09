package com.example.arifk.ravenmessenger;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionPagerAdapter extends FragmentPagerAdapter {

    private int currentPosition = 0;

    public int getCurrentPosition() {
        return currentPosition;
    }

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:

                ChatFragment chatFragment = new ChatFragment();
                currentPosition = 0;
                return chatFragment;

            case 1:

                FriendFragment friendFragment = new FriendFragment();
                currentPosition = 1;
                return friendFragment;

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

                return "Chats";

            case 1:
                return "Friends";

            default:
                return null;
        }
    }


}
