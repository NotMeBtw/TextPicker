package com.example.user.project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button buttonShoot, buttonPick;
    Uri fileUri;
    Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        buttonShoot = (Button) findViewById(R.id.buttonShoot);
        buttonShoot.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                shoot();
            }
        });
        buttonPick = (Button) findViewById(R.id.buttonPick);
        buttonPick.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                pick();
            }
        });
    }

    private void shoot() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            startActivityForResult(intent, 0);
        } else {
            Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    private void pick() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int rotate = 0;
        if (resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {

                ExifInterface exif = new ExifInterface(selectedImage.getPath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                }
                photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Matrix mat = new Matrix();
            if (rotate == 0 && photo.getWidth() > photo.getHeight())
                mat.postRotate(90);
            else
                mat.postRotate(rotate);

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), mat, true);
            photo.compress(Bitmap.CompressFormat.JPEG, 50, bao);
            byte[] byteArray = bao.toByteArray();

            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("photo", byteArray);
            startActivity(intent);
        }
    }

}