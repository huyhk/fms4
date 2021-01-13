package com.megatech.fms;

import android.app.Activity;
import android.app.SearchableInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TableLayout;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.model.PermissionModel;
import com.megatech.fms.model.UserInfo;
import com.megatech.fms.view.PageAdapter;

public class MainActivity extends UserBaseActivity implements RefuelListFragment.OnFragmentInteractionListener {
    private boolean mTwoPane;
    Button btnUpdate;
    Button toolbarRefuelButton;
    Button toolbarExtractButton;

    @Override
    protected void onResume() {
        super.onResume();
        TabLayout tbl = findViewById(R.id.main_tablayout);
        tbl.getTabAt(0).setText(currentApp.getTruckNo());

        btnUpdate.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SETTING_CODE)
            {
                TabLayout tbl = findViewById(R.id.main_tablayout);
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
        toolbarRefuelButton = findViewById(R.id.btnRefuel);
        toolbarExtractButton = findViewById(R.id.btnExtract);
        //toolbarRefuelButton.setVisibility(View.GONE);

        btnUpdate = findViewById(R.id.btnUpdate2);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewPager viewPager = findViewById(R.id.main_viewpager);
                ((PageAdapter) viewPager.getAdapter()).updateLists();
            }
        });

        Button btnRefuel = findViewById(R.id.btnNewRefuel);

        btnRefuel.setVisibility(View.INVISIBLE);
        ((Runnable) () -> {
            //HttpClient client = new HttpClient();
            //PermissionModel model = client.getPermission();
            //if (model == null || model.isAllowNewRefuel())
            //    btnRefuel.setVisibility(View.VISIBLE);

            if ((currentUser.getPermission() & UserInfo.USER_PERMISSION.CREATE_REFUEL.getValue()) > 0)
                btnRefuel.setVisibility(View.VISIBLE);

        }).run();
        btnRefuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_refuel();
            }
        });

        if (currentApp.isFirstUse()) {
            setting();
        }

        reader = LCRReader.create(this, currentApp.getDeviceIP(), 10001, true);

        setTabData();

        ((SearchView) findViewById(R.id.search_bar)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ViewPager viewPager = findViewById(R.id.main_viewpager);
                ((PageAdapter) viewPager.getAdapter()).filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                ViewPager viewPager = findViewById(R.id.main_viewpager);
                ((PageAdapter) viewPager.getAdapter()).filter(query);
                return false;
            }
        });

        ((SearchView) findViewById(R.id.search_bar)).setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                ViewPager viewPager = findViewById(R.id.main_viewpager);
                ((PageAdapter) viewPager.getAdapter()).filter("");
                return false;
            }
        });
    }


    private void new_refuel() {
        try {
            Intent intent = new Intent(this, NewRefuelActivity.class);
            intent.putExtra("EXTRACT", false);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e("INVENTORY", ex.getMessage());
        }
    }

    private void setTabData() {
        ViewPager viewPager = findViewById(R.id.main_viewpager);
        viewPager.setAdapter( new PageAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.main_tablayout);
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
