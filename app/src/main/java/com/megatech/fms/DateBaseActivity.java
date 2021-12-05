package com.megatech.fms;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

import com.megatech.fms.data.entity.Flight;
import com.megatech.fms.model.UserModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class DateBaseActivity extends UserBaseActivity {
    private Context context = this;
    private int mHour, mMinute, mYear, mMonth, mDay;
    protected Date selectedDate = new Date();
    public List<UserModel> userList = null;

    protected void showDateDialog() {

        final Calendar c = Calendar.getInstance();
        c.setTime(selectedDate);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(year, month, dayOfMonth);

                selectedDate = c.getTime();
                loaddata();

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }


    public abstract void loaddata() ;
    public abstract void bindData();

}
