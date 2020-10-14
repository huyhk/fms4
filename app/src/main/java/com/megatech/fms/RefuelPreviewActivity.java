package com.megatech.fms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.databinding.ActivityRefuelPreviewBinding;
import com.megatech.fms.databinding.InvoicePreviewBinding;
import com.megatech.fms.databinding.PreviewExtractBinding;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.InvoiceItemModel;
import com.megatech.fms.model.InvoiceModel;
import com.megatech.fms.model.REFUEL_ITEM_STATUS;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.PrintWorker;
import com.megatech.fms.view.InvoiceItemAdapter;
import com.megatech.fms.view.TruckArrayAdapter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class RefuelPreviewActivity extends UserBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadData();
        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL)
            setContentView(R.layout.activity_refuel_preview);
        else
            setContentView(R.layout.preview_extract);
        bindData();

        Drawable drawable = getResources().getDrawable(R.drawable.ic_edit);
        drawable.setAlpha(90);
        /*Button btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEdit();
            }
        });
        */
    }

    private void loadData() {


        Bundle b = getIntent().getExtras();
        Integer id = b.getInt("REFUEL_ID", 2);
        String mData = b.getString("REFUEL", "");
        if (mData != null && mData != "") {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            refuelData = gson.fromJson(mData, RefuelItemData.class);
        }
        if (refuelData == null)
            refuelData = new HttpClient().getRefuelItem(id);
        if (refuelData == null) {
            Toast.makeText(this, getString(R.string.error_not_found), Toast.LENGTH_LONG);
            return;
        }


    }

    private void bindData() {
        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL) {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_refuel_preview);
            binding.setMItem(refuelData);
        } else {
            extractBinding = DataBindingUtil.setContentView(this, R.layout.preview_extract);
            extractBinding.setMItem(refuelData);

        }
        List<AirlineModel> airlines = (new HttpClient()).getAirlines();
        ArrayAdapter<AirlineModel> spinnerAdapter = new ArrayAdapter<AirlineModel>(this, R.layout.support_simple_spinner_dropdown_item, airlines);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

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
                    break;
                }
            }
        }


        if (refuelData.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.REFUEL) {
            ArrayList<RefuelItemData> list = new ArrayList<RefuelItemData>();
            list.add(refuelData);
            list.addAll(refuelData.getOthers());
            truckArrayAdapter = new TruckArrayAdapter(this, list);
            ((ListView) findViewById(R.id.refuel_preview_truck_list)).setAdapter(truckArrayAdapter);
            ((ListView) findViewById(R.id.refuel_preview_truck_list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            TableRow header = findViewById(R.id.rowHeader);
        }

    }

    ActivityRefuelPreviewBinding binding;
    PreviewExtractBinding extractBinding;
    TruckArrayAdapter truckArrayAdapter;
    RefuelItemData refuelData;
    int result = Activity.RESULT_OK;

    private void printSingle() {

        if (!new PrintWorker(this).printItem(refuelData)) {
            refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
        }


//        setResult(RESULT_OK);
//        finish();
    }

    private void preview(boolean single) {
        InvoiceModel model = InvoiceModel.fromRefuel(refuelData, single);
        Dialog dialog = new Dialog(this);
        InvoicePreviewBinding binding = DataBindingUtil.inflate(dialog.getLayoutInflater(), R.layout.invoice_preview, null, false);
        binding.setInvoiceItem(model);
        dialog.setContentView(binding.getRoot());

        InvoiceItemAdapter itemAdapter = new InvoiceItemAdapter(this, model.getItems());
        ((ListView) dialog.findViewById(R.id.invoice_preview_item_list)).setAdapter(itemAdapter);
        dialog.findViewById(R.id.btnPrintConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printInvoice();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btnPrintCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();
    }

    private void printInvoice() {
        if (!new PrintWorker(this).printItem(refuelData, true)) {
            refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
        }


//        setResult(RESULT_OK);
//        finish();

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {

            case R.id.refuel_preview_print_order:
                preview(true);
                break;
            case R.id.refuel_preview_print_invoice:
                preview(false);
                break;
            case R.id.refuel_preview_new_item:
                createNewItem();
                break;
            case R.id.btnUpdate:
                save();
                break;
            case R.id.refuel_preview_aircraftCode:
                m_Title = getString(R.string.update_aircraftCode);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case R.id.refuel_preview_realAmount:
                m_Title = getString(R.string.update_real_amount);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
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
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case R.id.refuel_preview_parking:
                m_Title = getString(R.string.update_parking_lot);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case R.id.refuel_preview_vat:
                openVatSpinner();
                break;
            case R.id.refuel_preview_airline:
                openAirlineSpinner();
                break;
            case R.id.btnBack:
                finish();
                break;
        }

    }

    private void updateBinding() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new HttpClient().postRefuel(refuelData);
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

    private void openVatSpinner() {

        Spinner vatSpinner = findViewById(R.id.refuel_preview_vat_spinner);

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
                refuelData.setProductName(selected.getProductName());
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

    private void showEditDialog(final int id, int inputType) {


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

                doUpdateResult();

            }

            private void doUpdateResult() {
                m_Text = input.getText().toString();
                switch (id) {
                    case R.id.refuel_preview_aircraftCode:
                        refuelData.setAircraftCode(m_Text);
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

        builder.show();

    }

    private void toggleEdit() {
        if (!isEditing) {
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
        }
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
        toggleEdit();
//        ActivityRefuelPreviewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_refuel_preview);
//        binding.setMItem(refuelData);

    }

    private boolean isEditing = false;

    private void createNewItem() {
        RefuelItemData itemData = null;
        try {
            itemData = (RefuelItemData) refuelData.clone();
            itemData.setId(0);
            itemData.setTruckId(currentApp.getSetting().getTruckId());
            itemData.setTruckNo(currentApp.getTruckNo());
            itemData.setStatus(REFUEL_ITEM_STATUS.NONE);
            itemData.setStartNumber(0);
            itemData.setRealAmount(0);
            itemData.setEndNumber(0);

        } catch (CloneNotSupportedException ex) {

        }
        if (itemData != null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String data = gson.toJson(itemData);


            Intent intent = new Intent(this, RefuelDetailActivity.class);
            intent.putExtra("REFUEL", data);
            startActivity(intent);
            finish();
        }
    }

}
