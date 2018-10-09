package com.example.arifk.ravenmessenger;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class FriendRequestSentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request_sent);

        getToolbarSettings();
        manageTab();

    }

    private void getToolbarSettings()
    {
        Toolbar toolbar;
        toolbar = findViewById(R.id.request_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Requests");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void manageTab() {
        ViewPager viewPager = findViewById(R.id.pager_Tab);
        RequestPagerAdapter requestPagerAdapter = new RequestPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(requestPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_Header);
        tabLayout.setupWithViewPager(viewPager);
    }
}
