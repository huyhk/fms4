package com.megatech.fms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.databinding.ActivityNewRefuelBinding;
import com.megatech.fms.helpers.DateUtils;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.UserInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NewRefuelActivity extends UserBaseActivity implements View.OnClickListener {

    private EditText arrival, departure;
    private int mYear, mMonth, mDay, mHour, mMinute;
    RefuelItemData refuelData;
    ActivityNewRefuelBinding binding;

    AirlineModel oldSelected;
    ArrayAdapter<AirlineModel> spinnerAdapter;
    Spinner airline_spinner;
    int prev, current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_refuel);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        arrival = findViewById(R.id.new_refuel_arrival);
        departure = findViewById(R.id.new_refuel_departure);

        Bundle b = getIntent().getExtras();
        Boolean isExtract = b.getBoolean("EXTRACT", false);

        refuelData = new RefuelItemData();
        refuelData.setTruckId(currentApp.getSetting().getTruckId());
        refuelData.setTruckNo(currentApp.getTruckNo());
        refuelData.setRefuelItemType(isExtract ? RefuelItemData.REFUEL_ITEM_TYPE.EXTRACT : RefuelItemData.REFUEL_ITEM_TYPE.REFUEL);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_refuel);
        binding.setMItem(refuelData);

        List<AirlineModel> airlines = (new HttpClient()).getAirlines();
        spinnerAdapter = new ArrayAdapter<AirlineModel>(this, android.R.layout.simple_spinner_dropdown_item, airlines);
        if ((currentUser.getPermission() & UserInfo.USER_PERMISSION.CREATE_CUSTOMER.getValue()) > 0) {
            AirlineModel newModel = new AirlineModel();
            newModel.setCode("");
            newModel.setName(getString(R.string.new_customer));
            newModel.setId(0);
            spinnerAdapter.add(newModel);
        }
        spinnerAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        airline_spinner = findViewById(R.id.new_refuel_airline_spinner);
        airline_spinner.setAdapter(spinnerAdapter);
        airline_spinner.setSelection(0);

        airline_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prev = current;
                current = position;

                AirlineModel selected = (AirlineModel) parent.getItemAtPosition(position);
                if (selected.getId() == 0)
                    new_customer();
                else {
                    refuelData.setAirlineId(selected.getId());
                    refuelData.setPrice(selected.getPrice());
                    refuelData.setProductName(selected.getProductName());
                    refuelData.setAirlineModel(selected);

                    updateBinding();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    Button new_customer_save;
    EditText new_customer_code;
    EditText new_customer_name;
    EditText new_customer_address;
    EditText new_customer_taxcode;
    EditText new_customer_price;

    private void new_customer() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.new_customer_dialog, null);
        AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.getWindow().setLayout(600, 400);
        dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                airline_spinner.setSelection(prev);
            }
        });

        new_customer_code = layout.findViewById(R.id.new_customer_airline_code);
        new_customer_address = layout.findViewById(R.id.new_customer_airline_address);
        new_customer_name = layout.findViewById(R.id.new_customer_airline_name);
        new_customer_taxcode = layout.findViewById(R.id.new_customer_airline_taxcode);
        new_customer_price = layout.findViewById(R.id.new_customer_airline_price);
        new_customer_save = layout.findViewById(R.id.new_customer_btnsave);

        new_customer_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AirlineModel model = new AirlineModel();
                model.setCode(new_customer_code.getText().toString());
                model.setName(new_customer_name.getText().toString());
                model.setAddress(new_customer_address.getText().toString());
                model.setTaxCode(new_customer_taxcode.getText().toString());
                try {
                    model.setPrice(Double.parseDouble(new_customer_price.getText().toString()));
                } catch (NumberFormatException ex) {
                    model.setPrice(0);
                }

                if (model.getCode().isEmpty()) {
                    new_customer_code.requestFocus();
                    new_customer_code.setError(getString(R.string.error_empty_field));

                } else if (model.getName().isEmpty()) {
                    new_customer_name.requestFocus();
                    new_customer_name.setError(getString(R.string.error_empty_field));
                } else {

                    Runnable t = () -> {
                        HttpClient client = new HttpClient();
                        AirlineModel newModel = client.postAirline(model);
                        if (model != null) {
                            spinnerAdapter.insert(newModel, spinnerAdapter.getCount() - 1);
                            airline_spinner.setSelection(spinnerAdapter.getCount() - 2);
                            dlg.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_saving_data), Toast.LENGTH_LONG).show();
                        }


                    };
                    t.run();
                }

            }
        });
        dlg.setView(layout);
        dlg.setTitle(R.string.new_customer);
        dlg.setCancelable(true);

        dlg.show();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.btnSave) {
            v.setEnabled(false);
            save();
            v.setEnabled(true);
            return;
        }
        if (id == R.id.btnBack) {
            finish();
            return;
        }
        int inputType = ((EditText) v).getInputType();
        switch (id) {
            case R.id.new_refuel_arrival:
            case R.id.new_refuel_departure:
            case R.id.new_refuel_date:
                showTimeDialog(v);
                break;

            case R.id.new_refuel_aircraftCode:
                m_Title = getString(R.string.aircraft_code);
                showEditDialog(id, inputType);
                break;
            case R.id.new_refuel_aircraftType:
                m_Title = getString(R.string.aircraft_type);
                showEditDialog(id, inputType);
                break;
            case R.id.new_refuel_estimateAmount:
                m_Title = getString(R.string.estimate_amount);
                showEditDialog(id, inputType);
                break;
            case R.id.new_refuel_flightCode:
                m_Title = getString(R.string.flightCode);
                showEditDialog(id, inputType);
                break;
            case R.id.new_refuel_parking:
                m_Title = getString(R.string.parking_lot);
                showEditDialog(id, inputType);
                break;
            case R.id.new_refuel_routeName:
                m_Title = getString(R.string.route_name);
                showEditDialog(id, inputType);
                break;
        }

    }

    private void save() {
        HttpClient client = new HttpClient();
        RefuelItemData response = client.postRefuel(refuelData);
        if (response != null) {

            refuelData.setId(response.getId());
            if (refuelData != null) {
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

                String data = gson.toJson(refuelData);

                Intent intent = new Intent(this, RefuelDetailActivity.class);
                intent.putExtra("REFUEL", data);
                startActivity(intent);
                finish();
            }
        } else
            Toast.makeText(this, getString(R.string.error_saving_data), Toast.LENGTH_SHORT).show();
    }


    private void showDateDialog(View v) {
        String time = ((EditText) v).getText().toString();
        Date date = DateUtils.setDate(time);
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int month, int dayOfMonth) {
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        refuelData.setRefuelTime(c.getTime());
                        updateBinding();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void showTimeDialog(View v) {
        String time = ((EditText) v).getText().toString();
        Date date = DateUtils.setTime(time);
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        newDate.set(Calendar.MINUTE, minute);
                        if (v.getId() == R.id.new_refuel_arrival)
                            refuelData.setArrivalTime(newDate.getTime());
                        else if (v.getId() == R.id.new_refuel_departure)
                            refuelData.setDepartureTime(newDate.getTime());
                        else
                            refuelData.setRefuelTime(newDate.getTime());
                        updateBinding();
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private void updateBinding() {

        binding.invalidateAll();


    }

    private String m_Title, m_Text;

    private void showEditDialog(final int id, int inputType) {
        final EditText txt = findViewById(id);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(m_Title);
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        input.setInputType(inputType);

        input.setTypeface(Typeface.DEFAULT);
        input.setText(txt.getText());
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
                m_Text = input.getText().toString();
                txt.setText(m_Text);
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
}
