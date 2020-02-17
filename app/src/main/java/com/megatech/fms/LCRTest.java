package com.megatech.fms;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.liquidcontrols.lcr.iq.sdk.LcrSdk;
import com.liquidcontrols.lcr.iq.sdk.utils.AsyncCallback;

public class LCRTest extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcrtest);

        LcrSdk lcrSdk = LCRTest.getSdkInstance(this);
        lcrSdk.init(new AsyncCallback() {
            @Override
            public void onAsyncReturn(@Nullable Throwable throwable) {

            }
        });

        ((Button)findViewById(R.id.lcr_test_exit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private static LcrSdk _sdkinstance;
    public static LcrSdk getSdkInstance(Context ctx)
    {
        if (_sdkinstance == null)
            _sdkinstance = new LcrSdk(ctx);
        return  _sdkinstance;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
