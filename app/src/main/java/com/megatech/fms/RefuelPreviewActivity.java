package com.megatech.fms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.databinding.ActivityRefuelPreviewBinding;
import com.megatech.fms.databinding.InvoicePreviewBinding;
import com.megatech.fms.databinding.PreviewExtractBinding;
import com.megatech.fms.databinding.SelectUserBinding;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.PrintWorker;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.InvoiceModel;
import com.megatech.fms.model.REFUEL_ITEM_STATUS;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.UserModel;
import com.megatech.fms.view.AirlineArrayAdapter;
import com.megatech.fms.view.InvoiceItemAdapter;
import com.megatech.fms.view.TruckArrayAdapter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.megatech.fms.helpers.PrintWorker.PRINT_MODE;
import static com.megatech.fms.helpers.PrintWorker.PrintStateListener;

public class RefuelPreviewActivity extends UserBaseActivity implements View.OnClickListener {

    private PrintWorker printWorker;
    private final int REFUEL_WINDOW = 1;
    List<AirlineModel> airlines = null;

    /// bind data to view
    ArrayList<RefuelItemData> allItems = new ArrayList<RefuelItemData>();
    ArrayList<RefuelItemData> printItems = new ArrayList<RefuelItemData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadData();

        Drawable drawable = getResources().getDrawable(R.drawable.ic_edit);
        drawable.setAlpha(90);

        printWorker = new PrintWorker(this);


        printWorker.setPrintStateListener(new PrintStateListener() {
            @Override
            public void onError() {
                if (refuelData != null)
                    refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
            }

            @Override
            public void onSuccess() {
                if (refuelData != null)
                    refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.SUCCESS);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_Title = getString(R.string.invoice_number);
                        showEditDialog(R.id.refuel_preview_invoice_number, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);


                    }
                });


                if (printDialog !=null)
                    printDialog.dismiss();

            }

        });

    }

    private void loadData() {
        Bundle b = getIntent().getExtras();
        Integer id = b.getInt("REFUEL_ID", 0);
        Integer localId = b.getInt("REFUEL_LOCAL_ID", 0);
        String mData = b.getString("REFUEL", "");
        new AsyncTask<Void, Void, RefuelItemData>() {
            @Override
            protected RefuelItemData doInBackground(Void... voids) {

                airlines = DataHelper.getAirlines();

                if (userList == null)
                    userList = DataHelper.getUsers();

                if (mData != null && mData != "") {
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

                    refuelData = gson.fromJson(mData, RefuelItemData.class);
                }
                if (refuelData == null)
                    refuelData = DataHelper.getRefuelItem(id, localId);


                return refuelData;
            }

            @Override
            protected void onPostExecute(RefuelItemData itemData) {
                refuelData = itemData;
                bindData();
                super.onPostExecute(itemData);

            }
        }.execute();

    }

    boolean isEditable = true;
    ActivityRefuelPreviewBinding binding;
    PreviewExtractBinding extractBinding;
    TruckArrayAdapter truckArrayAdapter;
    RefuelItemData refuelData;
    int result = Activity.RESULT_OK;


    InvoiceModel invoiceModel;
    Dialog printDialog;

    //validate data before print preview
    private boolean validate() {
        if (printMode == PRINT_MODE.ONE_ITEM) {
            return refuelData.getManualTemperature() > 0 && refuelData.getDensity() > 0;
        } else {

            for (int i = 0; i < allItems.size(); i++) {
                RefuelItemData item = allItems.get(i);
                if (!(item.getManualTemperature() > 0 && item.getDensity() > 0)) {
                    truckArrayAdapter.setSelected(i);
                    ListView lv = findViewById(R.id.refuel_preview_truck_list);
                    lv.setSelection(i);
                    refuelData = item;
                    binding.setMItem(refuelData);
                    isEditable = refuelData.getTruckNo().equals(currentApp.getTruckNo());
                    for (int j = 0; j < lv.getChildCount(); j++)
                        lv.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                    // change the background color of the selected element
                    lv.getAdapter().getView(i, null, lv).setBackgroundColor(Color.LTGRAY);

                    return false;
                }

            }
        }
        return true;
    }

    private void bindData() {
        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL)
            setContentView(R.layout.activity_refuel_preview);
        else
            setContentView(R.layout.preview_extract);

        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL) {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_refuel_preview);
            binding.setMItem(refuelData);
        } else {
            extractBinding = DataBindingUtil.setContentView(this, R.layout.preview_extract);
            extractBinding.setMItem(refuelData);

        }

        ArrayAdapter<AirlineModel> spinnerAdapter = new ArrayAdapter<AirlineModel>(this, R.layout.support_simple_spinner_dropdown_item, airlines);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        Spinner airline_spinner = findViewById(R.id.refuel_preview_airline_spinner);
        airline_spinner.setAdapter(spinnerAdapter);

        if (refuelData.getAirlineId() > 0) {
            for (int i = 0; i < airline_spinner.getCount(); i++) {
                AirlineModel item = (AirlineModel) airline_spinner.getItemAtPosition(i);
                if (refuelData.getAirlineId() == item.getId()) {
                    airline_spinner.setSelection(i);
                    ((TextView) findViewById(R.id.refuel_preview_airline)).setText(item.getName());
                    refuelData.setAirlineId(item.getId());
                    refuelData.setProductName(item.getProductName());
                    refuelData.setAirlineModel(item);
                    refuelData.setAirlineId(item.getId());
                    refuelData.setPrice(item.getPrice());
                    refuelData.setCurrency(item.getCurrency());
                    refuelData.setUnit(item.getUnit());
                    refuelData.setTaxRate(!refuelData.isInternational() && item.isInternational() ? 0.1 : 0);

                    break;
                }
            }

            airline_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    AirlineModel selected = (AirlineModel) parent.getItemAtPosition(position);
                    refuelData.setAirlineId(selected.getId());
                    refuelData.setPrice(selected.getPrice());
                    refuelData.setCurrency(selected.getCurrency());
                    refuelData.setUnit(selected.getUnit());
                    refuelData.setTaxRate(!refuelData.isInternational() && selected.isInternational() ? 0.1 : 0);
                    refuelData.setProductName(selected.getProductName());
                    refuelData.setAirlineModel(selected);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (refuelData.getOthers().size()>0)
            {
                for (int i=0; i<refuelData.getOthers().size(); i++)
                    refuelData.getOthers().get(i).setAirlineModel(refuelData.getAirlineModel());
            }
        }


        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL) {

            allItems.add(refuelData);
            allItems.addAll(refuelData.getOthers());
            Collections.sort(allItems, new Comparator<RefuelItemData>() {
                @Override
                public int compare(RefuelItemData o1, RefuelItemData o2) {
                    return o1.getEndTime().compareTo(o2.getEndTime());
                }
            });
            int selectedPos = 0;
            for (int i =0; i<allItems.size(); i++)
                if (allItems.get(i).getId() == refuelData.getId())
                    selectedPos = i;
            truckArrayAdapter = new TruckArrayAdapter(this, allItems);
            truckArrayAdapter.setSelected(selectedPos);
            ListView lv = findViewById(R.id.refuel_preview_truck_list);
            lv.setAdapter(truckArrayAdapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    refuelData =  (RefuelItemData)parent.getItemAtPosition(position);
                    binding.setMItem(refuelData);
                    isEditable = refuelData.getTruckNo().equals( currentApp.getTruckNo());


                    view.setSelected(true);
                    for (int j = 0; j < parent.getChildCount(); j++)
                        parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);

                    // change the background color of the selected element
                    view.setBackgroundColor(Color.LTGRAY);

                }

            });


        }

    }

    private void preview() {


        if (refuelData.getAirlineId() <= 0 || refuelData.getAirlineModel() == null) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.validate))
                    .setMessage(getString(R.string.invalid_airline_model))
                    .setIcon(R.drawable.ic_error)
                    .show();

            return;
        }

        if (!validate()) {

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.validate))
                    .setMessage(getString(R.string.invalid_density_temperature))
                    .setIcon(R.drawable.ic_error)
                    .show();

            return;
        }
        invoiceModel = InvoiceModel.fromRefuel(refuelData, printMode == PRINT_MODE.ONE_ITEM? null: allItems);
        printDialog = new Dialog(this);
        InvoicePreviewBinding binding = DataBindingUtil.inflate(printDialog.getLayoutInflater(), R.layout.invoice_preview, null, false);
        binding.setInvoiceItem(invoiceModel);
        printDialog.setContentView(binding.getRoot());
        RadioGroup radioGroup = printDialog.findViewById(R.id.radioTemplate);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int id = checkedId;

            invoiceModel.setPrintTemplate(id == R.id.radInvoice ? InvoiceModel.PRINT_TEMPLATE.INVOICE : InvoiceModel.PRINT_TEMPLATE.BILL);
            binding.invalidateAll();

        });
        int selectedIndex = !refuelData.isInternational() && !refuelData.getAirlineModel().isInternational() ? 1 : 0;

        if (refuelData.getReturnAmount() > 0 || invoiceModel.getItems().size() > 5) {
            radioGroup.getChildAt(0).setEnabled(false);
            selectedIndex = 1;
        }

        ((RadioButton) radioGroup.getChildAt(selectedIndex)).setChecked(true);
        //if (refuelData.getReturnAmount()>0)
        //    printDialog.findViewById(R.id.btnPrintInvoice).setVisibility(View.INVISIBLE);

        //if (invoiceModel.getItems().size()>3) {
        //printDialog.findViewById(R.id.btnPrintInvoice).setVisibility(View.INVISIBLE);

        //}

        InvoiceItemAdapter itemAdapter = new InvoiceItemAdapter(this, invoiceModel.getItems());
        ((ListView) printDialog.findViewById(R.id.invoice_preview_item_list)).setAdapter(itemAdapter);
        printDialog.findViewById(R.id.btnPrintInvoice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean invoice = ((RadioButton) radioGroup.getChildAt(0)).isChecked();
                boolean old = ((RadioButton) printDialog.findViewById(R.id.radOld)).isChecked();
                if (invoice)
                    printInvoice(old);
                else printBill(old);
            }
        });


        printDialog.findViewById(R.id.btnPrintCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printDialog.dismiss();
            }
        });


        printDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        printDialog.show();
    }

    private void printBill(boolean old) {
        /*if (!new PrintWorker(this).printItem(refuelData, printMode, PRINT_TEMPLATE.BILL)) {
            refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
        }
        */
        if (!printWorker.printBill(invoiceModel, old)) {
            refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
        }

//        setResult(RESULT_OK);
//        finish();

    }

    private void printInvoice(boolean old) {
        //boolean isOK = new PrintWorker(this).printItem(refuelData, printMode, PRINT_TEMPLATE.INVOICE);

        boolean isOK = printWorker.printInvoice(invoiceModel, old);

        if (!isOK) {
            refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
        }
        else
        {
            //showEditDialog(R.id.refuel_preview_invoice_number, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        }

//        setResult(RESULT_OK);
//        finish();

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.refuel_preview_print_refuel:
                if (refuelData.getStatus() != REFUEL_ITEM_STATUS.DONE)
                {
                    doRefuel();
                }
                break;

            case R.id.refuel_preview_print_current:
                printMode = PRINT_MODE.ONE_ITEM;
                preview();
                break;
            case R.id.refuel_preview_print_all:
                printMode = PRINT_MODE.ALL_ITEM;
                preview();
                break;
            case R.id.refuel_preview_new_item:
                createNewItem();
                break;
            case R.id.btnUpdate:
                save();
                break;
            case R.id.refuel_preview_aircraftCode:
                m_Title = getString(R.string.update_aircraftCode);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ".+");
                break;
            case R.id.refuel_preview_aircraftType:
                m_Title = getString(R.string.update_aircraftType);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,".+");
                break;
            case R.id.refuel_preview_realAmount:
                m_Title = getString(R.string.update_real_amount);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, "^\\d*(\\.\\d+)?$");
                break;
            case R.id.refuel_preview_Density:
                m_Title = getString(R.string.update_density);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,"^\\d*(\\.\\d+)?$");
                break;
            case R.id.refuel_preview_Temperature:
                m_Title = getString(R.string.update_temparature);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.refuel_preview_qc_no:
                m_Title = getString(R.string.update_qc_no);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,".+");
                break;
            case R.id.refuel_preview_parking:
                m_Title = getString(R.string.update_parking_lot);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,".+");
                break;
            case R.id.refuel_preview_vat:
                openVatSpinner();
                break;
            case R.id.refuel_preview_airline:
                openAirlineDialog();
                break;
            case R.id.refuel_preview_return:
                m_Title = getString(R.string.update_return_amount);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,"^\\d+(\\.\\d+)?$");

                break;
            case R.id.refuel_preview_weight_note:
                m_Title = getString(R.string.update_weight_note);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                break;
            case R.id.refuel_preview_driver:
            case R.id.refuel_preview_operator:
                showSelectUser();
                break;
            case R.id.btnBack:
                finish();
                break;
        }

    }

    private void doRefuel() {
        if (refuelData != null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = gson.toJson(refuelData);

            Intent intent = new Intent(this, RefuelDetailActivity.class);
            intent.putExtra("REFUEL", data);
            startActivityForResult(intent, REFUEL_WINDOW);
            finish();
        }
    }
    private List<UserModel> userList = null;

    private void showSelectUser() {

        if (!isEditable)
        {
            Toast.makeText(this,R.string.edit_not_allow, Toast.LENGTH_LONG ).show();
            return;
        }


        if (userList!=null) {
            Dialog dialog = new Dialog(this);
            SelectUserBinding binding = DataBindingUtil.inflate(dialog.getLayoutInflater(), R.layout.select_user, null, false);
            binding.setRefuelItem(refuelData);
            dialog.setContentView(binding.getRoot());
            Spinner spn = (Spinner) dialog.findViewById(R.id.select_user_driver);

            ArrayAdapter<UserModel> spinnerAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, userList);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

            spn.setAdapter(spinnerAdapter);
            spn.setSelection(findUser(refuelData.getDriverId(), userList));

            spn = (Spinner) dialog.findViewById(R.id.select_user_operator);

            spn.setAdapter(spinnerAdapter);
            spn.setSelection(findUser(refuelData.getOperatorId(), userList));

            dialog.show();

            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog.findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Spinner spnDriver = (Spinner) dialog.findViewById(R.id.select_user_driver);
                    UserModel driver = (UserModel)spnDriver.getSelectedItem();


                    Spinner spnOperator = (Spinner) dialog.findViewById(R.id.select_user_operator);
                    UserModel operator = (UserModel)spnOperator.getSelectedItem();

                    if (driver.getId() == operator.getId())
                    {
                        new AlertDialog.Builder(dialog.getContext())
                                .setTitle(R.string.select_user)
                                .setMessage(R.string.error_same_user)
                                .setIcon(R.drawable.ic_error)
                                .show();
                        return;
                    }

                    if (driver!=null) {
                        refuelData.setDriverId(driver.getId());
                        refuelData.setDriverName(driver.getName());
                    }

                    if (operator!=null) {
                        refuelData.setOperatorId(operator.getId());
                        refuelData.setOperatorName(operator.getName());
                    }

                    updateBinding();
                    dialog.dismiss();
                }
            });

            dialog.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    }
    private int findUser(int userId, List<UserModel> userList)
    {
        int pos = 0;
        for(UserModel item: userList)
        {
            if (item.getId() == userId)
                return pos;
            pos++;
        }
        return -1;
    }
    private void updateBinding() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataHelper.postRefuel(refuelData);
            }
        }).start();
        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL) {
            binding.invalidateAll();
            truckArrayAdapter.notifyDataSetChanged();
        } else
            extractBinding.invalidateAll();


    }

    private boolean vatFirstClick = true;
    private boolean airlineFirstClick = true;

    private PRINT_MODE printMode = PRINT_MODE.ALL_ITEM;

    private void openVatSpinner() {

        Spinner vatSpinner = findViewById(R.id.refuel_preview_vat_spinner);

        ((ArrayAdapter)vatSpinner.getAdapter()).setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        vatSpinner.setVisibility(View.VISIBLE);
        findViewById(R.id.refuel_preview_vat).setVisibility(View.GONE);

        vatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (vatFirstClick) {
                    vatFirstClick = false;
                    return;

                }
                Spinner vatSpinner = (Spinner) parent;
                String vat = vatSpinner.getSelectedItem().toString();
                NumberFormat format = NumberFormat.getPercentInstance();
                try {
                    refuelData.setTaxRate(format.parse(vat).doubleValue());
                } catch (ParseException e) {

                }
                vatSpinner.setVisibility(View.GONE);
                findViewById(R.id.refuel_preview_vat).setVisibility(View.VISIBLE);
                updateBinding();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Spinner vatSpinner = (Spinner) parent;
                vatSpinner.getSelectedItem();
            }
        });

        vatSpinner.performClick();

    }

    private void openAirlineDialog() {
        Dialog airlineDlg = new Dialog(this);
        airlineDlg.setTitle(R.string.app_name);
        airlineDlg.setContentView(R.layout.airline_select_dialog);
        SearchView searchView = (SearchView) airlineDlg.findViewById(R.id.airline_dlg_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ListView lvAirline = airlineDlg.findViewById(R.id.list_airline);
                AirlineArrayAdapter adapter = (AirlineArrayAdapter) lvAirline.getAdapter();
                adapter.getFilter().filter(query);
                adapter.notifyDataSetChanged();
                //lvAirline.setAdapter(adapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ListView lvAirline = airlineDlg.findViewById(R.id.list_airline);
                AirlineArrayAdapter adapter = (AirlineArrayAdapter) lvAirline.getAdapter();
                adapter.getFilter().filter(newText);
                adapter.notifyDataSetChanged();

                return false;
            }
        });
        ListView lvAirline = airlineDlg.findViewById(R.id.list_airline);
        lvAirline.setAdapter(new AirlineArrayAdapter(this, airlines));
        lvAirline.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AirlineModel airline = (AirlineModel) parent.getItemAtPosition(position);
                setAirline(airline);
                AirlineArrayAdapter adapter = (AirlineArrayAdapter) lvAirline.getAdapter();
                adapter.getFilter().filter("");
                airlineDlg.dismiss();
//                for (int j = 0; j < parent.getChildCount(); j++) {
//                    if (j==position)
//                    {
//                        parent.getChildAt(j).setBackgroundColor( Color.LTGRAY);
//                        ((CheckedTextView) parent.getChildAt(j).findViewById(R.id.airline_item_check)).setChecked(true);
//                    }
//                    else {
//                        parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
//                        ((CheckedTextView) parent.getChildAt(j).findViewById(R.id.airline_item_check)).setChecked(j == position);
//                    }
//                }

                // change the background color of the selected element
                //view.setBackgroundColor(Color.LTGRAY);
                //((CheckedTextView) view.findViewById(R.id.airline_item_check)).setChecked(true);

            }

        });
        airlineDlg.show();
    }

    private void setAirline(RefuelItemData item, AirlineModel selected) {
        item.setAirlineId(selected.getId());
        item.setPrice(selected.getPrice());
        item.setCurrency(selected.getCurrency());
        item.setUnit(selected.getUnit());
        item.setProductName(selected.getProductName());
        item.setTaxRate(!refuelData.isInternational() && selected.isInternational() ? 0.1 : 0);

        item.setAirlineModel(selected);
    }

    private void setAirline(AirlineModel selected) {

        setAirline(refuelData, selected);


        updateBinding();
    }

    private void openAirlineSpinner() {

        Spinner airline_spinner = findViewById(R.id.refuel_preview_airline_spinner);

        airline_spinner.setVisibility(View.VISIBLE);
        findViewById(R.id.refuel_preview_airline).setVisibility(View.GONE);

        airline_spinner.performClick();

        airline_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (airlineFirstClick) {
                    airlineFirstClick = false;
                    return;

                }
                Spinner airline_spinner = (Spinner) parent;
                AirlineModel selected = (AirlineModel) parent.getItemAtPosition(position);
                refuelData.setAirlineId(selected.getId());
                refuelData.setPrice(selected.getPrice());
                refuelData.setCurrency(selected.getCurrency());
                refuelData.setUnit(selected.getUnit());
                refuelData.setProductName(selected.getProductName());
                refuelData.setTaxRate(!refuelData.isInternational() && selected.isInternational() ? 0.1 : 0);

                refuelData.setAirlineModel(selected);

                airline_spinner.setVisibility(View.GONE);
                findViewById(R.id.refuel_preview_airline).setVisibility(View.VISIBLE);
                updateBinding();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    private String m_Text = "";
    private String m_Title = "";
    private void showEditDialog(final int id, int inputType)
    {
        showEditDialog(id, inputType, ".*");
    }
    private void showEditDialog(final int id, int inputType, String pattern) {

        if (!isEditable)
        {
            Toast.makeText(this,R.string.edit_not_allow, Toast.LENGTH_LONG ).show();
            return;
        }


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(m_Title);
        final EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setTypeface(Typeface.DEFAULT);
        input.setText(((TextView) findViewById(id)).getText());
        input.setSelectAllOnFocus(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_DONE;
            }


        });
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //doUpdateResult();

            }

            private void doUpdateResult() {
                m_Text = input.getText().toString();
                switch (id) {
                    case R.id.refuel_preview_aircraftCode:
                        refuelData.setAircraftCode(m_Text);
                        //((TextView)findViewById(R.id.lblAircraftNo)).setText(refuelData.getAircraftCode());
                        break;
                    case R.id.refuel_preview_aircraftType:
                        refuelData.setAircraftType(m_Text);
                        //((TextView)findViewById(R.id.lblAircraftNo)).setText(refuelData.getAircraftCode());
                        break;
                    case R.id.refuel_preview_Density:
                        refuelData.setDensity(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_Density)).setText(String.format("%.2f",refuelData.getDensity()));
                        break;
                    case R.id.refuel_preview_Temperature:

                        refuelData.setManualTemperature(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_Temperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));

                        break;
                    case R.id.refuel_preview_realAmount:
                        refuelData.setRealAmount(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;
                    case R.id.refuel_preview_qc_no:
                        refuelData.setQualityNo(m_Text);
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;
                    case R.id.refuel_preview_parking:
                        refuelData.setParkingLot(m_Text);
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;

                    case R.id.refuel_preview_return:
                        refuelData.setReturnAmount(Double.parseDouble(m_Text.replace(",", "")));
                        break;

                    case R.id.refuel_preview_weight_note:
                        refuelData.setWeightNote(m_Text);
                        break;
                    case R.id.refuel_preview_invoice_number:
                        refuelData.setInvoiceNumber(m_Text);

                        break;
                }
                updateBinding();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();// builder.show();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doUpdateResult())
                    dialog.dismiss();
            }
            private boolean doUpdateResult() {

                m_Text = input.getText().toString();
                Pattern regex = Pattern.compile(pattern);
                Matcher matcher = regex.matcher(m_Text);
                if (!matcher.find())
                {
                    Toast.makeText(getBaseContext(), getString(R.string.invalid_data),Toast.LENGTH_LONG).show();
                    return false;
                }
                switch (id) {
                    case R.id.refuel_preview_aircraftCode:
                        refuelData.setAircraftCode(m_Text);
                        //((TextView)findViewById(R.id.lblAircraftNo)).setText(refuelData.getAircraftCode());
                        break;

                    case R.id.refuel_preview_aircraftType:
                        refuelData.setAircraftType(m_Text);
                        break;

                    case R.id.refuel_preview_Density:
                        refuelData.setDensity(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_Density)).setText(String.format("%.2f",refuelData.getDensity()));
                        break;
                    case R.id.refuel_preview_Temperature:

                        refuelData.setManualTemperature(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_Temperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));

                        break;
                    case R.id.refuel_preview_realAmount:
                        refuelData.setRealAmount(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;
                    case R.id.refuel_preview_qc_no:
                        refuelData.setQualityNo(m_Text);
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;
                    case R.id.refuel_preview_parking:
                        refuelData.setParkingLot(m_Text);
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;

                    case R.id.refuel_preview_return:
                        refuelData.setReturnAmount(Double.parseDouble(m_Text.replace(",", "")));
                        break;

                    case R.id.refuel_preview_weight_note:
                        refuelData.setWeightNote(m_Text);
                        break;
                    case R.id.refuel_preview_invoice_number:
                        refuelData.setInvoiceNumber(m_Text);
                        updateAllInvoice();
                        break;
                }
                updateBinding();

                return true;
            }
        });
    }

    private  void updateAllInvoice()
    {
        if (printMode == PRINT_MODE.ALL_ITEM)
        {
            for(RefuelItemData item: allItems) {
                item.setInvoiceNumber(refuelData.getInvoiceNumber());
                if (!item.equals((refuelData)))
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new HttpClient().postRefuel(item);
                        }
                    }).start();
            }
        }
    }
    private void toggleEdit() {
        /*if (!isEditing) {
            findViewById(R.id.edtAircraft).setVisibility(View.VISIBLE);
            findViewById(R.id.edtTemperature).setVisibility(View.VISIBLE);
            findViewById(R.id.edtDensity).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_edtAmount).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_airline_spinner).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_vat_spinner).setVisibility(View.VISIBLE);
            findViewById(R.id.edtAircraft).requestFocus();
            findViewById(R.id.refuel_preview_aircraftCode).setVisibility(View.GONE);
            findViewById(R.id.refuel_preview_Temperature).setVisibility(View.GONE);
            findViewById(R.id.refuel_preview_Density).setVisibility(View.GONE);
            findViewById(R.id.refuel_preview_airline).setVisibility(View.GONE);
            findViewById(R.id.refuel_preview_vat).setVisibility(View.GONE);
            findViewById(R.id.refuel_preview_realAmount).setVisibility(View.GONE);
            ((Button) findViewById(R.id.btnEdit)).setText(getString(R.string.back));
            findViewById(R.id.btnUpdate).setVisibility(View.VISIBLE);
            isEditing = true;
        } else {
            findViewById(R.id.edtAircraft).setVisibility(View.GONE);
            findViewById(R.id.edtTemperature).setVisibility(View.GONE);
            findViewById(R.id.edtDensity).setVisibility(View.GONE);
            findViewById(R.id.refuel_preview_edtAmount).setVisibility(View.GONE);

            findViewById(R.id.refuel_preview_airline_spinner).setVisibility(View.GONE);
            findViewById(R.id.refuel_preview_vat_spinner).setVisibility(View.GONE);
            findViewById(R.id.refuel_preview_aircraftCode).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_Temperature).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_Density).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_airline).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_vat).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_realAmount).setVisibility(View.VISIBLE);
            ((Button) findViewById(R.id.btnEdit)).setText(getString(R.string.edit_refuel));
            findViewById(R.id.btnUpdate).setVisibility(View.GONE);
            isEditing = false;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }*/
    }

    private void save() {
        refuelData.setAircraftCode(((EditText) findViewById(R.id.edtAircraft)).getText().toString());
        //((TextView)findViewById(R.id.refuel_preview_aircraftCode)).setText(refuelData.getAircraftCode());
        refuelData.setManualTemperature(Double.parseDouble(((EditText) findViewById(R.id.edtTemperature)).getText().toString()));
        //((TextView) findViewById(R.id.refuel_preview_Temperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));
        refuelData.setDensity(Double.parseDouble(((EditText) findViewById(R.id.edtDensity)).getText().toString()));
        //((TextView) findViewById(R.id.refuel_preview_Density)).setText(String.format("%.4f",refuelData.getDensity()));
        refuelData.setRealAmount(Double.parseDouble(((EditText) findViewById(R.id.refuel_preview_edtAmount)).getText().toString()));
        //((TextView) findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.0f",refuelData.getRealAmount()));
        Spinner airline_spinner = findViewById(R.id.refuel_preview_airline_spinner);
        AirlineModel item = (AirlineModel) airline_spinner.getSelectedItem();
        refuelData.setAirlineId(item.getId());
        ((TextView) findViewById(R.id.refuel_preview_airline)).setText(item.getCode());
        try {
            Spinner vatSpinner = findViewById(R.id.refuel_preview_vat_spinner);
            String vat = vatSpinner.getSelectedItem().toString();
            NumberFormat format = NumberFormat.getPercentInstance();
            refuelData.setTaxRate(format.parse(vat).doubleValue());
            //((TextView) findViewById(R.id.refuel_preview_vat)).setText(vat);

        } catch (Exception e) {
        }
        updateBinding();
        //toggleEdit();
//        ActivityRefuelPreviewBinding shiftBinding = DataBindingUtil.setContentView(this, R.layout.activity_refuel_preview);
//        shiftBinding.setMItem(refuelData);

    }

    private boolean isEditing = false;

    @SuppressLint("StaticFieldLeak")
    private void createNewItem() {

        try {
            final RefuelItemData itemData = (RefuelItemData) refuelData.clone();
            itemData.setId(0);
            itemData.setLocalId(0);
            itemData.setTruckId(currentApp.getSetting().getTruckId());
            itemData.setTruckNo(currentApp.getTruckNo());
            itemData.setStatus(REFUEL_ITEM_STATUS.NONE);
            itemData.setStartNumber(0);
            itemData.setRealAmount(0);
            itemData.setEndNumber(0);
            itemData.setOthers(null);

            new AsyncTask<Void, Void, RefuelItemData>() {
                @Override
                protected RefuelItemData doInBackground(Void... voids) {
                    //RefuelItemData response = DataHelper.postRefuel(itemData);
                    return itemData;
                }

                @Override
                protected void onPostExecute(RefuelItemData response) {
                    postRefuelCompleted(response);
                    super.onPostExecute(response);
                }
            }.execute();
        } catch (CloneNotSupportedException ex) {
            Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void postRefuelCompleted(RefuelItemData itemData) {
        if (itemData != null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = gson.toJson(itemData);

            Intent intent = new Intent(this, RefuelDetailActivity.class);
            intent.putExtra("REFUEL", data);
            startActivityForResult(intent, REFUEL_WINDOW);
            finish();
        }
    }

}
