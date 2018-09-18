package com.example.amira.spectrophotometer;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Toolbar toolbar;
    File file;
    Uri uri;
    Intent camIntent,galIntent,cropIntent;
    final int RequestPermissionCode = 1 ;
    Button add_data;
    TextView textView,slope,intercept;
    double m, c ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        add_data = (Button)findViewById(R.id.add_data);
        add_data.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             slope =  (TextView) findViewById(R.id.slope);
                intercept =  (TextView) findViewById(R.id.intercept);
                if(!slope.getText().toString().trim().isEmpty() &&
                        slope.getText().toString().trim() != "0" &&
                        !intercept.getText().toString().trim().isEmpty()){
                    try {
                        m = Double.parseDouble(slope.getText().toString().trim());
                        c = Double.parseDouble(intercept.getText().toString().trim());
                        toolbar = (Toolbar) findViewById(R.id.toolbar);
                        toolbar.setTitle("Crop Image");
                        setSupportActionBar(toolbar);
                        findViewById(R.id.form_layout).setVisibility(View.INVISIBLE);
                        textView.setText("");

                    }
                    catch(Exception e){
                        textView.setText("Enter a valid Double");
                    }


                }
            }
        });
        imageView = (ImageView)findViewById(R.id.imageView);

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            RequestRuntimePermission();
        }



        if(!OpenCVLoader.initDebug()){
            Log.d("MainActivity", "OpenCV not loaded");
        } else {
            Log.d("MainActivity", "OpenCV loaded");
        }
    }

    private void RequestRuntimePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
            Toast.makeText(this,"CAMERA permission allows us to access Camera App", Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA} ,RequestPermissionCode);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode ,  data);
        if (requestCode == 0 && resultCode == RESULT_OK){
            CropImage();
        }
        else if (requestCode == 2){
            if(data != null){
                uri = data.getData();
                CropImage();
            }
        }
        else if (requestCode == 1){
            if(data != null){
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");
                imageView.setImageBitmap(bitmap);

                // Mat src = new Mat();
                Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);

                //Turns the bitmap to a matrix
                Utils.bitmapToMat(bitmap,src);
                //makes a new Matrix


                Mat dst = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
                // turns the src rgb matrix into hsv matrix

                Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2HSV ,3);
                //makes a new matrix arraylist

                List<Mat> image = new ArrayList<>(3);
                //split the hsv into 3 matrcies and add them to the arraylist
                Core.split(dst,image);

                // each matrix is 50*50 where each index has an array of type double
                Mat h = image.get(0);
                Mat s = image.get(1);
                Mat v = image.get(2);
                double h_sum = 0;
                double s_sum =  0;
                double v_sum = 0;
                //looping through the matrcies and summing all up

                for( int i = 0 ; i < h.rows() ; i++){
                    for(int j = 0 ; j < h.cols() ; j++){
                        h_sum += (h.get(i,j)[0]/180);
                        s_sum += (s.get(i,j)[0]/255);
                        v_sum += (v.get(i,j)[0]/255);
                    }
                }

                textView = (TextView) findViewById(R.id.textView);
                textView.setText(" S: "+(s_sum/(h.rows()*h.cols())- c)/m);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case RequestPermissionCode:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this , "Permission Granted ",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this , "Permission Cancelled ",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void CropImage() {
        try {
            cropIntent = new Intent ("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri,"image/*");
            cropIntent.putExtra("crop", true);
            cropIntent.putExtra("outputX", 180);
            cropIntent.putExtra("outputY", 180);
            cropIntent.putExtra("aspectX", 3);
            cropIntent.putExtra("aspectY", 4);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent,1);

        }
        catch (ActivityNotFoundException e){
            Toast.makeText(this , "Exception ",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.btn_camera){
            CameraOpen();
        }
        else if (item.getItemId() == R.id.btn_gallery){
            GalleryOpen();
        }
        return true;
    }
    private void CameraOpen(){
        camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(),
                "file"+String.valueOf(System.currentTimeMillis())+".jpg");
        uri = Uri.fromFile(file);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        camIntent.putExtra("return-data",true);
        startActivityForResult(camIntent,0);

    }
    private void GalleryOpen(){
        galIntent = new Intent (Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(galIntent,"select image from galary"),2);
    }
}
