package com.example.user.project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView;
    ProgressBar progressBar;
    Button buttonShoot, buttonPick;
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

        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.INVISIBLE);

        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        try {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setShareIntent();
        return true;
    }

    private void setShareIntent() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Text recognition");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textView.getText());

        try {
            mShareActionProvider.setShareIntent(shareIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (resultCode == RESULT_OK) {

            selectedImage = data.getData();
            if (requestCode == 0) {
                photo = (Bitmap) data.getExtras().get("data");
            }
            if (requestCode == 1) {
                try {
                    photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
//            if (cursor != null) {
//                cursor.moveToFirst();
//            }
//
//            int columnIndex = cursor != null ? cursor.getColumnIndex(filePathColumn[0]) : 0;
//            picturePath = cursor != null ? cursor.getString(columnIndex) : null;
//            if (cursor != null) {
//                cursor.close();
//            }

            imageView.setImageBitmap(photo);
            upload(photo);
        }
    }

    private void upload(Bitmap photo) {
        Log.e("path", "----------------" + picturePath);

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        photo = Bitmap.createScaledBitmap(photo, photo.getWidth(), photo.getHeight(), true);
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        Log.e("base64", "-----" + ba1);

        uploadPhoto();

    }

    private void uploadPhoto() {
        AsyncHttpClient client = new AsyncHttpClient();

        progressBar.setVisibility(View.VISIBLE);

        client.addHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

        RequestParams requestParams = new RequestParams();
        requestParams.put("data", new ByteArrayInputStream(ba));

        client.post(URL, requestParams, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

                String response = "";
                try {
                    JSONArray regions = jsonObject.getJSONArray("regions");
                    for (int i = 0; i < regions.length(); i++) {
                        if (i > 0) response += "\n";
                        JSONObject region = regions.getJSONObject(i);
                        JSONArray lines = region.getJSONArray("lines");
                        for (int j = 0; j < lines.length(); j++) {
                            JSONObject line = lines.getJSONObject(j);
                            JSONArray words = line.getJSONArray("words");
                            for (int k = 0; k < words.length(); k++) {
                                JSONObject word = words.getJSONObject(k);
                                String text = word.getString("text");
                                response += text + " ";
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                textView.setText(response);
                setShareIntent();
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();

                Log.e("HTTP POST ERROR", statusCode + " " + throwable.getMessage());
            }
        });
    }
}