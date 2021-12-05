package com.megatech.fms;

import static com.megatech.fms.model.RefuelItemData.GALLON_TO_LITTER;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.megatech.fms.databinding.ActivityInvoiceBinding;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.helpers.ImageUtil;
import com.megatech.fms.helpers.PrintWorker;
import com.megatech.fms.model.ReceiptItemModel;
import com.megatech.fms.model.ReceiptModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.view.ReceiptItemAdapter;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvoiceActivity extends UserBaseActivity implements View.OnClickListener {

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
                    showErrorMessage(R.string.printer_error);
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
        if ( model.isCaptured())
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnBack:
                exit();
                break;
            case R.id.btnCapture:
                openCapture();
                break;
            case R.id.btnSave:
                save();
                break;
            case R.id.btnPrint:
                print();
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
        }
    }

    PrintWorker printWorker = null;

    private void print() {
        if (printWorker == null) {
            printWorker = new PrintWorker();

        }
        printWorker.printReceipt(model);
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
                        "com.megatech.fms.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");


            //imageView.setImageBitmap(imageBitmap);
            //model.setPdfImageString(ImageUtil.convert(imageBitmap));

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
            bmOptions.inSampleSize = 1;//scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            //Bitmap pdfBitmap = Bitmap.createScaledBitmap(bitmap,400,600,true);
            model.setPdfImageString(ImageUtil.convert(bitmap));


            model.setCaptured(true);
            binding.invalidateAll();
        }

    }
    String m_Title,m_Text;
    private void showEditDialog(final int id, int inputType, String pattern, boolean required) {


        Context context = this;
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
                            if (d>model.getWeight())
                            {
                                showErrorMessage(R.string.error_split_amount_too_large);
                                return false;
                            }
                            model.setSplitAmount(d);
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


    private void save() {

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
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",model.getNumber());
        setResult(Activity.RESULT_OK,returnIntent);

        finish();
    }
}
