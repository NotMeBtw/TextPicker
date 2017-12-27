package com.example.user.project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    ImageView imageView;
    Button btpic, btnup;
    private Uri fileUri;
    String picturePath;
    Uri selectedImage;
    Bitmap photo;
    String ba1;
    byte[] ba;
    ShareActionProvider mShareActionProvider;
    public static String URL = "https://westeurope.api.cognitive.microsoft.com/vision/v1.0/ocr";
    public static final String subscriptionKey = "2db9701b41834425971caf4c9e023d6b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

        btpic = (Button) findViewById(R.id.button);
        btpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickpic();
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu.
//        // Adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu, menu);
//
//        // Access the Share Item defined in menu XML
//        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
//
//        // Access the object responsible for
//        // putting together the sharing submenu
//        if (shareItem != null) {
//            mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
//        }
//
//        // Create an Intent to share your content
//        setShareIntent();
//        return true;
//    }

    private void setShareIntent() {

        // create an Intent with the contents of the TextView
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textView.getText());

        // Make sure the provider knows
        // it should work with that Intent
        mShareActionProvider.setShareIntent(shareIntent);
    }

    private void upload(Bitmap photo) {
        // Image location URL
        Log.e("path", "----------------" + picturePath);

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        photo = Bitmap.createScaledBitmap(photo, photo.getWidth() / 2, photo.getHeight() / 2, true);
        photo.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        Log.e("base64", "-----" + ba1);

        // Upload image to server
        uploadPhoto();

    }

    private void uploadPhoto() {
        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        // 11. start progress bar
        setProgressBarIndeterminateVisibility(true);

        client.addHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

        RequestParams requestParams = new RequestParams();
        requestParams.put("data", new ByteArrayInputStream(ba));

        client.post(URL, requestParams, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                setProgressBarIndeterminateVisibility(false);

                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

//                mJSONAdapter.updateData(jsonObject.optJSONArray("docs"));
                textView.setText(jsonObject.toString());
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                setProgressBarIndeterminateVisibility(false);

                Toast.makeText(getApplicationContext(),
                        "Error: "
                                + statusCode
                                + " "
                                + throwable.getMessage(),
                        Toast.LENGTH_LONG)
                        .show();

                Log.e("omg android", statusCode + " " + throwable.getMessage());
            }
        });
    }

    private void clickpic() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            startActivityForResult(intent, 100);
        } else {
            Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {

            selectedImage = data.getData();
            photo = (Bitmap) data.getExtras().get("data");

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }

            int columnIndex = cursor != null ? cursor.getColumnIndex(filePathColumn[0]) : 0;
            picturePath = cursor != null ? cursor.getString(columnIndex) : null;
            if (cursor != null) {
                cursor.close();
            }

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(photo);

            upload(photo);
        }
    }
}