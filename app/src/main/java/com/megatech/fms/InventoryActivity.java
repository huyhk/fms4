package com.megatech.fms;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InventoryActivity extends UserBaseActivity implements View.OnClickListener {

    Button btn;
    EditText edt;
    EditText edtQCNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        edt = findViewById(R.id.txtInventory);
        edtQCNo = findViewById(R.id.txtQualityControlNo);
        try {
            edt.setText(String.format("%.0f", currentApp.getCurrentAmount()));
            edtQCNo.setText(currentApp.getQCNo());
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Button btn = findViewById(R.id.btnUpdate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float amount = 0;
                try {
                    amount = Float.parseFloat(edt.getText().toString());
                } catch (NumberFormatException ex) {
                }
                currentApp.setInventory(amount, edtQCNo.getText().toString() );
                finish();
            }
        });
    }

    private void save() {
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                finish();
                break;
        }
    }
}
