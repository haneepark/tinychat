package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Context context=this;
//    MyPreferences pref=null; //테스트목적외에 필요없음
//    MySQLite mySQLite = null;//테스트목적외에 필요없음
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.tab_friends,
            R.drawable.tab_talk
    };
    private FloatingActionButton fab1,fab2;

    static final String FRIEND_TAB = "friend_tab", ROOM_TAB = "room_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        if (pref==null){ pref = MyPreferences.getInstance(context); }
//        if (mySQLite==null){ mySQLite = MySQLite.getInstance(context); }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // do not show default name text and instead, show the textView i included
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // do not show back button

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        fab1 = (FloatingActionButton) findViewById(R.id.fab_friend);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_room);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab1.setVisibility(View.VISIBLE);
        fab2.setVisibility(View.INVISIBLE);

        if (MyUtil.IsNetworkConnected(MainActivity.this)){
            // start TCP service
            Intent intent = new Intent(MainActivity.this, MyTCPService.class);
            startService(intent);
        }
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FriendTab(), FRIEND_TAB);
        adapter.addFragment(new RoomTab(), ROOM_TAB);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position==0){
//                    Toast.makeText(context, "page 1", Toast.LENGTH_SHORT).show();
                    fab1.setVisibility(View.VISIBLE);
                    fab2.setVisibility(View.INVISIBLE);
                } else if (position==1){
//                    Toast.makeText(context, "page 2", Toast.LENGTH_SHORT).show();
                    fab1.setVisibility(View.INVISIBLE);
                    fab2.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // tab 이름 보이고 숨기기
//            return mFragmentTitleList.get(position);
            return null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_friend : //fab1
                Intent i = new Intent(context,AddFriendActivity.class);
                startActivity(i);
                break;
            case R.id.fab_room : //fab2
                Intent ii = new Intent(context,AddRoomActivity.class);
                startActivity(ii);
                break;
        }
    }
}
