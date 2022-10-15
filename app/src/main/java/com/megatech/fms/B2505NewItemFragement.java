package com.megatech.fms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.megatech.fms.databinding.B2505NewBinding;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.BM2505Model;
import com.megatech.fms.model.FlightModel;
import com.megatech.fms.model.UserModel;
import com.megatech.fms.view.AirlineArrayAdapter;
import com.megatech.fms.view.FlightArrayAdapter;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class B2505NewItemFragement extends DialogFragment {

    public B2505NewItemFragement() {
        this.model = new BM2505Model();
        this.model.setTime(new Date());
        this.model.setOperatorId(FMSApplication.getApplication().getUser().getUserId());
        this.model.setTruckId(FMSApplication.getApplication().getTruckId());

    }

    public B2505NewItemFragement(BM2505Model model) {
        this.model = model;
    }

    public static B2505NewItemFragement newInstance(BM2505Model model) {

        B2505NewItemFragement frag = new B2505NewItemFragement(model);
        Bundle args = new Bundle();

        frag.setArguments(args);
        return frag;
    }

    Locale locale = Locale.getDefault();
    NumberFormat numberFormat = NumberFormat.getInstance(locale);
    Dialog dlg;
    BM2505Model model;
    private List<FlightModel> flights;
    private B2505Activity activity;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        dlg = super.onCreateDialog(savedInstanceState);
        return dlg;
    }

    public void findViews(View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    // recursively call this method
                    findViews(child);
                }
            } else if (v instanceof TextView) {

                v.setOnClickListener(this::onClick);
                //do whatever you want ...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    View rootView;
    B2505NewBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.b2505_new, container, false);
        binding.setMItem(this.model);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        activity = ((B2505Activity) getActivity());
        List<UserModel> userList = activity.userList;
        flights = activity.flightList;
        Spinner spn = view.findViewById(R.id.b2505_new_operator);

        ArrayAdapter<UserModel> spinnerAdapter = new ArrayAdapter<UserModel>(activity, R.layout.support_simple_spinner_dropdown_item, userList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spn.setAdapter(spinnerAdapter);
        if (model.getOperatorId() > 0) {
            for (int i = 0; i < userList.size(); i++)
                if (model.getOperatorId() == userList.get(i).getId()) {
                    spn.setSelection(i);
                    break;
                }
        }
        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                UserModel user = (UserModel) adapterView.getItemAtPosition(i);
                model.setOperatorId(user.getId());
                model.setOperatorName(user.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

       /* Spinner spnFlight = view.findViewById(R.id.b2505_new_flight_spinner);
        ArrayAdapter<FlightModel> flightAdapter = new ArrayAdapter<>(activity, R.layout.support_simple_spinner_dropdown_item, flights);
        flightAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spnFlight.setAdapter(flightAdapter);
        if (model.getFlightId() > 0) {
            for (int i = 0; i < flights.size(); i++)
                if (model.getFlightId() == flights.get(i).getId()) {
                    spnFlight.setSelection(i);
                    break;
                }
        }
        spnFlight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                FlightModel user = (FlightModel) adapterView.getItemAtPosition(i);
                model.setFlightId(user.getId());
                model.setFlightCode(user.getFlightCode());
                model.setAircraftCode(user.getAircraftCode());
                binding.invalidateAll();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    }


    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.b2505_new_back:
                dlg.dismiss();
                break;
            case R.id.b2505_new_save:
                save();
                break;
            case R.id.b2505_new_time:
                showTimeDialog();
                break;
            case R.id.b2505_new_flight:
                openFlightSelect();
                break;
            case R.id.b2505_new_tank_no:
                m_Title = getString(R.string.update_tank_no);
                showEditDialog(id, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case R.id.b2505_new_hose_pressure:
                m_Title = getString(R.string.update_hose_presure);
                showEditDialog(id, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case R.id.b2505_new_pressure_diff:
                m_Title = getString(R.string.update_pressure_diff);
                showEditDialog(id, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case R.id.b2505_new_appearance_cb:
                dlg.findViewById(R.id.b2505_new_appearance).setVisibility(View.GONE);
                model.setAppearanceCheck("C&B");
                binding.invalidateAll();
                break;
            case R.id.b2505_new_appearance_other:
            case R.id.b2505_new_appearance:
                dlg.findViewById(R.id.b2505_new_appearance).setVisibility(View.VISIBLE);
                m_Title = getString(R.string.update_appearance_check);
                showEditDialog(id, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case R.id.b2505_new_rtc_no:
                m_Title = getString(R.string.update_rtc_no);
                showEditDialog(id, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case R.id.b2505_new_temperature:
                m_Title = getString(R.string.update_temparature);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.b2505_new_density:
                m_Title = getString(R.string.update_density);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.b2505_new_density15:
                m_Title = getString(R.string.update_density15);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
        }
    }

    private void save() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                DataHelper.postBM2505(model);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                activity.loaddata();
                dlg.dismiss();
            }


        }.execute();


    }

    private void openFlightSelect() {
        Dialog flightDlg = new Dialog(getActivity());
        flightDlg.setTitle(R.string.app_name);
        flightDlg.setContentView(R.layout.flight_select_dialog);
        SearchView searchView = flightDlg.findViewById(R.id.flight_dlg_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ListView lvAirline = flightDlg.findViewById(R.id.list_airline);
                FlightArrayAdapter adapter = (FlightArrayAdapter) lvAirline.getAdapter();
                adapter.getFilter().filter(query);
                adapter.notifyDataSetChanged();
                //lvAirline.setAdapter(adapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ListView lvAirline = flightDlg.findViewById(R.id.list_airline);
                FlightArrayAdapter adapter = (FlightArrayAdapter) lvAirline.getAdapter();
                adapter.getFilter().filter(newText);
                adapter.notifyDataSetChanged();

                return false;
            }
        });
        ListView lvAirline = flightDlg.findViewById(R.id.list_airline);
        lvAirline.setAdapter(new FlightArrayAdapter(getActivity(), flights));
        lvAirline.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FlightModel flightModel = (FlightModel) parent.getItemAtPosition(position);
                model.setFlightCode(flightModel.getFlightCode());
                model.setAircraftCode(flightModel.getAircraftCode());
                model.setFlightId(flightModel.getId());
                FlightArrayAdapter adapter = (FlightArrayAdapter) lvAirline.getAdapter();
                adapter.getFilter().filter("");
                flightDlg.dismiss();

                binding.invalidateAll();
            }

        });
        flightDlg.show();
    }

    private void showTimeDialog() {

        final Calendar c = Calendar.getInstance();
        c.setTime(model.getTime());
        TimePickerDialog datePickerDialog = new TimePickerDialog(this.getActivity(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                c.set(Calendar.MINUTE, timePicker.getMinute());
                model.setTime(c.getTime());
                binding.invalidateAll();
            }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        datePickerDialog.show();
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


        Context context = this.getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(m_Title);
        final EditText input = new EditText(context);
        input.setInputType(inputType);
        input.setTypeface(Typeface.DEFAULT);

        input.setText(((TextView) dlg.findViewById(id)).getText());


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
        if (id == R.id.b2505_new_density || id==R.id.b2505_new_density15)
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
                    return false;
                }
                Pattern regex = Pattern.compile(pattern);
                Matcher matcher = regex.matcher(m_Text);
                if (!matcher.find()) {
                    return false;
                }
                try {
                    double d;
                    switch (id) {

                        case R.id.b2505_new_tank_no:
                            model.setTankNo(m_Text);
                            break;
                        case R.id.b2505_new_rtc_no:
                            model.setRTCNo(m_Text);
                            break;
                        case R.id.b2505_new_temperature:
                            d = numberFormat.parse(m_Text).doubleValue();
                            model.setTemperature(d);
                            break;
                        case R.id.b2505_new_density:
                            d = numberFormat.parse(m_Text).doubleValue();
                            model.setDensity(d);
                            break;
                        case R.id.b2505_new_density15:
                            d = numberFormat.parse(m_Text).doubleValue();
                            model.setDensity15(d);
                            break;
                        case R.id.b2505_new_appearance_other:
                        case R.id.b2505_new_appearance:
                            model.setAppearanceCheck(m_Text);
                            break;
                        case R.id.b2505_new_pressure_diff:
                            model.setPressureDiff(m_Text);
                            break;
                        case R.id.b2505_new_hose_pressure:
                            model.setHosePressure(m_Text);
                            break;

                    }
                } catch (NumberFormatException ex) {
                    activity.showErrorMessage(R.string.invalid_number_format);
                    return false;
                } catch (Exception ex) {
                    return false;
                }
                binding.invalidateAll();
                return true;
            }
        });
    }
}
