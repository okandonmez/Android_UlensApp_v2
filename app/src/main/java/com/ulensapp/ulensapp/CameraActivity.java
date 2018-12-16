package com.ulensapp.ulensapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    Camera camera;  // Kamera Nesnesi

    SurfaceView surfaceView;            // Custom UI eklenmesi için
    SurfaceHolder surfaceHolder;        // Surface nesneleri

    LayoutInflater controlInflater = null;

    Camera.PictureCallback rawCallback;     // Kamera için parametreler
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;

    boolean previewing = false;

    AlertDialog.Builder builder;

    ProgressDialog pd;

    ConstraintLayout clCamera;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences= getSharedPreferences("prefs", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        //token=sharedPreferences.getString("token","");


        builder = new AlertDialog.Builder(CameraActivity.this);
        token = getIntent().getStringExtra("token");

        Log.e("token",token);

        clCamera = findViewById(R.id.clCamera);




        final String token = getIntent().getStringExtra("token");
        // Toast.makeText(getApplicationContext(),token,Toast.LENGTH_SHORT).show();


//        CameraActivity.this.requestWindowFeature(Window.FEATURE_NO_TITLE);


        CameraActivity.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        CameraActivity.this.setContentView(R.layout.activity_camera);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // Ekran rotasyonu
        // Yazmazsam yan çekmeye çalışır


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);   // Full Screen için


        surfaceView = (SurfaceView)findViewById(R.id.camerapreview);        // Aktivitinin layoutunda yer alan
        // surfaceview nesnesi tanıtmak


        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(CameraActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        controlInflater = LayoutInflater.from(getBaseContext());            // Anlamadım :(

        View viewControl = controlInflater.inflate(R.layout.cameralayoutnew, null);


        final Button capture = (Button) viewControl.findViewById(R.id.takepicture);   // cameraLayout.xml'deki
        // butona ulaşmak için
//        ImageView imgBack = viewControl.findViewById(R.id.corner);
//        imgBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(),HomePage.class);
//                intent.putExtra("token",token);
//                startActivity(intent);
//                finish();
//            }
//        });

        // final ImageView corner = (ImageView) viewControl.findViewById(R.id.corner);


        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pd = new ProgressDialog(CameraActivity.this);
                pd.setMessage("Fotoğraf çekildi, İşleniyor bekleyiniz");
                //  pd.show();
                Toast.makeText(getApplicationContext(),"Fotoğraf çekildi, İşleniyor bekleyiniz",Toast.LENGTH_SHORT).show();

                captureImage();


                capture.setVisibility(View.INVISIBLE);
                // corner.setVisibility(View.INVISIBLE);


            }
        });



        rawCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Log", "onPictureTaken - raw");
            }
        };

        /** Handles data for jpeg picture */
        shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };

        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeByteArray(data,0,data.length,options);
//                int imageHeight = options.outHeight;
//                int imageWidth = options.outWidth;
//                String imageType = options.outMimeType;
//
//                Log.e("Bitmap scales", String.valueOf(calculateInSampleSize(options,512,384)));
//                options.inJustDecodeBounds = true;
//
                Bitmap bitmap = decodeSampledBitmapFromResource(data,512,384);

                final String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/Camera/ULens";
                File myDir = new File(root);
                myDir.mkdirs();

                Calendar c = Calendar.getInstance();

                SimpleDateFormat df = new SimpleDateFormat("d MMM yyyy, HH:mm:ss");
                String formattedDate = df.format(c.getTime());

                final String fname = "Image-" + formattedDate + ".jpg";
                File file = new File(myDir, fname);

                if (file.exists()) file.delete();

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    try{
                        bitmap = RotateBitmap(bitmap,90);
                    }catch (Error ee){
                        Toast.makeText(getApplicationContext(),"Fotoğrafı Tekrar Çekiniz.",Toast.LENGTH_LONG).show();
                        ee.printStackTrace();
                        return;
                    }

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                    Log.e("!!!!!!!!! Is Recycled,", String.valueOf(bitmap.isRecycled()));
                    bitmap.recycle();
                    bitmap = null;

                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);

                Intent intent = new Intent(getBaseContext(), InvoiceDetail.class);

                intent.putExtra("token",token);
                intent.putExtra("BitmapPath", root+"/"+fname);

                startActivity(intent);
                finish();


            }
        };






        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
    }

//    @Override
//    protected void onDestroy() {
//        pd.dismiss();
//        super.onDestroy();
//    }

    public static Bitmap decodeSampledBitmapFromResource(byte[] data, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data,0,data.length,options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data,0,data.length,options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void restartApp(){
        Intent mStartActivity = new Intent(CameraActivity.this, SplashScreen.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(CameraActivity.this, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) CameraActivity.this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }


    private void captureImage() {
        // TODO Auto-generated method stub
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }


    public Bitmap RotateBitmap(Bitmap source, float angle) throws OutOfMemoryError // It rotates the bitmap for given parameter
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);


        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);


    }

    public void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.setDisplayOrientation(90);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);



    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;

    }
}