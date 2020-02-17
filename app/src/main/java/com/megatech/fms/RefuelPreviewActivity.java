package com.megatech.fms;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.databinding.ActivityRefuelPreviewBinding;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.PrintWorker;
import com.megatech.fms.view.TruckArrayAdapter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RefuelPreviewActivity extends UserBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refuel_preview);
        setToolbar();
        loadData();
        Button btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEdit();
            }
        });

    }

    private void loadData() {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String data = getIntent().getExtras().getString("REFUEL");
        refuelData = gson.fromJson(data, RefuelItemData.class);
        ActivityRefuelPreviewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_refuel_preview);

        binding.setMItem(refuelData);

        List<AirlineModel> airlines = (new HttpClient()).getAirlines();
        ArrayAdapter<AirlineModel> spinnerAdapter = new ArrayAdapter<AirlineModel>(this,R.layout.support_simple_spinner_dropdown_item,airlines);
        //spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        Spinner airline_spinner = (Spinner) findViewById(R.id.refuel_preview_airline_spinner);
        airline_spinner.setAdapter(spinnerAdapter);

        if (refuelData.getAirlineId()>0) {
            for (int i=0;i<airline_spinner.getCount();i++)
            {
                AirlineModel item = (AirlineModel)  airline_spinner.getItemAtPosition(i);
                if (refuelData.getAirlineId() == item.getId())
                {
                    airline_spinner.setSelection(i);
                    ((TextView) findViewById(R.id.refuel_preview_airline)).setText(item.getName());
                    refuelData.setAirlineId(item.getId());
                    refuelData.setProductName(item.getProductName());
                    refuelData.setAirlineModel(item);
                    break;
                }
            }
        }
        airline_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AirlineModel selected = (AirlineModel) parent.getItemAtPosition(position);
                refuelData.setAirlineId(selected.getId());
                refuelData.setPrice(selected.getPrice());
                refuelData.setProductName(selected.getProductName());
                refuelData.setAirlineModel(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<RefuelItemData> list = new ArrayList<RefuelItemData>();
        list.add(refuelData);
        list.addAll(refuelData.getOthers());
        TruckArrayAdapter truckArrayAdapter = new TruckArrayAdapter(this, list);
        ((ListView) findViewById(R.id.refuel_preview_truck_list)).setAdapter( truckArrayAdapter);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
/*
            ((TextView) findViewById(R.id.lblFlightNo)).setText(refuelData.getFlightCode());
            ((TextView) findViewById(R.id.lblAircraftNo)).setText(refuelData.getAircraftCode());
            ((TextView) findViewById(R.id.lblParkingLot)).setText(refuelData.getParkingLot());
            ((TextView) findViewById(R.id.lblAmount)).setText(String.format("%.2f", refuelData.getRealAmount()));

            ((TextView) findViewById(R.id.lblTemperature)).setText(String.format("%.2f", refuelData.getTemperature()));
            ((TextView) findViewById(R.id.lblDensity)).setText(String.format("%.2f", refuelData.getDensity()));
            ((TextView) findViewById(R.id.lblStartTime)).setText(format.format(refuelData.getStartTime()));
            ((TextView) findViewById(R.id.lblEndTime)).setText(format.format(refuelData.getEndTime()));
*/

        } catch (Exception e) {
        }
    }

    RefuelItemData refuelData;
    int result = Activity.RESULT_OK;
    private void saveAndPrint()
    {

        if (!new PrintWorker(this).printItem(refuelData))
        {
            refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
        }


        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id){

            case R.id.edit_d:
                m_Text = String.format("%.2f",refuelData.getDensity());
                m_Title = getString(R.string.update_density);
                showEditDialog(id,InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;

            case R.id.refuel_preview_print_order:
                saveAndPrint();
                break;
            case R.id.refuel_preview_print_invoice:
                printInvoice();
                break;
            case R.id.btnSave:
                save();
                break;
        }

    }

    private void printInvoice() {
        if (!new PrintWorker(this).printItem(refuelData,true))
        {
            refuelData.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
        }


        setResult(RESULT_OK);
        finish();

    }

    private String m_Text ="";
    private String m_Title ="";

    private void showEditDialog(final int id, int inputType)
    {
        final Dialog dlg = new Dialog(this);
        dlg.setContentView(R.layout.edit_refuel_dialog);
        dlg.setTitle(getString(R.string.dialog_update_refuel));
        ((TextView)dlg.findViewById(R.id.dialog_aircraft_no)).setText(refuelData.getAircraftCode());
        ((TextView)dlg.findViewById(R.id.dialog_density)).setText(String.format("%.2f",refuelData.getDensity()));
        ((TextView)dlg.findViewById(R.id.dialog_temp)).setText(String.format("%.2f",refuelData.getTemperature()));

        Button btnclose = (Button) dlg.findViewById(R.id.dialog_btn_close);
        // if button is clicked, close the custom dialog
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        Button btnsave = (Button) dlg.findViewById(R.id.dialog_btn_save);
        // if button is clicked, close the custom dialog
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refuelData.setAircraftCode(((TextView)dlg.findViewById(R.id.dialog_aircraft_no)).getText().toString());
                ((TextView)findViewById(R.id.lblAircraftNo)).setText(refuelData.getAircraftCode());
                refuelData.setDensity(Double.parseDouble(((TextView)dlg.findViewById(R.id.dialog_density)).getText().toString()));
                ((TextView)findViewById(R.id.lblDensity)).setText(String.format("%.2f",refuelData.getDensity()));
                refuelData.setManualTemperature(Double.parseDouble(((TextView)dlg.findViewById(R.id.dialog_temp)).getText().toString()));
                ((TextView)findViewById(R.id.lblTemperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));

                dlg.dismiss();
            }
        });
        dlg.show();
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(m_Title);
        final EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setText(m_Text);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                m_Text = input.getText().toString();
                switch (id){
                    case R.id.edit_a:
                        refuelData.setAircraftCode(m_Text);
                        ((TextView)findViewById(R.id.lblAircraftNo)).setText(refuelData.getAircraftCode());
                        break;
                    case R.id.edit_d:
                        refuelData.setDensity(Double.parseDouble(m_Text));
                        ((TextView)findViewById(R.id.lblDensity)).setText(String.format("%.2f",refuelData.getDensity()));
                        break;
                    case R.id.edit_t:
                        refuelData.setManualTemparature(Double.parseDouble(m_Text));
                        ((TextView)findViewById(R.id.lblTemperature)).setText(String.format("%.2f",refuelData.getManualTemparature()));

                        break;
                }


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
        */
    }

    private void toggleEdit() {
        if (!isEditing) {
            ((EditText) findViewById(R.id.edtAircraft)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.edtTemperature)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.edtDensity)).setVisibility(View.VISIBLE);
            ((Spinner) findViewById(R.id.refuel_preview_airline_spinner)).setVisibility(View.VISIBLE);
            ((Spinner) findViewById(R.id.refuel_preview_vat_spinner)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.edtAircraft)).requestFocus();
            findViewById(R.id.refuel_preview_aircraftCode).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuel_preview_Temperature).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuel_preview_Density).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuel_preview_airline).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuel_preview_vat).setVisibility(View.INVISIBLE);
            ((Button)findViewById(R.id.btnEdit)).setText(getString(R.string.back));
            findViewById(R.id.btnSave).setVisibility(View.VISIBLE);
            isEditing = true;
        }
        else
        {
            ((EditText) findViewById(R.id.edtAircraft)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.edtTemperature)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.edtDensity)).setVisibility(View.INVISIBLE);
            ((Spinner) findViewById(R.id.refuel_preview_airline_spinner)).setVisibility(View.INVISIBLE);
            ((Spinner) findViewById(R.id.refuel_preview_vat_spinner)).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuel_preview_aircraftCode).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_Temperature).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_Density).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_airline).setVisibility(View.VISIBLE);
            findViewById(R.id.refuel_preview_vat).setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.btnEdit)).setText(getString(R.string.edit_refuel));
            findViewById(R.id.btnSave).setVisibility(View.INVISIBLE);
            isEditing = false;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
        }
    }

    private void save() {
        refuelData.setAircraftCode(((EditText)findViewById(R.id.edtAircraft)).getText().toString());
        ((TextView)findViewById(R.id.refuel_preview_aircraftCode)).setText(refuelData.getAircraftCode());
        refuelData.setManualTemperature(Float.parseFloat(((EditText)findViewById(R.id.edtTemperature)).getText().toString()));
        ((TextView) findViewById(R.id.refuel_preview_Temperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));
        refuelData.setDensity(Float.parseFloat(((EditText)findViewById(R.id.edtDensity)).getText().toString()));
        ((TextView) findViewById(R.id.refuel_preview_Density)).setText(String.format("%.2f",refuelData.getDensity()));

        Spinner airline_spinner = (Spinner) findViewById(R.id.refuel_preview_airline_spinner);
        AirlineModel item = (AirlineModel) airline_spinner.getSelectedItem();
        refuelData.setAirlineId(item.getId());
        ((TextView) findViewById(R.id.refuel_preview_airline)).setText(item.getCode());
        try {
            Spinner vatSpinner = (Spinner) findViewById(R.id.refuel_preview_vat_spinner);
            String vat = vatSpinner.getSelectedItem().toString();
            NumberFormat format = NumberFormat.getPercentInstance();
            refuelData.setTaxRate(format.parse(vat).doubleValue());
            ((TextView) findViewById(R.id.refuel_preview_vat)).setText(vat);

        }
        catch (Exception e){}
        new HttpClient().postRefuel(refuelData);
        toggleEdit();
    }


    private  boolean isEditing = false;
}
