package com.guomato.snapshot;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Scene;
import android.view.Menu;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MenuFragment.OnMenuItemClickListener {

    public static final String EXTRA_ALBUM_LIST = "com.guomato.snapshot.EXTRA_ALBUM_LIST";

    public static final String EXTRA_ALBUM = "com.guomato.snapshot.EXTRA_ALBUM";

    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private ArrayList<String> mAlbumItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatas();

        initViews();
    }

    @Override
    public void onItemClick(int position) {
        if (position != -1) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction tc = fm.beginTransaction();

            Fragment fragment = SceneFragment.newInstance(mAlbumItems.get(position));
            if (fragment != null) {
                tc.replace(R.id.content_container, fragment);
            }

            tc.commitAllowingStateLoss();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * 初始化数据
     */
    private void initDatas() {
        mAlbumItems = getIntent().getStringArrayListExtra(EXTRA_ALBUM_LIST);
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //初始化Toolbar以及DrawerLayout

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        setSupportActionBar(mToolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //初始化menu和content
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tc = fm.beginTransaction();

        Fragment menuFragment = fm.findFragmentById(R.id.menu_container);
        if (menuFragment == null) {
            menuFragment = MenuFragment.newInstance(mAlbumItems);
            tc.add(R.id.menu_container, menuFragment);
        }

        Fragment sceneFragment = fm.findFragmentById(R.id.content_container);
        if (sceneFragment == null) {
            if (mAlbumItems != null && mAlbumItems.size() > 0) {
                sceneFragment = SceneFragment.newInstance(mAlbumItems.get(0));
                tc.add(R.id.content_container, sceneFragment);
            }
        }

        tc.commitAllowingStateLoss();
    }

}
