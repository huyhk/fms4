package com.megatech.fms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_COMMAND;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_DEVICE_CONNECTION_STATE;
import com.megatech.fms.databinding.DialogShiftBinding;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.helpers.Logger;
import com.megatech.fms.model.LCRDataModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.ShiftModel;
import com.megatech.fms.model.UserInfo;
import com.megatech.fms.view.PageAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

public class MainActivity extends UserBaseActivity implements RefuelListFragment.OnFragmentInteractionListener {
    private boolean mTwoPane;
    Button btnUpdate;
    Button toolbarRefuelButton;
    Button toolbarExtractButton;

    DialogShiftBinding shiftBinding;

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
    protected void onResume() {
        super.onResume();
        TabLayout tbl = findViewById(R.id.main_tablayout);
        tbl.getTabAt(0).setText(currentApp.getTruckNo());

        btnUpdate.setVisibility(View.VISIBLE);
        showShiftInfo();
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
        if (btnUpdate!=null)
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateRefuelList();
            }
        });

        Button btnRefuel = findViewById(R.id.btnNewRefuel);
        if (btnRefuel!=null) {
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

            checkInternet();


        }

        if (!currentApp.isFirstUse())
        initReader();
        setTabData();
        prepareSearchBox();

        checkIncompleteItem();
    }

    private void initReader() {
        reader = LCRReader.create(this, currentApp.getDeviceIP(), 10001, true);
        reader.setConnectionListener(new LCRReader.LCRConnectionListener() {
            @Override
            public void onConnected() {

                reader.requestDateFormat();
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
                reader.doDisconnectDevice();

            }

            @Override
            public void onErrorMessage(String errorMsg) {

            }

            @Override
            public void onFieldAddSucess(String field_name) {

            }
        });
    }

    private void prepareSearchBox() {


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

    private void checkIncompleteItem() {

        new AsyncTask<Void, Void, RefuelItemData>() {
            @Override
            protected RefuelItemData doInBackground(Void... voids) {
                Logger.appendLog("MAIN", "Check incomplete item");
                RefuelItemData postRefuel = DataHelper.getImcomplete();
                return postRefuel;
            }

            @Override
            protected void onPostExecute(RefuelItemData itemData) {
                if (itemData!=null)
                {
                    showConfirmMessage(R.string.incomplete_continue, new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            openIncompleItem(itemData);
                            return null;
                        }
                    });
                }

                super.onPostExecute(itemData);

            }
        }.execute();
    }

    private void openIncompleItem(RefuelItemData itemData) {

        Intent intent = new Intent(this, RefuelDetailActivity.class);
        intent.putExtra("REFUEL", itemData.toJson());
        startActivity(intent);
    }

    private void showShiftInfo() {
        TextView lblShift = findViewById(R.id.lblShiftInfo);
        if (lblShift != null) {
            ShiftModel model = currentApp.getShift();
            Date d = new Date();
            if (model == null || d.compareTo(model.getStartTime()) < 0 || d.compareTo(model.getEndTime()) > 0) {
                model = new HttpClient().getShift();
                if (model != null)
                    currentApp.saveShift(model);
            }
            if (model != null) {
                ShiftModel cModel = model.isSelected() ? model : model.getPrevShift().isSelected() ? model.getPrevShift() : model.getNextShift();
                if (cModel.getStartTime() ==null  || cModel.getName() == null) {
                    model.setSelected(true);
                    cModel = model;
                }

                lblShift.setText(String.format("%s: %s ", getString(model.isSelected() ? R.string.current_shift : model.getPrevShift().isSelected() ? R.string.prev_shift : R.string.next_shift), cModel.toString()));
            }
        }

        Button btnShift = findViewById(R.id.btn_shift);
        btnShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShiftDialog();
            }
        });
    }

    private void showShiftDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.shift_select);
        shiftBinding = DataBindingUtil.inflate(this.getLayoutInflater(), R.layout.dialog_shift, null, false);
        builder.setView(shiftBinding.getRoot());
        ShiftModel model = currentApp.getShift();

        shiftBinding.setShift(model);

        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ShiftModel model = shiftBinding.getShift();


                currentApp.saveShift(model);
                updateRefuelList();
                showShiftInfo();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dlg = builder.create();
        dlg.show();
        dlg.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dlg.findViewById(R.id.radNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioClick(v);
            }
        });
        dlg.findViewById(R.id.radCurrent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioClick(v);
            }
        });
        dlg.findViewById(R.id.radPrev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioClick(v);
            }
        });


    }

    private void updateRefuelList() {
        ViewPager viewPager = findViewById(R.id.main_viewpager);
        ((PageAdapter) viewPager.getAdapter()).updateLists();
    }

    private void radioClick(View v) {
        View dlg = v.getRootView();
        if (dlg != null) {
            switch (v.getId()) {
                case R.id.radCurrent:
                    ((RadioButton) dlg.findViewById(R.id.radPrev)).setChecked(false);
                    ((RadioButton) dlg.findViewById(R.id.radNext)).setChecked(false);
                    break;
                case R.id.radNext:
                    ((RadioButton) dlg.findViewById(R.id.radPrev)).setChecked(false);
                    ((RadioButton) dlg.findViewById(R.id.radCurrent)).setChecked(false);
                    break;
                case R.id.radPrev:
                    ((RadioButton) dlg.findViewById(R.id.radCurrent)).setChecked(false);
                    ((RadioButton) dlg.findViewById(R.id.radNext)).setChecked(false);
                    break;
            }

            shiftBinding.invalidateAll();

        }

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
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void checkInternet()
    {
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(()->{
                    showInternetIcon(!hostAvailable("skypec.com.vn",80));
                });


            }
        },10,5*1000);

    }

    public boolean hostAvailable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2000);
            return true;
        } catch (IOException e) {
            // Either we have a timeout or unreachable host or failed DNS lookup
            System.out.println(e);
            return false;
        }
    }

    private void showInternetIcon(boolean show)
    {
        if (optionMenu == null) return;
        MenuItem v = optionMenu.findItem(R.id.action_status);
        if (v!=null) {
            Drawable icon = getDrawable(R.drawable.ic_no_connection);

            //v.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

            if (!show) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                SpannableString s = new SpannableString("WIFI:" + ssid.replace("\"",""));
                s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);

                v.setTitle(s);
                v.setIcon(null);

            }
            else {
                v.setIcon(icon);
            }
        }

    }
}
