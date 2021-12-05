package com.megatech.fms.helpers;

import android.util.Log;

import com.megatech.fms.model.InvoiceModel;
import com.megatech.fms.model.ReceiptModel;

import java.net.HttpURLConnection;

public class ReceiptAPI extends  BaseAPI{

    public ReceiptAPI()
    {
        url = BASE_URL + "/api/receipts";
    }

    public ReceiptModel post(ReceiptModel model)
    {
        try {
            //Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            String parm = gson.toJson(model);
            HttpClient.HttpResponse response = httpClient.sendPOST(url, parm);
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
                return gson.fromJson(response.getData(), ReceiptModel.class);
        }
        catch (Exception e)
        {
            Log.e("ReceiptAPI: post(ReceiptModel)", e.getMessage());
        }
        return null;
    }
}
