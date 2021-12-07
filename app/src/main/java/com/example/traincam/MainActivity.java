package com.example.traincam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    PreviewView mPreviewView;
    ImageView captureImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreviewView = findViewById(R.id.previewView);
        captureImage = findViewById(R.id.captureImg);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }



    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener( () -> {


                try {
                    // Camera provider is now guaranteed to be available
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (InterruptedException | ExecutionException e) {
                    // Currently no exceptions thrown. cameraProviderFuture.get()
                    // shouldn't block since the listener is being called, so no need to
                    // handle InterruptedException.
                }

        }, ContextCompat.getMainExecutor(this));
    }


    void bindPreview(@NonNull ProcessCameraProvider cameraProvider){

        // Set up the view finder use case to display camera preview
        Preview preview = new Preview.Builder().build();

        // Choose the camera by requiring a lens facing
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Connect the preview use case to the previewView
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        // Set up the capture use case to allow users to take photos
        ImageCapture.Builder builder = new ImageCapture.Builder();
        //       .build();

        // Attach use cases to the camera with the same lifecycle owner
        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageCapture, preview);

        //ButtonOnclick
        captureImage.setOnClickListener(v -> {

            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mDateFormat.format(new Date())+ ".jpg");
            //File file = new File(Environment.getExternalStorageDirectory() + File.separator + "");
            //File file = new File(getContext().getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".png");




            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    Log.d("erroronimagesaved","erroronimagesaved");
                    error.printStackTrace();
                }
            });
        });

    }

//    public String getBatchDirectoryName() {
//
//        String app_folder_path = "";
//        app_folder_path = Environment.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/images";
//        Log.d("folder",app_folder_path);
//        File dir = new File(app_folder_path);
//        if (!dir.exists() && !dir.mkdirs()) {
//
//        }
//
//        return app_folder_path;
//    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
}