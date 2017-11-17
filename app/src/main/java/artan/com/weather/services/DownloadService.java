package artan.com.weather.services;


import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.io.IOException;

import artan.com.weather.model.MyWeather;
import artan.com.weather.utils.HttpHelper;

public class DownloadService extends IntentService {
    public static final String DOWNLOAD_SERVICE_NAME = "DownloadService";
    public static final String INTENT_EXTRA = "intentExtra";

    public DownloadService(){
        super("downloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Uri uri = intent.getData();

        String response;

        try {
            response = HttpHelper.downloadUrl(uri.toString());
        } catch (IOException e) {
            response = null;
        }

        Gson gson = new Gson();
        MyWeather myWeather = gson.fromJson(response, MyWeather.class);

        Intent messageIntent = new Intent(DOWNLOAD_SERVICE_NAME);
        messageIntent.putExtra(INTENT_EXTRA, myWeather);

        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        manager.sendBroadcast(messageIntent);


    }
}
