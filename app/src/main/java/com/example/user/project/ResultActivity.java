package com.example.user.project;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class ResultActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView;
    ProgressBar progressBar;
    ShareActionProvider mShareActionProvider;
    public static String URL = "https://westeurope.api.cognitive.microsoft.com/vision/v1.0/ocr";
    public static final String subscriptionKey = "2db9701b41834425971caf4c9e023d6b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.INVISIBLE);

        textView = (TextView) findViewById(R.id.response);
        imageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        byte[] byteArray = intent.getExtras().getByteArray("photo");
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));

        uploadPhoto(byteArray);
    }

    private void uploadPhoto(byte[] byteArray) {
        AsyncHttpClient client = new AsyncHttpClient();

        progressBar.setVisibility(View.VISIBLE);

        client.addHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

        RequestParams requestParams = new RequestParams();
        requestParams.put("data", new ByteArrayInputStream(byteArray));

        client.post(URL, requestParams, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

                String response = "";
                try {
                    String orientation = jsonObject.getString("orientation");
                    String textAngle = jsonObject.getString("textAngle");

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
}
