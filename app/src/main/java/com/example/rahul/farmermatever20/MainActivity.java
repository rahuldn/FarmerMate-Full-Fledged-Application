package com.example.rahul.farmermatever20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    Button btnChoose, btnUpload, button;
    ImageView imageUpload;
    final int CODE_GALLERY_REQUEST = 999;
    Bitmap bitmap;
    String urlUpload = "http://192.168.43.45/api_demo/upload.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        imageUpload = (ImageView) findViewById(R.id.imageUpload);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RetrieveContentsActivity.class);
                startActivity(intent);
            }
        });

         btnChoose.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 ActivityCompat.requestPermissions(
                 MainActivity.this,
                         new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                         CODE_GALLERY_REQUEST
                 );

             }
         });

         btnUpload.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //post image to server
                 StringRequest stringRequest = new StringRequest(Request.Method.POST, urlUpload, new Response.Listener<String>() {
                     @Override
                     public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();

                     }
                 }, new Response.ErrorListener() {
                     @Override
                     public void onErrorResponse(VolleyError error) {
                         Toast.makeText(getApplicationContext(), "error: " + error.toString(),Toast.LENGTH_LONG).show();
                     }
                 }){
                     @Override
                     protected Map<String, String> getParams() throws AuthFailureError {
                         Map<String, String> params =new  HashMap<>();
                         String imageData = imageToString(bitmap);
                         params.put("image", imageData);

                         return params;
                     }
                 };
                 RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                 requestQueue.add(stringRequest);

                 button.setVisibility(View.VISIBLE);
             }
         });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== CODE_GALLERY_REQUEST){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"),CODE_GALLERY_REQUEST);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access gallery!", Toast.LENGTH_LONG).show();
            }
            return;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE_GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri filePath = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(filePath);
                 bitmap = BitmapFactory.decodeStream(inputStream);
                imageUpload.setImageBitmap(bitmap);

            } catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;

    }

}

