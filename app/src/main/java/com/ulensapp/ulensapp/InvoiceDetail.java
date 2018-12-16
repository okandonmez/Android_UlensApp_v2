package com.ulensapp.ulensapp;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.ulensapp.ulensapp.RegEx.ExactBill;
import com.ulensapp.ulensapp.RegEx.Price;
import com.ulensapp.ulensapp.SupportClasses.SingleUploadBroadcastReceiver;
import com.ulensapp.ulensapp.SupportClasses.VolleyClass;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvoiceDetail extends AppCompatActivity implements SingleUploadBroadcastReceiver.Delegate{

    ImageView imgHeader;
    TextView detectedTextView,txtName,txtDate,txtAmount,txtTaxAmount,txtToken;
    EditText edtTax,edtName,edtAmount, edtKdvAmount,edtDescription, edtInvoiceNo;
    Spinner spPayment;
    Spinner spCategory;

    Button btnSubmit;

    String []paymentMethod = {"Kredi Kartı","Nakit"};

    String sendInvoiceUrl="https://www.ulensapp.com/Api/Bill/PostInvoice";
    String addInvoicePhotoUrl = "https://ulensapp.com/Api/Bill/PostInvoicePhoto";

    Switch swBillable;
    String bitmapPath;


    int year,month,day;
    static final int DILOG_ID=0;

    AlertDialog.Builder builder;

    String token;
    RelativeLayout rlInvoiceDetail;

    Spinner spKdvRatio;

    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        btnSubmit = findViewById(R.id.btnSubmitDetail);
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        spKdvRatio = findViewById(R.id.spKdvRatio);
        detectedTextView = findViewById(R.id.txtTaxAmoPosted);
        edtInvoiceNo = findViewById(R.id.edtInvoiceNo);

        txtName = findViewById(R.id.txtName);
        edtName = findViewById(R.id.edtNameDetail);


        txtDate = findViewById(R.id.txtDateRow);
        txtAmount = findViewById(R.id.txtAmount);
        txtToken = findViewById(R.id.txtToken);
        edtTax = findViewById(R.id.edtTax);
        edtAmount = findViewById(R.id.edtAmount);
        edtKdvAmount=findViewById(R.id.edtKdvAmount);
        edtDescription = findViewById(R.id.edtDescription);

        swBillable = findViewById(R.id.swBillable);

        spPayment = findViewById(R.id.spPayment);
        spCategory = findViewById(R.id.spCategory);

        txtTaxAmount=findViewById(R.id.txtTaxAmount);

        rlInvoiceDetail = findViewById(R.id.rlInvoiceDetail);

        imgHeader = findViewById(R.id.imgBillDetail);


        List<String> list = new ArrayList<String>();
        list.add("%1");
        list.add("%8");
        list.add("%18");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKdvRatio.setAdapter(dataAdapter);
        spKdvRatio.setSelection(1);




        bitmapPath = getIntent().getStringExtra("BitmapPath");
        token = getIntent().getStringExtra("token");




        Bitmap bMap = null;
        File imgFile = new  File(bitmapPath);
        Bitmap myBitmap = null;
        if(imgFile.exists()){
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            bMap = myBitmap;
            imgHeader.setImageBitmap(myBitmap);

            final Bitmap finalMyBitmap = myBitmap;
            imgHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InvoiceDetail.this);
                    LayoutInflater inflater = InvoiceDetail.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialogimage, null);
                    ImageView imageView = dialogView.findViewById(R.id.my_image);
                    imageView.setImageBitmap(finalMyBitmap);



                    builder.setView(dialogView)
                            .setPositiveButton("Kapat", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    Dialog dialog = builder.create();
                    dialog.show();

                    dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                }
            });

        }






        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, paymentMethod);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spPayment.setAdapter(spinnerArrayAdapter);

//        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item,  getApplicationContext().getResources().getStringArray(R.array.spinnerCategory));
//        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view getApplicationContext().getResources().getStringArray(R.array.spinnerCategory)
//        spCategory.setAdapter(spinnerArrayAdapter2);

        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this, R.layout.spinneritemblacktext,  getApplicationContext().getResources().getStringArray(R.array.spinnerCategory));
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view getApplicationContext().getResources().getStringArray(R.array.spinnerCategory)
        spCategory.setAdapter(spinnerArrayAdapter2);
        spCategory.setSelection(5);

        //new MultiplyTask().execute(bMap);


        final Bitmap finalBMap1 = bMap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        try {
                            synchronized (this) {
                                wait(500);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        inspectFromBitmap(finalBMap1);
                                    }
                                });

                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                };
                thread.start();


            }
        });

        Calendar cal = Calendar.getInstance();

        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);


        showDialogOnClick();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                sendInvoice(token);
            }
        });


    }


    public void fullScreen() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }


    private void sendInvoice(final String token) {

        if (txtDate.getText().equals("1970-01-01")){
            Toast.makeText(getApplicationContext(),"Tarih Giriniz",Toast.LENGTH_LONG).show();
            txtDate.setError("Tarih Girin.");
            return;
        }
        if (edtAmount.getText().toString().equals("0.00")){
            Toast.makeText(getApplicationContext(),"Fiyat Giriniz",Toast.LENGTH_LONG).show();
            edtAmount.setError("Fiyat Girin.");
            return;
        }
        if (edtKdvAmount.getText().toString().equals("0.00")){
            Toast.makeText(getApplicationContext(),"KDV Fiyatını Giriniz",Toast.LENGTH_LONG).show();
            edtKdvAmount.setError("KDV Girin.");
            return;
        }
        if (edtName.getText().toString().equals("Bulunamadi")){
            Toast.makeText(getApplicationContext(),"Fiş Adı Giriniz",Toast.LENGTH_LONG);
            edtName.setError("Fiş Adı Giriniz");
            return;
        }
//        if (edtDescription.getText().toString().equals("")){
//            // Toast.makeText(getApplicationContext(),"Fiş Detayı Giriniz",Toast.LENGTH_SHORT).show();
//            edtDescription.setError("Fiş Detayı Giriniz");
//            return;
//        }

        final String sw ;

        if(swBillable.isChecked())
            sw = "true";
        else
            sw = "false";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, sendInvoiceUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject json = new JSONObject(response);
                            //  Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();



                            if (json.getString("HasError").equals("false")){
                                Intent intent = new Intent(getApplicationContext(),HomePage.class);
                                intent.putExtra("token",token);
                                startActivity(intent);
                                finish();

                                Toast.makeText(getApplicationContext(),"Fatura başarıyla gönderildi",Toast.LENGTH_LONG).show();

                            }


                            String Id = json.getString("returnId");

                            //Toast.makeText(getApplicationContext(),Id,Toast.LENGTH_SHORT).show();
                            if (json.getString("HasError").equals("false")){
                                reultipart(getApplicationContext(),Id);
                            }

                            if (json.get("HasError").equals("true")){
                                Snackbar snackbar = Snackbar.make(rlInvoiceDetail,"Bir Hata Meydana Geldi.",Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }




                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization","Bearer"+" "+token);

                return params;

            }


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                int paymentMethod = spPayment.getSelectedItemPosition();
                paymentMethod--;
                String strPayment = String.valueOf(paymentMethod);
                int KdvRatio = spKdvRatio.getSelectedItemPosition();
                String KDVRatio = null;
                if (KdvRatio == 0)
                    KDVRatio = "1";
                else if (KdvRatio == 1)
                    KDVRatio = "8";
                else if (KdvRatio == 2)
                    KDVRatio = "18";

                String payment = Integer.toString(spPayment.getSelectedItemPosition());
                Log.e("payment",payment);


                params.put("Name",edtName.getText().toString());
                params.put("Amount",edtAmount.getText().toString());
                params.put("KDVAmount",edtKdvAmount.getText().toString());
                params.put("KDVRatio",KDVRatio);
                params.put("Billable",sw);
                params.put("Date",txtDate.getText().toString()+" 12:00:00");
                params.put("Description",edtDescription.getText().toString());
                params.put("Category",Integer.toString(spCategory.getSelectedItemPosition()));
                params.put("Payment",payment);

                return params;
            }
        };

        VolleyClass.getInstance(InvoiceDetail.this).addToRequestQueue(stringRequest);


    }

    public void showDialogOnClick(){
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(DILOG_ID);
            }
        });

    }

    private DatePickerDialog.OnDateSetListener dpickerlistener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            year = i;
            month = ++i1;
            day = i2;

            txtDate.setText(year+"-"+month+"-"+day);
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id == DILOG_ID)
            return new DatePickerDialog(this,dpickerlistener,year,month,day);
        return null;

    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) // It rotates the bitmap for given parameter
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void inspectFromBitmap(Bitmap bitmap) {  // Kendisine gelen BitMap resimden text okuması yapar.


        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
        try {
            if (!textRecognizer.isOperational()) {
                Log.e(TAG, "Detector dependencies are not yet available.");

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getApplicationContext(), android.R.style.Theme_DeviceDefault_Light_Dialog);
                } else {
                    builder = new AlertDialog.Builder(getApplicationContext());
                }
                builder.setTitle("Uyarı")
                        .setMessage("Kütüphane kurulamadı. Play Servis hizmetlerinden, veriyi temizlemeyi veya Ana Sayfadan" +
                                "Manuel Girişi deneyebilirsiniz.")
                        .setPositiveButton("Geri Dön", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getApplicationContext(),HomePage.class));
                                InvoiceDetail.this.finish();
                            }
                        })
                        .setIcon(R.drawable.iconerror)
                        .show();
                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                if (hasLowStorage) {
                    Toast.makeText(this,"Yetersiz alan nedeni ile, kütüphane kurulamadı", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "low storage");
                }
            }

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();  // Galeriden seçilen veya fotoğrafı çekilen
            // BitMap imaj Frame'e Build edilir
            if(frame == null){
                Log.e("ERROR", "frame null");
                return;
            }

            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);  // Text okuma işlemi
            List<TextBlock> textBlocks = new ArrayList<>();


            for (int i = 0; i < origTextBlocks.size(); i++) {
                TextBlock textBlock = origTextBlocks.valueAt(i);
                textBlocks.add(textBlock);
            }

            Collections.sort(textBlocks, new Comparator<TextBlock>() {
                @Override
                public int compare(TextBlock o1, TextBlock o2) {
                    int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                    int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                    if (diffOfTops != 0) {
                        return diffOfTops;
                    }
                    return diffOfLefts;
                }
            });

            StringBuilder detectedText = new StringBuilder();
            for (TextBlock textBlock : textBlocks) {
                if (textBlock != null && textBlock.getValue() != null) {
                    detectedText.append(textBlock.getValue());
                    detectedText.append("\n");
                }
            }

            detectedTextView.setText(detectedText.toString());     // Son olarak okunan ve string hale getirilen
            // detectedText nesnesi set edilir.

            Log.e("text",detectedText.toString());

            ExactBill exactBill = new ExactBill();
            Map<String, String> mapResult = exactBill.exactAll(detectedText.toString());

            Price price = new Price();
            Map<String, String> mapResultPrice = price.exact(detectedText.toString());


            if (mapResult.get("InvoiceNo")!=null){
                Log.e("InvoiceNo",mapResult.get("InvoiceNo"));
                edtInvoiceNo.setText(mapResult.get("InvoiceNo"));
            }

            if (mapResult.get("Ratio") != null){
                String ratio = mapResult.get("Ratio");
                if (ratio.contains("18")){
                    spKdvRatio.setSelection(2);
                }
                else if (ratio.contains("8")){
                    spKdvRatio.setSelection(1);
                }
                else{
                    spKdvRatio.setSelection(0);
                }
            }

            if (mapResult.get("Name")!=null){
                txtName.setText(mapResult.get("Name"));
                edtName.setText(mapResult.get("Name"));
            }
            if (mapResult.get("Date")!=null){
                String date = mapResult.get("Date");

                SimpleDateFormat fromUser = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

                String reformattedStr = null;
                try {

                    reformattedStr = myFormat.format(fromUser.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.e("reformatedDate",reformattedStr);

                txtDate.setText(reformattedStr);
            }

            if (mapResultPrice.get("Price") != null){
                edtAmount.setText(mapResultPrice.get("Price"));
            }

            if (mapResultPrice.get("Tax") != null){
                // txtTaxAmount.setText(mapResultPrice.get("Tax"));
                edtKdvAmount.setText(mapResultPrice.get("Tax"));
            }
            if (mapResult.get("Payment")!=null){
                String strPayment = "";
                strPayment = mapResult.get("Payment").toString();

                // Toast.makeText(getApplicationContext(),strPayment,Toast.LENGTH_SHORT).show();
                if (strPayment.equals("NAKIT")){
                    spPayment.setSelection(1);
                }
                else{
                    spPayment.setSelection(0);
                }

            }

            btnSubmit.setVisibility(View.VISIBLE);


            ClipboardManager cm = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(detectedTextView.getText());


        }
        finally {
            textRecognizer.release();
        }


    }

    @Override
    public void onBackPressed() {
        builder = new AlertDialog.Builder(InvoiceDetail.this);

        builder.setTitle("Çıkış");
        builder.setMessage("Fişi Kaydetmeden Çıkmak mı İstiyorsunuz ? ");
        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(),HomePage.class);
                intent.putExtra("token",token);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton("İptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private static final String TAG = "AndroidUploadService";

    private final SingleUploadBroadcastReceiver uploadReceiver =
            new SingleUploadBroadcastReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

    public void uploadMultipart(final Context context,String Id) {
        try {
            String uploadId = UUID.randomUUID().toString();
            uploadReceiver.setDelegate(this);
            uploadReceiver.setUploadID(uploadId);

            new MultipartUploadRequest(this,uploadId,addInvoicePhotoUrl)
                    .addFileToUpload(bitmapPath,"file")
                    .addHeader("Authorization","Bearer "+token)
                    .addParameter("invoiceId",Id)
                    .setNotificationConfig(new UploadNotificationConfig().setTitle("ULens").setCompletedMessage("Resim Başarıyla Yüklendi").setIcon(R.drawable.ulogomini))
                    .setMaxRetries(2)
                    .startUpload();

        } catch (Exception exc) {
            Log.e(TAG, exc.getMessage(), exc);
        }

    }

    @Override
    public void onProgress(int progress) {
        //your implementation
    }

    @Override
    public void onProgress(long uploadedBytes, long totalBytes) {
        Toast.makeText(getApplicationContext(),Long.toString(uploadedBytes),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(Exception exception) {
        //your implementation
    }

    @Override
    public void onCompleted(int serverResponseCode, byte[] serverResponseBody) {

    }

    @Override
    public void onCancelled() {
        //your implementation
    }







}