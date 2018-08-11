package com.example.amira.spectrophotometer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!OpenCVLoader.initDebug()){
            Log.d("MainActivity", "OpenCV not loaded");
        } else {
            Log.d("MainActivity", "OpenCV loaded");
        }

        imageView = (ImageView)findViewById(R.id.imageView);
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i , 0);
    }
    protected void onActivityResult(int requestCode,int resultCode , Intent data){
        super.onActivityResult(requestCode, resultCode ,  data);
        //Take the image as a bitmap
        Bitmap bm = (Bitmap)data.getExtras().get("data");
        //Re-size the bitmap
         Bitmap bitmap = Bitmap.createBitmap(bm,50,50,50, 50);
        //show the cropped bitmap
        imageView.setImageBitmap(bitmap);
         //makes a new Matrix
        Mat src = new Mat();
        //Turns the bitmap to a matrix
         Utils.bitmapToMat(bitmap,src);
        //makes a new Matrix
        Mat dst =new Mat();
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
        for( int i = 0 ; i < 50 ; i++){
            for(int j = 0 ; j < 50 ; j++){
                 h_sum += (h.get(i,j)[0]/180);
                 s_sum += (s.get(i,j)[0]/255);
                v_sum += (v.get(i,j)[0]/255);
            }
        }
        // printing the values
        Log.w("H S V", h_sum/2500+" "+s_sum/2500+ " "+v_sum/2500+" ");


    }
}
