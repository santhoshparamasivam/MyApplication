package com.example.sathya.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 55;
    private static final int REQUEST_CAMERA_PERMISSION = 66;
    private static final int CAMERA_REQUEST_WRITE_PERMISSION = 88;
    Button btn_capture,btn_upload;
    File image_output_File;
    String pictureImagePath;
    Uri outputFileUri;
    Uri imageUri;
    String path;
    String fileName;
    Bitmap bitmap;
    ImageView imageView;
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_upload=findViewById(R.id.btn_upload);
        btn_capture = findViewById(R.id.capture);
        imageView=findViewById(R.id.imageView);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStoragePermissionGranted(1);
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToServer();
            }
        });
    }

    private void uploadToServer() {
        File Multipart_file = new File(path);
        Log.e("file",Multipart_file.getName()+"  "+Multipart_file.getPath());
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), Multipart_file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", Multipart_file.getName(), requestFile);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder().addHeader("Header", "value").build();
                return chain.proceed(request);
            }
        });
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("Your UrL Here")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        ApiManager service = retrofit.create(ApiManager.class);
        service.updateImage(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            int status_code=response.code();
                            if (status_code==200)
                                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                         Toast.makeText(getApplicationContext(),"Failure",Toast.LENGTH_LONG).show();
                    }
                });



    }

    private void isStoragePermissionGranted(int checkpermission) {
        switch (checkpermission) {
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    isStoragePermissionGranted(CAMERA_REQUEST_WRITE_PERMISSION);
                }
                break;
            case CAMERA_REQUEST_WRITE_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_WRITE_PERMISSION);
                } else {
                    openBackCamera();
                }
                break;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!(permissions == null)) {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        isStoragePermissionGranted(CAMERA_REQUEST_WRITE_PERMISSION);
                    } else {
                        String details = "You need to enable Camera permissions for this app. The following screen will ask for permission, please approve?";
                        Toast.makeText(this, details, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                case CAMERA_REQUEST_WRITE_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        openBackCamera();
                    } else {
                        String details = "You need to enable Camera permissions for this app. The following screen will ask for permission, please approve?";
                        Toast.makeText(this, details, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }
    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (outputFileUri != null) {
                    try {
                        File imageFile =new File((outputFileUri.getPath()));
                        imageUri = Uri.fromFile(imageFile);
                        path = imageUri.getPath();
                        if (path != null) {
                            fileName = new File(path).getName();
                        }
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                            Toast.makeText(this, "Please Select Image And Try Agian", Toast.LENGTH_SHORT).show();

                }
            } else {

                        Toast.makeText(this, "Please Select Image And Try Agian", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openBackCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File mainDir = new File(  "/sdcard/Image/");
        if (!mainDir.exists() && !mainDir.isDirectory()) {
            mainDir.mkdirs();
        }else {
            mainDir = new File( "/sdcard/Image/");
            if (!mainDir.exists() && !mainDir.isDirectory()) {
                mainDir.mkdirs();
            }
        }

        image_output_File = new File(mainDir, imageFileName);
        pictureImagePath = mainDir.getAbsolutePath() + "/" + imageFileName;
        if (!image_output_File.exists()) {
            try {
                image_output_File.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(pictureImagePath);
        outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);


    }
}