package artan.com.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import artan.com.weather.model.MyWeather;
import artan.com.weather.services.DownloadService;
import artan.com.weather.utils.NetworkHelper;

import static artan.com.weather.services.DownloadService.DOWNLOAD_SERVICE_NAME;
import static artan.com.weather.services.DownloadService.INTENT_EXTRA;

public class MainActivity extends AppCompatActivity {
    private static String city = "NYC";
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private TextView tempTv, tempMinTv, tempMaxTv, cityTv, countryTv, humidityTv, descriptionTv;
    private EditText cityNameEt;
    private ImageView iconImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBackgroundImage();
        initializeUiViews();
        registerBroadcastReceiver();
        startDownloadService();
    }

    private void initBackgroundImage() {
        ImageView background = (ImageView) findViewById(R.id.iv_background);
        Glide.with(this)
                .load(R.drawable.background1)
                .centerCrop()
                .into(background);
    }

    private void initializeUiViews(){
        tempTv = (TextView)findViewById(R.id.temp_tv);
        tempMinTv = (TextView)findViewById(R.id.temp_min_tv);
        tempMaxTv = (TextView)findViewById(R.id.temp_max_tv);
        cityTv = (TextView)findViewById(R.id.city_tv);
        countryTv = (TextView)findViewById(R.id.country_tv);
        humidityTv = (TextView)findViewById(R.id.humidity_tv);
        cityNameEt = (EditText)findViewById(R.id.city_name_et);
        iconImg = (ImageView)findViewById(R.id.icon_img);
        descriptionTv = (TextView)findViewById(R.id.description_tv);

        sharedPref = getSharedPreferences("city", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        String lastCity = sharedPref.getString("cityName", "");
        if(!lastCity.equals("")){
            city=lastCity;
        }
    }

    private void startDownloadService() {
        if(NetworkHelper.hasNetworkAccess(this)) {
            String weatherUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + ",us&APPID=34affffa3d57962ba6e78627f070fad2";

            Intent intent = new Intent(this, DownloadService.class);
            intent.setData(Uri.parse(weatherUrl));
            startService(intent);
        }else{
            Snackbar.make(findViewById(R.id.activity_main), "Network Status: Disconnected!", Snackbar.LENGTH_LONG).show();
        }
    }

    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(DOWNLOAD_SERVICE_NAME));
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyWeather myWeather = (MyWeather) intent.getParcelableExtra(INTENT_EXTRA);

            if(myWeather == null){
                cityNameEt.setError("That is not a US City!");
            }else {
                String icon_url = "http://openweathermap.org/img/w/"+myWeather.getWeather().get(0).getIcon()+".png";
                double tempKelvin = myWeather.getMain().getTemp();
                double tempMinK = myWeather.getMain().getTempMin();
                double tempMaxK = myWeather.getMain().getTempMax();

                Picasso.with(getBaseContext()).load(icon_url).into(iconImg);
                tempTv.setText(kelvinToFahrenheit(tempKelvin) + " F");
                tempMinTv.setText(kelvinToFahrenheit(tempMinK) +" F");
                tempMaxTv.setText(kelvinToFahrenheit(tempMaxK) +" F");
                cityTv.setText(myWeather.getName() +", ");
                countryTv.setText(myWeather.getSys().getCountry() +"");
                humidityTv.setText(myWeather.getMain().getHumidity() +"%");
                descriptionTv.setText(myWeather.getWeather().get(0).getDescription());

                editor.putString("cityName", city);
                editor.commit();
            }
        }
    };

    private void hideKeyboard(){
        //this hides the keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private int kelvinToFahrenheit(double tempk){
        double tempF = (tempk - 273.15) * 9/5 + 32;

               int result = (int)tempF;

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mBroadcastReceiver);
    }

    public void go(View view) {
        city = cityNameEt.getText().toString();
        if(!city.equals("")) {
            startDownloadService();
            hideKeyboard();
        }else{
            cityNameEt.setError("Enter a US city name!");
        }
    }
}
