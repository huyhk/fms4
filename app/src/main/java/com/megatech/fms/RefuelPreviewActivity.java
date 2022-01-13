package com.megatech.fms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.databinding.ActivityRefuelPreviewBinding;
import com.megatech.fms.databinding.InvoicePreviewBinding;
import com.megatech.fms.databinding.PreviewExtractBinding;
import com.megatech.fms.databinding.RefuelBm2508Binding;
import com.megatech.fms.databinding.SelectUserBinding;
import com.megatech.fms.enums.INVOICE_TYPE;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.helpers.Logger;
import com.megatech.fms.helpers.PrintWorker;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.InvoiceFormModel;
import com.megatech.fms.model.InvoiceModel;
import com.megatech.fms.model.REFUEL_ITEM_STATUS;
import com.megatech.fms.model.ReceiptModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.UserModel;
import com.megatech.fms.view.AirlineArrayAdapter;
import com.megatech.fms.view.InvoiceItemAdapter;
import com.megatech.fms.view.TruckArrayAdapter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.megatech.fms.helpers.PrintWorker.PRINT_MODE;
import static com.megatech.fms.helpers.PrintWorker.PrintStateListener;
import static com.megatech.fms.model.RefuelItemData.GALLON_TO_LITTER;

public class RefuelPreviewActivity extends UserBaseActivity implements View.OnClickListener {

    private PrintWorker printWorker;
    private final int REFUEL_WINDOW = 1;
    private final int RECEIPT_WINDOW = 1;
    List<AirlineModel> airlines = null;

    /// bind data to view
    ArrayList<RefuelItemData> allItems = new ArrayList<RefuelItemData>();
    ArrayList<RefuelItemData> printItems = new ArrayList<RefuelItemData>();

    private boolean printTest = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        remoteId = b.getInt("REFUEL_ID", 0);
        localId = b.getInt("REFUEL_LOCAL_ID", 0);

        loadData();

        Drawable drawable = getResources().getDrawable(R.drawable.ic_edit, getTheme());
        drawable.setAlpha(90);

        printWorker = new PrintWorker(this);


        printWorker.setPrintStateListener(new PrintStateListener() {
            @Override
            public void onConnectionError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorMessage(R.string.printer_error);
                    }
                });
            }

            @Override
            public void onError() {
                if (refuelData != null)
                    refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage(R.string.validate, R.string.print_error, R.drawable.ic_error, new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                m_Title = getString(R.string.invoice_number);
                                showEditDialog(R.id.refuel_preview_invoice_number, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, true);
                                return null;
                            }
                        });
                    }
                });
            }

            @Override
            public void onSuccess() {

                if (!printTest) {
                    if (refuelData != null)
                        refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.SUCCESS);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_Title = getString(R.string.invoice_number);
                            showEditDialog(R.id.refuel_preview_invoice_number, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, true);
                        }
                    });
                }

            }

        });

    }

    private int localId;
    private int remoteId;

    //AlertDialog progressDialog;
    private void loadData() {

        setProgressDialog();
        new AsyncTask<Void, Void, RefuelItemData>() {
            @Override
            protected RefuelItemData doInBackground(Void... voids) {
                Logger.appendLog("PRW", "Start loading");

                airlines = DataHelper.getAirlines();

                if (userList == null)
                    userList = DataHelper.getUsers();


                refuelData = DataHelper.getRefuelItem(remoteId, localId);
                return refuelData;
            }

            @Override
            protected void onPostExecute(RefuelItemData itemData) {
                Logger.appendLog("PRW", "End loading");
                refuelData = itemData;
                bindData();
                super.onPostExecute(itemData);

            }
        }.execute();

    }

    boolean isEditable = true;
    ActivityRefuelPreviewBinding binding;
    InvoicePreviewBinding previewBinding;
    PreviewExtractBinding extractBinding;
    TruckArrayAdapter truckArrayAdapter;
    RefuelItemData refuelData;
    int result = Activity.RESULT_OK;


    InvoiceModel invoiceModel;
    BaseDialog printDialog;
    Locale locale = Locale.getDefault();
    NumberFormat numberFormat = NumberFormat.getInstance(locale);


    //validate data before print preview
    private boolean validate()
    {
        return validate(false);
    }
    private boolean validate(boolean isReturn) {
        if (printItems == null || printItems.size() <= 0) {
            showErrorMessage(R.string.preview_empty_list);
            return false;
        }
        /*
        if (refuelData.getAircraftCode() == null || refuelData.getAircraftCode().isEmpty()) {
            showErrorMessage(R.string.invalid_aircraft_code);
            return false;
        }
        */
        if (printMode == PRINT_MODE.ONE_ITEM) {
            boolean valid = refuelData.getManualTemperature() > 0 && refuelData.getDensity() > 0;
            boolean validQC = refuelData.getQualityNo() != null && !refuelData.getQualityNo().isEmpty();
            if (!valid) {
                showErrorMessage(R.string.invalid_density_temperature);
            } else if (!validQC) {
                showErrorMessage(R.string.invalid_qc_no);
            }

            return valid && validQC;
        } else {

            boolean valid = true;
            boolean validQC = true;
            boolean hasReturn = false;
            for (int i = 0; i < printItems.size(); i++) {
                RefuelItemData item = printItems.get(i);
                valid = (item.getManualTemperature() > 0 && item.getDensity() > 0);

                validQC = item.getQualityNo() != null && !item.getQualityNo().isEmpty();

                hasReturn |= item.getReturnAmount()>0;

                if (!valid || !validQC) {
                    truckArrayAdapter.setSelectedObject(item);
                    //ListView lv = findViewById(R.id.refuel_preview_truck_list);
                    //lv.setSelection(i);
                    refuelData = item;
                    binding.setMItem(refuelData);
                    isEditable = refuelData.getTruckNo().equals(currentApp.getTruckNo());
                    //for (int j = 0; j < lv.getChildCount(); j++)
                    //    lv.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);
                    // change the background color of the selected element
                    //lv.getAdapter().getView(i, null, lv).setBackgroundColor(Color.LTGRAY);

                    break;
                }

            }
            if (!valid || !validQC) {
                showErrorMessage(!valid ? R.string.invalid_density_temperature : R.string.invalid_qc_no);
            }

            if (isReturn && !hasReturn) {
                showErrorMessage(R.string.no_return_amount);
                return false;
            }
            return valid && validQC;
        }

    }

    private void bindData() {
        Logger.appendLog("PRW", "Start binding");
        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL)
            setContentView(R.layout.activity_refuel_preview);
        else
            setContentView(R.layout.preview_extract);

        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL) {
            //binding =  DataBindingUtil.setContentView(this, R.layout.activity_refuel_preview);
            binding =  DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_refuel_preview,null,false);
            binding.setMItem(refuelData);
            setContentView(binding.getRoot());
        } else {
            //extractBinding = DataBindingUtil.setContentView(this, R.layout.preview_extract);
            extractBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.preview_extract,null,false);

            extractBinding.setMItem(refuelData);

            setContentView(extractBinding.getRoot());

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
                    setAirline(refuelData, item);
                    break;
                }
            }

            airline_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    AirlineModel selected = (AirlineModel) parent.getItemAtPosition(position);
                    refuelData.setInvoiceNameCharter(null);
                    setAirline(selected);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (refuelData.getOthers().size() > 0) {
                for (int i = 0; i < refuelData.getOthers().size(); i++)
                    refuelData.getOthers().get(i).setAirlineModel(refuelData.getAirlineModel());
            }
        }
        allItems.clear();
        allItems.add(refuelData);

        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL) {

            //allItems.add(refuelData);
            allItems.addAll(refuelData.getOthers());

            Collections.sort(allItems, new Comparator<RefuelItemData>() {
                @Override
                public int compare(RefuelItemData o1, RefuelItemData o2) {
                    return o1.getEndTime().compareTo(o2.getEndTime());
                }
            });
            int selectedPos = 0;
            for (int i = 0; i < allItems.size(); i++)
                if (allItems.get(i).getId().equals(refuelData.getId()))
                    selectedPos = i;
            truckArrayAdapter = new TruckArrayAdapter(this, allItems);
            truckArrayAdapter.setSelected(selectedPos);
            ListView lv = findViewById(R.id.refuel_preview_truck_list);
            lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            lv.setAdapter(truckArrayAdapter);
            lv.setSelection(selectedPos);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    refuelData = (RefuelItemData) parent.getItemAtPosition(position);
                    binding.setMItem(refuelData);
                    isEditable = refuelData.getTruckNo().equals(currentApp.getTruckNo());

                    truckArrayAdapter.setSelectedObject(refuelData);


                }

            });


        }
        //Logger.appendLog("PRW","End binding");
        closeProgressDialog();
    }

    private boolean oldTemplate = true;
    InvoiceFormModel[] invoiceForms;
    //List<InvoiceFormModel> invoiceForms;
    //List<InvoiceFormModel> billForms;
    InvoiceFormModel defaultModel = null;
    private void openReceipt()
    {
        openReceipt(false);
    }
    private void openReceipt(boolean isReturn)
    {

        ListView lv = findViewById(R.id.refuel_preview_truck_list);
        printItems = ((TruckArrayAdapter) lv.getAdapter()).getCheckedItems();
        if (!isReturn) {

            if (validate()) {
                ReceiptModel model = ReceiptModel.createReceipt(printItems);
                Intent intent = new Intent(this, InvoiceActivity.class);
                intent.putExtra("RECEIPT", model.toJson());
                startActivityForResult(intent, RECEIPT_WINDOW);
            }
        }
        else
        {
            if (validate(isReturn)) {
                ReceiptModel model = ReceiptModel.createReceipt(printItems, true);
                Intent intent = new Intent(this, InvoiceActivity.class);
                intent.putExtra("RECEIPT", model.toJson());
                //startActivityForResult(intent, RECEIPT_WINDOW);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECEIPT_WINDOW && resultCode == Activity.RESULT_OK) {
            String number = data.getStringExtra("result");
            updateAllReceipt(number);
            truckArrayAdapter.notifyDataSetChanged();
        }
    }

    private void preview() {
        try {
            SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
            oldTemplate = preferences.getBoolean("OLD_TEMPLATE", true);

            ListView lv = findViewById(R.id.refuel_preview_truck_list);
            printItems = ((TruckArrayAdapter) lv.getAdapter()).getCheckedItems();


            if (refuelData.getAirlineId() <= 0 || refuelData.getAirlineModel() == null) {
                showErrorMessage(R.string.invalid_airline_model);

                return;
            }
            if (refuelData.getDriverId() == 0 || refuelData.getOperatorId() == 0) {
                showErrorMessage(R.string.invalid_user);
                return;
            }
            if (!validate()) {

                return;
            }
            invoiceForms = FMSApplication.getApplication().getInvoiceForms();
            //invoiceForms = InvoiceFormModel.getInvoiceForms(InvoiceFormModel.INVOICE_TYPE.INVOICE);
            //billForms = InvoiceFormModel.getInvoiceForms(InvoiceFormModel.INVOICE_TYPE.BILL);

            defaultModel = null;

            invoiceModel = InvoiceModel.fromRefuel(refuelData, printItems);
            setDefaultForm();
            //setInvoiceForm(defaultModel);
            printDialog = new BaseDialog(this);

            previewBinding = DataBindingUtil.inflate(printDialog.getLayoutInflater(), R.layout.invoice_preview, null, false);
            previewBinding.setInvoiceItem(invoiceModel);
            printDialog.setContentView(previewBinding.getRoot());
            RadioGroup radioGroup = printDialog.findViewById(R.id.radioTemplate);
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                int id = checkedId;

                invoiceModel.setPrintTemplate(id == R.id.radInvoice ? INVOICE_TYPE.INVOICE : INVOICE_TYPE.BILL);
                setDefaultForm();
                previewBinding.invalidateAll();

            });
            int selectedIndex = !refuelData.isInternational() && !refuelData.getAirlineModel().isInternational() ? 1 : 0;


            ((RadioButton) radioGroup.getChildAt(selectedIndex)).setChecked(true);

            ((RadioButton) printDialog.findViewById(R.id.radOld)).setChecked(oldTemplate);


            InvoiceItemAdapter itemAdapter = new InvoiceItemAdapter(this, invoiceModel.getItems());
            ((ListView) printDialog.findViewById(R.id.invoice_preview_item_list)).setAdapter(itemAdapter);
            printDialog.findViewById(R.id.btnPrintInvoice).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean invoice = ((RadioButton) radioGroup.getChildAt(0)).isChecked();
                    oldTemplate = ((RadioButton) printDialog.findViewById(R.id.radOld)).isChecked();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("OLD_TEMPLATE", oldTemplate);
                    editor.commit();
                    print(oldTemplate, invoice, false);

                    printDialog.findViewById(R.id.row_invoice_number).setVisibility(View.VISIBLE);
                }
            });

            printDialog.findViewById(R.id.btnPrintTest).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean invoice = ((RadioButton) radioGroup.getChildAt(0)).isChecked();
                    oldTemplate = ((RadioButton) printDialog.findViewById(R.id.radOld)).isChecked();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("OLD_TEMPLATE", oldTemplate);
                    editor.commit();


                    print(oldTemplate, invoice, true);

                    //printDialog.findViewById(R.id.row_invoice_number).setVisibility(View.VISIBLE);
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
        } catch (Exception ex) {
            Logger.appendLog(ex.getMessage());
        }

    }

    private void print(boolean oldTemplate, boolean invoice) {
        print(oldTemplate, invoice, false);
    }

    private void print(boolean oldTemplate, boolean invoice, boolean test) {
        printTest = test;
        if (invoice)
            printInvoice(oldTemplate);
        else printBill(oldTemplate);
    }

    private void setDefaultForm() {
        if (invoiceModel != null) {
            InvoiceFormModel invoiceDefault, billDefault;

            for (InvoiceFormModel item : invoiceForms) {

                if (item.isLocalDefault() && item.getPrintTemplate() == invoiceModel.getPrintTemplate()) {
                    defaultModel = item;
                    break;
                } else if (item.isDefault() && item.getPrintTemplate() == invoiceModel.getPrintTemplate())
                    defaultModel = item;

            }
            if (defaultModel != null) {
                invoiceModel.setInvoiceFormId(defaultModel.getId());
                invoiceModel.setFormNo(defaultModel.getFormNo());
                invoiceModel.setSign(defaultModel.getSign());
                //setInvoiceForm(defaultModel);
            }
        }
    }

    private void printBill(boolean old) {
        /*if (!new PrintWorker(this).printItem(refuelData, printMode, INVOICE_TYPE.BILL)) {
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
        //boolean isOK = new PrintWorker(this).printItem(refuelData, printMode, INVOICE_TYPE.INVOICE);

        boolean isOK = printWorker.printInvoice(invoiceModel, old);

        if (!isOK) {
            refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
        } else {

            //showEditDialog(R.id.refuel_preview_invoice_number, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        }

//        setResult(RESULT_OK);
//        finish();

    }

    private void checkAll(boolean checked) {
        ListView lv = findViewById(R.id.refuel_preview_truck_list);
        if (checked)
            ((TruckArrayAdapter) lv.getAdapter()).selectAll();
        else
            ((TruckArrayAdapter) lv.getAdapter()).selectNone();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        int id = v.getId();
        switch (id) {
            case R.id.truck_item_select_all:
                checkAll(((CheckBox) v).isChecked());
                break;
            case R.id.refuel_preview_print_refuel:
                if (refuelData.getStatus() != REFUEL_ITEM_STATUS.DONE) {
                    doRefuel();
                }
                break;

            case R.id.refuel_preview_print_current:
                printMode = PRINT_MODE.ONE_ITEM;
                openReceipt();
                break;
            case R.id.refuel_preview_print_return:
                printMode = PRINT_MODE.ALL_ITEM;
                openReceipt(true);
                break;
            case R.id.refuel_preview_print_all:
                printMode = PRINT_MODE.ALL_ITEM;

                    preview();
                break;
            case R.id.refuel_preview_new_item:
                createNewItem();
                break;
            case R.id.btnUpdate:
                //save();
                break;
            case R.id.refuel_preview_charter_name:
                m_Title = getString(R.string.update_charter_name);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ".+");
                break;
            case R.id.refuel_preview_aircraftCode:
                m_Title = getString(R.string.update_aircraftCode);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ".+");
                break;
            case R.id.refuel_preview_aircraftType:
                m_Title = getString(R.string.update_aircraftType);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ".+");
                break;
            case R.id.refuel_preview_realAmount:

                m_Title = getString(R.string.update_real_amount);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.refuel_preview_weight:
                if (refuelData.getDensity() <= 0)
                    showErrorMessage(R.string.density_must_input);
                else {
                    m_Title = getString(R.string.update_real_amount_kg);
                    showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
                break;
            case R.id.refuel_preview_Density:
                m_Title = getString(R.string.update_density);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.refuel_preview_Temperature:
                m_Title = getString(R.string.update_temparature);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.refuel_preview_qc_no:
                m_Title = getString(R.string.update_qc_no);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ".+");
                break;
            case R.id.refuel_preview_parking:
                m_Title = getString(R.string.update_parking_lot);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ".+");
                break;
            case R.id.refuel_preview_vat:
                openVatSpinner();
                break;
            case R.id.refuel_preview_airline:
                openAirlineDialog();
                break;
            case R.id.refuel_preview_return:
                if (refuelData.getDensity() <= 0)
                    showErrorMessage(R.string.density_must_input);
                else {
                    m_Title = getString(R.string.update_return_amount);
                    showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
                break;
            case R.id.refuel_preview_weight_note:
                m_Title = getString(R.string.update_weight_note);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;
            case R.id.refuel_preview_routeName:
                m_Title = getString(R.string.update_route_name);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                break;
            case R.id.refuel_preview_return_invoice_number:
                m_Title = getString(R.string.update_return_invoice_number);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;
            case R.id.refuel_preview_price:
                m_Title = getString(R.string.update_price);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.refuel_preview_international:
                updatePrice();
                break;
            case R.id.refuel_preview_driver:
            case R.id.refuel_preview_operator:
                showSelectUser();
                break;
            case R.id.refuel_preview_starttime:
            case R.id.refuel_preview_endtime:
                showTimeDialog(id);
                break;

            case R.id.preview_form_no:
            case R.id.preview_sign:
                showFormNoSpinner();
                break;

            case R.id.preview_invoice_number:
                m_Title = getString(R.string.invoice_number);
                showEditDialog(R.id.refuel_preview_invoice_number, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, true);
                break;
            case R.id.btnBack:
                finish();
                break;

            case R.id.refuel_preview_refresh:
                loadData();
                break;
            case R.id.refuel_preview_check_form:
                openCheckForm();

                break;
            case R.id.refuel_preview_start_meter:
                m_Title = getString(R.string.update_start_meter);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, true);
                break;
            case R.id.refuel_preview_end_meter:
                m_Title = getString(R.string.update_end_meter);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, true);

                break;
        }

    }

    RefuelBm2508Binding checkFormBinding;
    boolean isNew = false;

    private void openCheckForm() {

        Dialog checkFormDlg = new BaseDialog(this);

        if (refuelData.getBM2508Result() == null) {
            refuelData.setBM2508Result(15);
            isNew = true;
        }
        checkFormBinding = DataBindingUtil.inflate(checkFormDlg.getLayoutInflater(), R.layout.refuel_bm_2508, null, false);
        checkFormBinding.setMItem(refuelData);
        checkFormDlg.setContentView(checkFormBinding.getRoot());
        checkFormDlg.setCanceledOnTouchOutside(false);
        checkFormDlg.setCancelable(false);

        checkFormDlg.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNew)
                    refuelData.setBM2508Result(null);
                checkFormDlg.dismiss();
            }
        });
        checkFormDlg.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmMessage(R.string.check_form_delete, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        refuelData.setBM2508Result(null);
                        updateBinding();
                        checkFormDlg.dismiss();
                        return null;
                    }
                });

            }
        });
        checkFormDlg.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                refuelData.setBM2508BondingCable(((CheckBox) checkFormDlg.findViewById(R.id.chk_bonding_cable)).isChecked());
                refuelData.setBM2508FuelingCap(((CheckBox) checkFormDlg.findViewById(R.id.chk_fueling_cap)).isChecked());
                refuelData.setBM2508FuelingHose(((CheckBox) checkFormDlg.findViewById(R.id.chk_fueling_hose)).isChecked());
                refuelData.setBM2508Ladder(((CheckBox) checkFormDlg.findViewById(R.id.chk_ladder)).isChecked());

                updateBinding();

                checkFormDlg.dismiss();
            }
        });


        checkFormDlg.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        checkFormDlg.show();
    }

    private void showFormNoSpinner() {
        //Spinner spn = printDialog.findViewById(R.id.preview_spinner_form_no);
        ArrayAdapter<InvoiceFormModel> adapter;
        if (invoiceModel.getPrintTemplate() == INVOICE_TYPE.INVOICE)
            adapter = new ArrayAdapter<InvoiceFormModel>(this, android.R.layout.simple_list_item_single_choice, Arrays.stream(invoiceForms).filter(x -> x.getPrintTemplate() == INVOICE_TYPE.INVOICE).collect(Collectors.toList()));
        else
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, Arrays.stream(invoiceForms).filter(x -> x.getPrintTemplate() == INVOICE_TYPE.BILL).collect(Collectors.toList()));
        //adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        int position = defaultModel != null ? adapter.getPosition(defaultModel) : -1;
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setSingleChoiceItems(adapter, position,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView lw = ((AlertDialog) dialog).getListView();
                        InvoiceFormModel checkedItem = adapter.getItem(which);
                        invoiceModel.setInvoiceFormId(checkedItem.getId());
                        invoiceModel.setFormNo(checkedItem.getFormNo());
                        invoiceModel.setSign(checkedItem.getSign());
                        previewBinding.invalidateAll();
                        //setInvoiceForm(checkedItem);
                        defaultModel = checkedItem;
                        saveDefaultForm(checkedItem);
                        dialog.dismiss();
                    }
                });
        b.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        Dialog d = b.create();
        d.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        d.show();
    }

    private void saveDefaultForm(InvoiceFormModel checkedItem) {
        SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (checkedItem.getPrintTemplate() == INVOICE_TYPE.BILL)
            editor.putInt("DEFAULT_FORM_BILL", checkedItem.getId());
        else
            editor.putInt("DEFAULT_FORM_INVOICE", checkedItem.getId());
        editor.commit();
    }

    private void setInvoiceForm(InvoiceFormModel checkedItem) {
        if (printItems != null && printItems.size() > 0) {
            for (RefuelItemData item : printItems) {
                item.setInvoiceFormId(checkedItem.getId());
                item.setFormNo(checkedItem.getFormNo());
                item.setSign(checkedItem.getSign());
            }


        }
    }


    private void updatePrice() {

        if (refuelData.getAirlineModel() != null) {
            if (refuelData.getAirlineModel().isInternational() && refuelData.isInternational()) {
                setAll(R.id.refuel_preview_price, refuelData.getAirlineModel().getPrice());
            } else
                setAll(R.id.refuel_preview_price, refuelData.getAirlineModel().getPrice01());

            setAll(R.id.refuel_preview_vat, refuelData.getAirlineModel().isInternational() && !refuelData.isInternational() ? 0.1 : 0);
            refuelData.setPrintTemplate(!refuelData.getAirlineModel().isInternational() && !refuelData.isInternational() ? INVOICE_TYPE.BILL : INVOICE_TYPE.INVOICE);
        }
        updateBinding();
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

        if (!isEditable) {
            Toast.makeText(this, R.string.edit_not_allow, Toast.LENGTH_LONG).show();
            return;
        }


        if (userList != null) {
            Dialog dialog = new Dialog(this);
            SelectUserBinding binding = DataBindingUtil.inflate(dialog.getLayoutInflater(), R.layout.select_user, null, false);
            binding.setRefuelItem(refuelData);
            dialog.setContentView(binding.getRoot());
            Spinner spn = dialog.findViewById(R.id.select_user_driver);

            ArrayAdapter<UserModel> spinnerAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, userList);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

            spn.setAdapter(spinnerAdapter);
            spn.setSelection(findUser(refuelData.getDriverId(), userList));

            spn = dialog.findViewById(R.id.select_user_operator);

            spn.setAdapter(spinnerAdapter);
            spn.setSelection(findUser(refuelData.getOperatorId(), userList));

            dialog.show();

            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog.findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Spinner spnDriver = dialog.findViewById(R.id.select_user_driver);
                    UserModel driver = (UserModel) spnDriver.getSelectedItem();


                    Spinner spnOperator = dialog.findViewById(R.id.select_user_operator);
                    UserModel operator = (UserModel) spnOperator.getSelectedItem();

                    if (driver.getId() == operator.getId()) {
                        new AlertDialog.Builder(dialog.getContext())
                                .setTitle(R.string.select_user)
                                .setMessage(R.string.error_same_user)
                                .setIcon(R.drawable.ic_error)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();
                        return;
                    }

                    if (driver != null) {
                        refuelData.setDriverId(driver.getId());
                        refuelData.setDriverName(driver.getName());
                    }

                    if (operator != null) {
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

    private int findUser(int userId, List<UserModel> userList) {
        int pos = 0;
        for (UserModel item : userList) {
            if (item.getId() == userId)
                return pos;
            pos++;
        }
        return -1;
    }

    private String LOG_TAG = "PRW";

    private void updateBinding() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.appendLog(LOG_TAG, "post all refuels");
                DataHelper.postRefuels(allItems);

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

        String[] vat_array = getResources().getStringArray(R.array.vat_array);
        int pos = Arrays.asList(vat_array).indexOf(String.format("%.0f%%", refuelData.getTaxRate() * 100));
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.update_tax_rate);

        b.setSingleChoiceItems(R.array.vat_array, pos, (dialog, which) -> {
            NumberFormat format = NumberFormat.getPercentInstance();
            try {
                setAll(R.id.refuel_preview_vat, format.parse(vat_array[which]).doubleValue());
                dialog.dismiss();
                updateBinding();
            } catch (ParseException e) {

            }

        });
        b.create().show();

    }

    private void openAirlineDialog() {
        Dialog airlineDlg = new Dialog(this);
        airlineDlg.setTitle(R.string.app_name);
        airlineDlg.setContentView(R.layout.airline_select_dialog);
        SearchView searchView = airlineDlg.findViewById(R.id.airline_dlg_search);
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
                refuelData.setInvoiceNameCharter(null);
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
        setAirline(item, selected, false);
    }

    private void setAirline(RefuelItemData item, AirlineModel selected, boolean updatePrice) {

        item.setAirlineId(selected.getId());
        //item.setPrice(selected.getPrice());
        /*if (item.isInternational() && item.isInternational())
            item.setPrice(selected.getPrice());
        else
            item.setPrice(selected.getPrice01());*/
        item.setCurrency(selected.getCurrency());
        item.setUnit(selected.getUnit());
        item.setProductName(selected.getProductName());
        //if (selected.getId() != item.getAirlineId())
        item.setTaxRate(!refuelData.isInternational() && selected.isInternational() ? 0.1 : 0);

        item.setAirlineModel(selected);

        //((TextView) findViewById(R.id.refuel_preview_airline)).setText(selected.getName());

        if (item.getInvoiceNameCharter() == null || item.getInvoiceNameCharter().isEmpty() || updatePrice)
            item.setInvoiceNameCharter(selected.getName());

        if (updatePrice) {
            if (item.isInternational())
                item.setPrice(selected.getPrice());
            else
                item.setPrice(selected.getPrice01());

            if (!item.isInternational() && selected.isInternational())
                item.setTaxRate(0.1);
            else
                item.setTaxRate(0);
        }
    }

    private void setAirline(AirlineModel selected) {

        for (RefuelItemData itemData : allItems)
            setAirline(itemData, selected, true);


        updateBinding();
    }

    private void setAll(int id, String text) {
        setAll(allItems, id, text);
    }

    private void setAll(ArrayList<RefuelItemData> items, int id, String text) {
        for (RefuelItemData item : items) {
            switch (id) {
                case R.id.refuel_preview_aircraftCode:
                    item.setAircraftCode(text);
                    break;
                case R.id.refuel_preview_aircraftType:
                    item.setAircraftType(text);
                    break;
                case R.id.refuel_preview_parking:
                    item.setParkingLot(text);
                    break;
                case R.id.refuel_preview_charter_name:
                    item.setInvoiceNameCharter(text);
                    break;

                case R.id.refuel_preview_routeName:
                    item.setRouteName(text);
                    break;
//                case R.id.refuel_preview_weight_note:
//                    item.setWeightNote(text);
//                    break;

            }
        }
    }

    private void setAll(int id, double val) {
        setAll(allItems, id, val);
    }

    private void setAll(ArrayList<RefuelItemData> items, int id, double val) {
        for (RefuelItemData item : items) {
            switch (id) {
                case R.id.refuel_preview_price:
                    item.setPrice(val);
                    //item.setChangeFlag(RefuelItemData.CHANGE_FLAG.PRICE);
                    break;
                case R.id.refuel_preview_vat:
                    item.setTaxRate(val);
                    break;
            }
        }

    }

    private void setAll(int id, boolean val) {
        setAll(allItems, id, val);
    }

    private void setAll(ArrayList<RefuelItemData> items, int id, boolean val) {
        for (RefuelItemData item : allItems) {
            switch (id) {
                case R.id.refuel_preview_international:
                    item.setInternational(val);
                    break;


            }
        }
    }

    private String m_Text = "";
    private String m_Title = "";

    private void showEditDialog(final int id, int inputType) {
        showEditDialog(id, inputType, ".*");
    }

    private void showEditDialog(final int id, int inputType, String pattern) {
        showEditDialog(id, inputType, pattern, false);
    }

    private void showEditDialog(final int id, int inputType, boolean required) {
        showEditDialog(id, inputType, ".*", required);
    }

    private void showEditDialog(final int id, int inputType, String pattern, boolean required) {

        if (!isEditable) {
            Toast.makeText(this, R.string.edit_not_allow, Toast.LENGTH_LONG).show();
            return;
        }

        Context context = this;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(m_Title);
        final EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setTypeface(Typeface.DEFAULT);

        input.setText(((TextView) findViewById(id)).getText());

        if (id == R.id.refuel_preview_routeName) {
            if (((TextView) findViewById(id)).getText().toString().isEmpty()) {
                SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
                String airport = preferences.getString("AIRPORT", "");
                if (airport != "")
                    input.setText(airport + "-");
            }


        }

        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) > 0)
            input.setKeyListener(DigitsKeyListener.getInstance("0123456789,."));

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_DONE;
            }


        });
        builder.setView(input);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        if (!required) {
            builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        final AlertDialog dialog = builder.create();// builder.show();
        dialog.setCancelable(!required);

        dialog.show();
        input.requestFocus();
        if (id == R.id.refuel_preview_Density)
            input.setSelection(2, input.getText().length());
        else
            input.setSelection(0, input.getText().length());

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doUpdateResult())
                    dialog.dismiss();
            }

            private boolean doUpdateResult() {

                m_Text = input.getText().toString().trim();
                if (required && m_Text.isEmpty()) {
                    showErrorMessage(R.string.empty_required_field);
                    return false;
                }
                Pattern regex = Pattern.compile(pattern);
                Matcher matcher = regex.matcher(m_Text);
                if (!matcher.find()) {
                    Toast.makeText(getBaseContext(), getString(R.string.invalid_data), Toast.LENGTH_LONG).show();
                    return false;
                }
                try {
                    switch (id) {
                        case R.id.refuel_preview_aircraftCode:

                        case R.id.refuel_preview_charter_name:

                        case R.id.refuel_preview_aircraftType:
                            //case R.id.refuel_preview_weight_note:

                        case R.id.refuel_preview_routeName:

                        case R.id.refuel_preview_parking:
                            setAll(id, m_Text);
                            break;
                        case R.id.refuel_preview_weight_note:
                            refuelData.setWeightNote(m_Text);
                            break;
                        case R.id.refuel_preview_invoice_number:
                        case R.id.preview_invoice_number:
                            //refuelData.setInvoiceNumber(m_Text);
                            if (!updateAllInvoice(m_Text)) {
                                showErrorMessage(R.string.duplicate_invoice_number);
                                return false;
                            }
                            break;

                        case R.id.refuel_preview_Density:
                            double d = numberFormat.parse(m_Text).doubleValue();
                            if (d < 0.72 || d > 0.86) {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.error_data)
                                        .setMessage(R.string.invalid_density)
                                        .setIcon(R.drawable.ic_error)
                                        .setPositiveButton("OK", (dialog1, which) -> {
                                            dialog1.dismiss();
                                        })
                                        .create()
                                        .show();
                                return false;
                            }
                            refuelData.setDensity(d);
                            //((TextView)findViewById(R.id.refuel_preview_Density)).setText(String.format("%.2f",refuelData.getDensity()));
                            break;
                        case R.id.refuel_preview_Temperature:
                            double t = numberFormat.parse(m_Text).doubleValue();
                            refuelData.setManualTemperature(t);
                            //((TextView)findViewById(R.id.refuel_preview_Temperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));

                            break;
                        case R.id.refuel_preview_realAmount:
                            double realAmount = numberFormat.parse(m_Text).doubleValue();
                            refuelData.setRealAmount(realAmount);
                            refuelData.setChangeFlag(RefuelItemData.CHANGE_FLAG.GROSS_QTY);


                            //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                            break;

                        case R.id.refuel_preview_weight:
                            double weight = numberFormat.parse(m_Text).doubleValue();
                            double gallon = refuelData.getDensity() == 0 ? 0 : Math.round(Math.round(weight / refuelData.getDensity()) / GALLON_TO_LITTER);
                            refuelData.setRealAmount(gallon);
                            refuelData.setChangeFlag(RefuelItemData.CHANGE_FLAG.GROSS_QTY);

                        case R.id.refuel_preview_qc_no:
                            refuelData.setQualityNo(m_Text);
                            //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                            break;
                        case R.id.refuel_preview_price:
                            setAll(id, numberFormat.parse(m_Text).doubleValue());
                            refuelData.setChangeFlag(RefuelItemData.CHANGE_FLAG.PRICE);
                            //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                            break;
                        case R.id.refuel_preview_return:
                            double returnAmount = numberFormat.parse(m_Text).doubleValue();
                            calculateReturnAmount(returnAmount);
                            break;

                        case R.id.refuel_preview_return_invoice_number:

                            refuelData.setReturnInvoiceNumber(m_Text);
                            break;

                    }
                } catch (ParseException ex) {
                    Toast.makeText(getBaseContext(), R.string.invalid_number_format, Toast.LENGTH_LONG).show();
                    return false;
                }
                updateBinding();

                return true;
            }
        });
    }

    private void calculateReturnAmount(double returnAmount) {

        if (refuelData.getDensity() > 0) {
            double vol = Math.round(returnAmount / refuelData.getDensity());
            double gal = Math.round(vol / GALLON_TO_LITTER);
            double newAmount = Math.round(Math.round(gal * GALLON_TO_LITTER) * refuelData.getDensity());
            if (gal > refuelData.getRealAmount()) {
                showWarningMessage(getString(R.string.return_amount_greater_warning));
            } else if (newAmount != returnAmount) {
                showWarningMessage(getString(R.string.new_return_amount_value) + " " + newAmount + " KG");

            }

            refuelData.setReturnAmount(newAmount);
        }
    }
    private boolean updateAllReceipt(String invoiceNumber) {
        for (RefuelItemData item : printItems) {
            if (item.getInvoiceNumber() != null && item.getInvoiceNumber().equals(invoiceNumber)) {
                return false;
            }
        }

        for (RefuelItemData item : printItems) {
            item.setReceiptNumber(invoiceNumber);
            item.setReceiptCount(item.getReceiptCount() + 1);
            item.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.SUCCESS);

        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                DataHelper.postRefuels(printItems);
            }
        }).start();

        return true;
    }
    private boolean updateAllInvoice(String invoiceNumber) {
        for (RefuelItemData item : printItems) {
            if (item.getInvoiceNumber() != null && item.getInvoiceNumber().equals(invoiceNumber)) {
                return false;
            }
        }

        for (RefuelItemData item : printItems) {
            item.setInvoiceNumber(invoiceNumber);
            item.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.SUCCESS);

            item.setPrice(refuelData.getPrice());
            item.setTaxRate(refuelData.getTaxRate());
            item.setPrintTemplate(invoiceModel.getPrintTemplate());
            item.setInvoiceFormId(invoiceModel.getInvoiceFormId());

            //item.setInvoiceModel(invoiceModel);

        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                DataHelper.postInvoice(invoiceModel);
                DataHelper.postRefuels(printItems);
            }
        }).start();
        if (printDialog != null)
            printDialog.dismiss();
        return true;
    }


    private Context context = this;
    private int mHour, mMinute, mYear, mMonth, mDay;

    private void showTimeDialog(int id) {
        final Date date = new Date();
        if (id == R.id.refuel_preview_starttime)
            date.setTime(refuelData.getStartTime().getTime());
        else
            date.setTime(refuelData.getEndTime().getTime());

        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(year, month, dayOfMonth);
                TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                c.set(Calendar.MINUTE, minute);
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                if (id == R.id.refuel_preview_starttime)
                                    refuelData.setStartTime(c.getTime());
                                else if (id == R.id.refuel_preview_endtime)
                                    refuelData.setEndTime(c.getTime());

                                updateBinding();
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
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
            itemData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.NONE);

            itemData.setStartNumber(0);
            itemData.setRealAmount(0);
            itemData.setEndNumber(0);
            itemData.setInvoiceNumber("");
            itemData.setReturnAmount(0);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refuelData = null;
        Runtime.getRuntime().gc();
    }
}
