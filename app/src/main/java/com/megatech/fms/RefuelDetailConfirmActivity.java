package com.megatech.fms;


import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.megatech.fms.databinding.ActivityRefuelDetailConfirmBinding;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.helpers.LCRWorker;
import com.megatech.fms.helpers.Logger;
import com.megatech.fms.model.RefuelItemData;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RefuelDetailConfirmActivity extends UserBaseActivity implements View.OnClickListener {
    private LCRWorker lcrWorker ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refuel_detail_confirm);


        lcrWorker = new LCRWorker(FMSApplication.getApplication().getDeviceIP());
        lcrWorker.setLcrWorkerListener(new LCRWorker.LCRWorkerListener() {


            @Override
            public void onConnected() {
                lcrWorker.requestField(LCRWorker.LCR_FIELD.PREV_METER);
                lcrWorker.requestField(LCRWorker.LCR_FIELD.GROSS_QTY);
                lcrWorker.requestField(LCRWorker.LCR_FIELD.GROSS_METER);
            }

            @Override
            public void onSent() {

            }

            @Override
            public void onReceived(LCRWorker.LCR_FIELD field, Object data) {
                try {
                    double d = Double.parseDouble(data.toString());
                    if (d > 0) {
                        if (field == LCRWorker.LCR_FIELD.PREV_METER)
                            mItem.setStartNumber(d);
                        else if (field == LCRWorker.LCR_FIELD.GROSS_METER)
                            mItem.setEndNumber(d);
                        else if (field == LCRWorker.LCR_FIELD.GROSS_QTY)
                            mItem.setRealAmount(d);
                    }
                } catch (Exception ex) {
                }
            }

            @Override
            public void onCompleted() {

                //postData();
                runOnUiThread(() -> {
                    if (!isFinishing())
                        bindData();
                });

            }
        });

        loaddata();
    }

    private void loaddata() {
        setProgressDialog();
        new Thread(() -> {

            Bundle b = getIntent().getExtras();
            String mData = b.getString("REFUEL", "");

            RefuelItemData itemData = null;
            if (mData != null && !mData.equals("")) {
                itemData = RefuelItemData.fromJson(mData);
            }

            if (itemData != null ) {
                mItem = itemData;


                if (mItem!=null && (mItem.getQualityNo() == null || mItem.getQualityNo() .isEmpty()))
                    mItem.setQualityNo(currentApp.getQCNo());
                Logger.appendLog(LOG_TAG, "Start confirm Flight Code: " + mItem.getFlightCode());
                runOnUiThread(() -> {
                    if (!isFinishing())
                        bindData();
                });

                //lcrWorker.connect();
            }
            else {
                runOnUiThread(() -> {
                    showErrorMessage(R.string.error_data);
                    finish();
                });

            }

        }).start();

    }
    ActivityRefuelDetailConfirmBinding binding;
    private void bindData() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refuel_detail_confirm);
        if (binding !=null && mItem!=null)
            binding.setMItem(mItem);
        binding.invalidateAll();
        closeProgressDialog();
    }

    RefuelItemData mItem;
    private void showEditDialog(final int id, int inputType) {
        showEditDialog(id, inputType, ".*");
    }
    private void showEditDialog(final int id, int inputType, String pattern){
        TextView view = findViewById(id);
        if (view!=null)
            showEditDialog(view, inputType, pattern);
    }

    String m_Title, m_Text;
    private void showEditDialog(final TextView view, int inputType, String pattern) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(m_Title);
        final EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setTypeface(Typeface.DEFAULT);
        input.setText(view.getText());

        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL )>0)
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

        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        final AlertDialog dialog = builder.create();// builder.show();

        dialog.show();
        input.requestFocus();
        if (view.getId() == R.id.refuel_confirm_Density)
            input.setSelection(2,input.getText().length());
        else
            input.setSelection(0,input.getText().length());

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doUpdateResult())
                    dialog.dismiss();
            }

            private boolean doUpdateResult() {
                try {
                    m_Text = input.getText().toString();
                    Pattern regex = Pattern.compile(pattern);
                    Matcher matcher = regex.matcher(m_Text);
                    if (!matcher.find()) {
                        Toast.makeText(context, getString(R.string.invalid_data), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return updateDialogResult(view.getId(), m_Text);
                } catch (Exception ex) {
                    Toast.makeText(context,R.string.invalid_number_format, Toast.LENGTH_LONG).show();
                    return false;

                }

            }
        });
    }

    private boolean updateDialogResult(int id, String m_text) {
        try {
            switch (id) {

                case R.id.refuel_confirm_Density:
                    double d = numberFormat.parse(m_Text).doubleValue();
                    if (d < 0.72 || d > 0.86) {
                        this.showErrorMessage(R.string.error_data, R.string.invalid_density, R.drawable.ic_error);

                        return false;
                    }
                    mItem.setDensity(d);
                    break;
                case R.id.refuel_confirm_Temperature:
                    mItem.setManualTemperature(numberFormat.parse(m_Text).doubleValue());
                    break;
                case R.id.refuel_confirm_qc_no:
                    mItem.setQualityNo(m_Text);
                    break;
                case R.id.refuel_confirm_start_meter:
                    mItem.setStartNumber(numberFormat.parse(m_Text).doubleValue());
                    break;
                case R.id.refuel_confirm_end_meter:
                    mItem.setEndNumber(numberFormat.parse(m_Text).doubleValue());
                    break;
                case R.id.refuel_confirm_real_amount:
                    mItem.setRealAmount(numberFormat.parse(m_Text).doubleValue());
                    break;
                case R.id.refuel_confirm_return:
                    calculateReturnAmount(numberFormat.parse(m_Text).doubleValue());
                    break;
                case R.id.refuel_confirm_weight_note:
                    mItem.setWeightNote(m_Text);
                    break;
            }
            updateBinding();
        } catch (ParseException ex) {
            Toast.makeText(this, R.string.invalid_number_format, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    private void calculateReturnAmount(double returnAmount) {

        if (mItem.getDensity()>0) {
            double vol = Math.round(returnAmount / mItem.getDensity());
            double gal = Math.round(vol / RefuelItemData.GALLON_TO_LITTER);
            double newAmount = Math.round(Math.round(gal * RefuelItemData.GALLON_TO_LITTER) * mItem.getDensity());
            if (gal > mItem.getRealAmount()) {
                showWarningMessage(R.string.return_amount_greater_warning );
            } else if (newAmount != returnAmount) {
                showWarningMessage(getString(R.string.new_return_amount_value) + " " + newAmount + " KG");

            }

            mItem.setReturnAmount(newAmount);
        }
    }
    private void updateBinding() {
        if (binding!=null)
            binding.invalidateAll();
    }

    Locale locale = Locale.getDefault();
    NumberFormat numberFormat = NumberFormat.getInstance(locale);
    Context context= this;
    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        switch (id)
        {
            case R.id.refuel_confirm_real_amount:

                    m_Title = getString(R.string.update_real_amount);
                    showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                break;
            case R.id.refuel_confirm_Density:
                m_Title = getString(R.string.update_density);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.refuel_confirm_Temperature:
                m_Title = getString(R.string.update_temparature);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.refuel_confirm_qc_no:
                m_Title = getString(R.string.update_qc_no);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ".+");
                break;
            case R.id.refuel_confirm_return:
                if (mItem.getDensity()<=0)
                    showErrorMessage(R.string.density_must_input);
                else {
                    m_Title = getString(R.string.update_return_amount);
                    showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
                break;

            case R.id.refuel_confirm_weight_note:
                m_Title = getString(R.string.update_weight_note);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,"(\\d+)?");
                break;
            case R.id.refuel_confirm_start_meter:
                m_Title = getString(R.string.update_start_meter);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case R.id.refuel_confirm_end_meter:
                m_Title = getString(R.string.update_end_meter);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;

            case R.id.refuel_confirm_start_time:
            case R.id.refuel_confirm_end_time:
                showTimeDialog(id);
                break;

            case R.id.btnConfirm:
                save();
            default:
                break;
        }
    }

    private void save() {
        if (mItem.getDensity()< 0.72 ||  mItem.getDensity() >0.86)
            showErrorMessage(R.string.invalid_density);
        else if(mItem.getManualTemperature()<=0)
            showErrorMessage(R.string.invalid_temperature);
        else if(mItem.getRealAmount()<=0)
            showErrorMessage(R.string.invalid_real_amount);
        else if(mItem.getStartNumber()> mItem.getEndNumber() || mItem.getStartNumber() + mItem.getRealAmount() != mItem.getEndNumber() )
            showErrorMessage(R.string.invalid_start_end_meter);
        else if(mItem.getStartTime().after( mItem.getEndTime() ))
            showErrorMessage(R.string.invalid_start_end_time);
        else if (mItem.getQualityNo().trim().isEmpty())
        {
            showErrorMessage(R.string.invalid_qc_no);
        }
        else
            postData();
    }


    private int mHour, mMinute, mYear, mMonth, mDay;

    private void showTimeDialog(int id) {
        final Date date = new Date();
        if (id == R.id.refuel_confirm_start_time)
            date.setTime(mItem.getStartTime().getTime());
        else
            date.setTime(mItem.getEndTime().getTime());

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
                                updateTime(id,c);


                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void updateTime(int id, Calendar c) {

        if (id == R.id.refuel_confirm_start_time)
            mItem.setStartTime(c.getTime());
        else if (id == R.id.refuel_confirm_end_time)
            mItem.setEndTime(c.getTime());
        updateBinding();
    }

    private String LOG_TAG = "RFC";
    private void postData()
    {
        setProgressDialog();
        new AsyncTask<Void, Void, RefuelItemData>() {
            @Override
            protected RefuelItemData doInBackground(Void... voids) {
                Logger.appendLog(LOG_TAG, "Post item");
                mItem = DataHelper.postRefuel(mItem);
                return mItem;
            }

            @Override
            protected void onPostExecute(RefuelItemData itemData) {
                Logger.appendLog(LOG_TAG, "Post item completed");
                postRefuelCompleted(itemData);
                super.onPostExecute(itemData);

            }
        }.execute();
    }
    private void postRefuelCompleted(RefuelItemData mItem) {
        closeProgressDialog();

            if (mItem != null) {

                Intent intent = new Intent(this, RefuelPreviewActivity.class);
                intent.putExtra("REFUEL_ID", mItem.getId());
                intent.putExtra("REFUEL_LOCAL_ID", mItem.getLocalId());
                int confirm_OPEN = 1;
                startActivityForResult(intent, confirm_OPEN);
            }

            finish();

    }
}
