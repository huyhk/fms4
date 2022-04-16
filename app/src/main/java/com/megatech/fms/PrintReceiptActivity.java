package com.megatech.fms;

import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.megatech.fms.databinding.ActivityInvoiceBinding;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.helpers.Logger;
import com.megatech.fms.helpers.PrintWorker;
import com.megatech.fms.helpers.ZebraWorker;
import com.megatech.fms.model.ReceiptModel;
import com.megatech.fms.view.ReceiptItemAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrintReceiptActivity extends UserBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);


        loaddata();
        printWorker = new PrintWorker();
        printWorker.setPrintStateListener(new PrintWorker.PrintStateListener() {
            @Override
            public void onConnectionError() {

                runOnUiThread(() -> {
                    showErrorMessage(R.string.printer_connection_error);
                });
            }

            @Override
            public void onError() {

            }

            @Override
            public void onSuccess() {
                model.setPrinted(true);
                binding.invalidateAll();
            }
        });

        zebra = new ZebraWorker(this);
        zebra.setStateListener(new ZebraWorker.ZebraStateListener() {
            @Override
            public void onConnectionError() {
                closeProgressDialog();
                showErrorMessage(R.string.printer_connection_error);
            }

            @Override
            public void onError() {
                closeProgressDialog();
                showErrorMessage(R.string.printer_error);
            }

            @Override
            public void onSuccess() {
                model.setPrinted(true);
                binding.invalidateAll();
                closeProgressDialog();
            }
        });
        if (BuildConfig.FHS)
        {
            findViewById(R.id.btnCapture).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.btnSign).setVisibility(View.GONE);
            findViewById(R.id.btnSellerSign).setVisibility(View.GONE);
        }

    }

    private void loaddata() {

        setProgressDialog();
        Bundle b = getIntent().getExtras();
        String data = b.getString("RECEIPT");
        model = ReceiptModel.fromJson(data);
        bindData();


    }

    ReceiptModel model = null;
    ActivityInvoiceBinding binding;

    private void bindData() {
        if (model != null) {
            binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_invoice, null, false);
            binding.setInvoiceItem(model);
            setContentView(binding.getRoot());

            ListView lv = findViewById(R.id.invoice_item_list);
            lv.setAdapter(new ReceiptItemAdapter(this, model.getItems()));

        }
        closeProgressDialog();
    }

    private  void exit()
    {
        if ( model.isCaptured() || model.isPrinted())
        {
            showConfirmMessage(R.string.receipt_not_saved, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    finish();
                    return null;
                }
            });
        }
        else
            finish();
    }
    private final int SELLER_SIGNATURE = 445;
    private final int BUYER_SIGNATURE = 446;
    private void openSignature(boolean buyer)
    {
        Intent intent = new Intent(this, ReceiptSignActivity.class);
        startActivityForResult(intent, buyer? BUYER_SIGNATURE: SELLER_SIGNATURE);

    }
    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        switch (id) {
            case R.id.btnBack:
                exit();
                break;
            case R.id.btnCapture:
                openCapture();
                break;
            case R.id.btnSave:
                openSave();
                break;
            case R.id.btnPrint:
                print();
                break;
            case R.id.btnSign:
            case R.id.btnSellerSign:
                openSignature(id == R.id.btnSign);
                break;

            case R.id.receipt_defueling_number:
                if (model.getReturnAmount()>0)
                {
                    m_Title = getString(R.string.update_defueling_no);
                    showEditDialog(id, InputType.TYPE_CLASS_TEXT, ".*", false);
                }
                break;
            case R.id.receipt_split_check:
                if (model.isInvoiceSplit()) {
                    m_Title = getString(R.string.update_split_amount);
                    showEditDialog(id, InputType.TYPE_NUMBER_FLAG_DECIMAL, ".*", true);
                }
                else {
                    model.setSplitAmount(0);
                    binding.invalidateAll();
                }
                break;
        }
    }

    PrintWorker printWorker = null;

    ZebraWorker zebra = null;
    private void print() {
        if (BuildConfig.FHS)
        {
            if (zebra == null) {
                zebra = new ZebraWorker(this);
                zebra.setStateListener(new ZebraWorker.ZebraStateListener() {
                    @Override
                    public void onConnectionError() {
                        showErrorMessage(R.string.printer_error);
                    }

                    @Override
                    public void onError() {

                    }

                    @Override
                    public void onSuccess() {
                        model.setPrinted(true);
                        binding.invalidateAll();
                    }
                });

            }
            setProgressDialog();
            zebra.printReceipt(model);
        }
        else {
            if (printWorker == null) {
                printWorker = new PrintWorker();

            }
            if (model.isReturn())
                printWorker.printReturn(model);
            else
                printWorker.printReceipt(model);
        }
    }

    private int REQUEST_IMAGE_CAPTURE = 1;

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name

        String imageFileName = "JPEG_" + model.getNumber() + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openCapture() {
        Logger.appendLog("RECEIPT_WINDOW", "Capture printed receipt");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID +".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    boolean exitAfterCapture = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Logger.appendLog("RECEIPT_WINDOW", "Capture completed");
            int targetW = 500;
            int targetH = 600;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(1, Math.min(targetW/photoW, targetH/photoH));

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = 3;//scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            //Bitmap pdfBitmap = Bitmap.createScaledBitmap(bitmap,600,500,true);
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(currentPhotoPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //model.setPdfImageString(ImageUtil.convert(bitmap));
            model.setPdfPath(currentPhotoPath);
            model.setCaptured(true);
            binding.invalidateAll();
            if (exitAfterCapture)
                save();
        }

        if (requestCode == BUYER_SIGNATURE && resultCode == RESULT_OK)
        {
            String file = data.getExtras().getString("signature_file");
            model.setSignaturePath(file);
            binding.invalidateAll();
            ((Button)findViewById(R.id.btnSign)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
        }
        if (requestCode == SELLER_SIGNATURE && resultCode == RESULT_OK)
        {
            String file = data.getExtras().getString("signature_file");
            model.setSellerSignaturePath(file);
            binding.invalidateAll();
            ((Button)findViewById(R.id.btnSellerSign)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
        }
    }
    String m_Title,m_Text;
    private void showEditDialog(final int id, int inputType, String pattern, boolean required) {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(m_Title);
        final EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setTypeface(Typeface.DEFAULT);

        input.setText(((TextView) findViewById(id)).getText());


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

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doUpdateResult())
                    dialog.dismiss();
            }

            private boolean doUpdateResult() {

                Locale locale = Locale.getDefault();
                NumberFormat numberFormat = NumberFormat.getInstance(locale);

                m_Text = input.getText().toString().trim();
                if (required && m_Text.isEmpty()) {
                    showErrorMessage(R.string.empty_required_field);
                    return false;
                }
                Pattern regex = Pattern.compile(pattern);
                Matcher matcher = regex.matcher(m_Text);
                if (!matcher.find()) {
                    Toast.makeText(getBaseContext(), getString(R.string.invalid_data), Toast.LENGTH_LONG).show();
                    return false;
                }
                try {
                    switch (id) {

                        case R.id.receipt_split_check:
                            double d = numberFormat.parse(m_Text).doubleValue();
                            if (d > model.getWeight()) {
                                showErrorMessage(R.string.error_split_amount_too_large);
                                return false;
                            }
                            model.setSplitAmount(d);
                            binding.invalidateAll();
                            break;

                        case R.id.receipt_defueling_number:
                            model.setDefuelingNo(m_Text);
                            binding.invalidateAll();
                            break;
                    }
                } catch (ParseException ex) {
                    Toast.makeText(getBaseContext(), R.string.invalid_number_format, Toast.LENGTH_LONG).show();
                    return false;
                }


                return true;
            }
        });
    }

    private void openSave()
    {
        if (!model.isCaptured() && !BuildConfig.FHS)
        {
            showConfirmMessage(R.string.not_capture_confirm, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    exitAfterCapture = true;
                    openCapture();
                    return null;
                }
            }, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    save();
                    return null;
                }
            });
        }
        else
            showConfirmMessage(R.string.e_invoice_confirm, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    save();
                    return null;
                }
            });

    }

    private void save() {
        Logger.appendLog("RECEIPT_WINDOW", "save receipt " + model.getNumber());
        setProgressDialog();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                DataHelper.postReceipt(model);
                return null;
            }

            @Override
            protected void onPostExecute(Void response) {
                postCompleted();
                super.onPostExecute(response);
            }
        }.execute();


    }

    private void postCompleted()
    {
        closeProgressDialog();
        Logger.appendLog("RECEIPT_WINDOW", "save receipt completed " + model.getNumber());
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",model.getNumber());
        setResult(Activity.RESULT_OK,returnIntent);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.info)
        .setMessage(R.string.save_receipt_completed)
        .setCancelable(false)
        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        builder.create().show();
    }
}
