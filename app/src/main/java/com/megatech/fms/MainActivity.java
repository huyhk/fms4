package com.megatech.fms;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TableLayout;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.view.PageAdapter;

public class MainActivity extends UserBaseActivity implements RefuelListFragment.OnFragmentInteractionListener {
    private boolean mTwoPane;


    @Override
    protected void onResume() {
        super.onResume();
        TabLayout tbl = (TabLayout) findViewById(R.id.main_tablayout);
        tbl.getTabAt(0).setText(currentApp.getTruckNo());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SETTING_CODE)
            {
                TabLayout tbl = (TabLayout) findViewById(R.id.main_tablayout);
                tbl.getTabAt(0).setText(currentApp.getTruckNo());
            }
            {
                ViewPager viewPager = findViewById(R.id.main_viewpager);
                ((PageAdapter) viewPager.getAdapter()).updateLists();
            }

        }
    }

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();
        if (currentApp.isFirstUse())
        {
            setting();
        }
        reader = LCRReader.create(this, currentApp.getDeviceIP(), 10001, true);
        ViewPager viewPager = findViewById(R.id.main_viewpager);
        viewPager.setAdapter( new PageAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.main_tablayout);

        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);


    }

    LCRReader reader;
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reader!=null)
            reader.destroy();
    }
}
