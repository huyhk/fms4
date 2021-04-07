package com.megatech.fms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_COMMAND;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_DEVICE_CONNECTION_STATE;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.model.LCRDataModel;
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


        updateCurrentAmount();
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


        if (!currentApp.isFirstUse())
        reader = LCRReader.create(this, currentApp.getDeviceIP(), 10001, true);
        reader.setConnectionListener(new LCRReader.LCRConnectionListener() {
            @Override
            public void onConnected() {

                //reader.requestSerial();
                //reader.doDisconnectDevice();
                //reader.destroy();
                //reader.destroy();
            }

            @Override
            public void onError() {

            }

            @Override
            public void onDeviceAdded(boolean failed) {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onCommandError(LCR_COMMAND command) {

            }

            @Override
            public void onConnectionStateChange(LCR_DEVICE_CONNECTION_STATE state) {

            }
        });
        reader.setFieldDataListener(new LCRReader.LCRDataListener() {
            @Override
            public void onDataChanged(LCRDataModel dataModel, LCRReader.FIELD_CHANGE field_change) {
                /*if (field_change == LCRReader.FIELD_CHANGE.SERIAL)
                {
                    if (currentApp.getSetting().getDeviceSerial() != dataModel.getSerialId())
                        runOnUiThread(()->{
                            showInvalidTruckError();
                        });
                }*/
            }

            @Override
            public void onErrorMessage(String errorMsg) {

            }
        });
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

    private void showInvalidTruckError() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.wrong_truck_connect)
                .setIcon(R.drawable.ic_warning)
                .create()
                .show();
    }

    private void updateCurrentAmount() {
        //currentApp.initCurrentAmount( new HttpClient().getTruckAmount(currentApp.getTruckNo()));

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
