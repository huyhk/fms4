package com.megatech.fms.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.megatech.fms.model.ReceiptModel;

import java.io.File;
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
            Logger.appendLog("ReceiptAPI", "Post Receipt: " + model.getNumber());
            getPdfString(model);
            String parm = gson.toJson(model);
            HttpClient.HttpResponse response = httpClient.sendPOST(url, parm);
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
                return gson.fromJson(response.getData(), ReceiptModel.class);
        }
        catch (Exception ex)
        {
            Logger.appendLog("ReceiptAPI", ex.getMessage());
            //Log.e("ReceiptAPI: post(ReceiptModel)", e.getMessage());
        }
        return null;
    }

    private void getPdfString(ReceiptModel model)
    {
        try {
            if (model.getPdfPath() !=null && !model.getPdfPath().isEmpty()) {
                File f = new File(model.getPdfPath());
                if (f.exists()) {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(model.getPdfPath(), bmOptions);
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = 2;//scaleFactor;

                    Bitmap bitmap = BitmapFactory.decodeFile(model.getPdfPath(), bmOptions);
                    model.setPdfImageString(ImageUtil.convert(bitmap));
                }
            }
            if (model.getSignaturePath() !=null && !model.getSignaturePath().isEmpty()) {
                File f = new File(model.getSignaturePath());
                if (f.exists()) {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(model.getSignaturePath(), bmOptions);
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = 1;//scaleFactor;

                    Bitmap bitmap = BitmapFactory.decodeFile(model.getSignaturePath(), bmOptions);
                    model.setSignImageString(ImageUtil.convert(bitmap));
                }
            }

            if (model.getSellerSignaturePath() !=null && !model.getSellerSignaturePath().isEmpty()) {
                File f = new File(model.getSellerSignaturePath());
                if (f.exists()) {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(model.getSellerSignaturePath(), bmOptions);
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = 1;//scaleFactor;

                    Bitmap bitmap = BitmapFactory.decodeFile(model.getSellerSignaturePath(), bmOptions);
                    model.setSellerImageString(ImageUtil.convert(bitmap));
                }
            }
        }
        catch (Exception ex)
        {
            Logger.appendLog("ReceiptAPI", ex.getMessage());
        }
    }
}
