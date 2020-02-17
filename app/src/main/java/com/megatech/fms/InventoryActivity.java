package com.megatech.fms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class InventoryActivity extends UserBaseActivity {

    Button btn;
    EditText edt;
    EditText edtQCNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        setToolbar();
        edt = findViewById(R.id.txtInventory);
        edtQCNo = findViewById(R.id.txtQualityControlNo);
        try {
            edt.setText(String.format("%.2f", currentApp.getCurrentAmount()));
            edtQCNo.setText(currentApp.getQCNo());
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Button btn = findViewById(R.id.btnSave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float amount = Float.parseFloat(edt.getText().toString());
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
}
