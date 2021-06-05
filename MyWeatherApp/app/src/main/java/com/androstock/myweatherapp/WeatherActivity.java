package com.androstock.myweatherapp;

/**
 * Created by TOSHIBA on 26.07.2019.
 */

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherActivity extends AppCompatActivity {
    public static String TAG = WeatherActivity.class.getSimpleName();
    List<String>tempList;
    @BindView(R.id.tvCityName)
    TextView tvCityName;

    @BindView(R.id.recycleView)
    RecyclerView recycleView;

    private Handler handler;

    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        progressBar = new ProgressDialog(this);
        progressBar.setTitle(getString(R.string.please_wait));
        progressBar.setCanceledOnTouchOutside(false);

        handler = new Handler();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(mLayoutManager);
        recycleView.setItemAnimator(new DefaultItemAnimator());

        updateWeatherData();
    }

    private void updateWeatherData() {
        progressBar.show();
        new Thread() {
            public void run() {

                final JSONObject json = NetworkCall.getJSON(WeatherActivity.this, City.city);
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.dismiss();
                            loadData("forecastfile");
                            Toast.makeText(WeatherActivity.this, "INTERNET BAGLANTINIZI KONTROL EDİNİZ", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void renderWeather(JSONObject json) {
        try {
            JSONObject jsonCity = json.getJSONObject("city");
            tvCityName.setText(jsonCity.getString("name").toUpperCase(Locale.US) + ", " + jsonCity.getString("country"));
            if(Veriler.list.size()>0 || Veriler.list != null){
                Veriler.list.clear();
            }
            tempList=new ArrayList<>();
            for (int i = 0; i < json.getJSONArray("list").length(); i++) {
                JSONObject details = json.getJSONArray("list").getJSONObject(i);
                String date = details.getString("dt_txt");

                JSONObject main = details.getJSONObject("main");
                String temp = main.getString("temp");
                String humidity = main.getString("humidity");
                String pressure = main.getString("pressure");

                JSONObject jsonArrayWeather = details.getJSONArray("weather").getJSONObject(0);
                String mains = jsonArrayWeather.getString("main");
                String description = jsonArrayWeather.getString("description");
                String icon = jsonArrayWeather.getString("icon");

                JSONObject wind = details.getJSONObject("wind");
                String speed = wind.getString("speed");
                String deg = wind.getString("deg");

                WeatherPOJO weatherPOJO = new WeatherPOJO();
                weatherPOJO.setCity(jsonCity.getString("name").toUpperCase(Locale.US) + ", " + jsonCity.getString("country"));
                weatherPOJO.setDate(date);
                weatherPOJO.setTemp(temp);
                weatherPOJO.setHumidity(humidity);
                weatherPOJO.setPressure(pressure);

                weatherPOJO.setDescription(description);
                weatherPOJO.setSpeed(speed);
                weatherPOJO.setDeg(deg);
                weatherPOJO.setIcon(icon);
                Veriler.list.add(weatherPOJO);
                //share preferences
                String message=weatherPOJO.getCity()+"//"+weatherPOJO.getDate()+"//"+weatherPOJO.getTemp()+"//"+weatherPOJO.getHumidity()+"//"+
                        weatherPOJO.getPressure()+"//"+weatherPOJO.getDescription()+"//"+weatherPOJO.getSpeed()+"//"+
                        weatherPOJO.getDeg()+"//"+weatherPOJO.getIcon();
                tempList.add(message);
            }
            saveData(tempList,"forecastfile");
            setAdapter();
        } catch (Exception e) {
            progressBar.dismiss();
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }

    }
    //share  preferences
    private void saveData(List<String> tempList, String forecastfile) {
        Array array= new Array();
        if(tempList==null){
            array.setDatalist(Arrays.asList(""));
        }
        else array.setDatalist(tempList);
        SharedPreferences sharedPreferences=getSharedPreferences(forecastfile,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        Gson gson =new Gson();
        String json =gson.toJson(array,Array.class);
        editor.putString("key",json);
        editor.apply();

    }

    private void setAdapter() {
        WeatherAdapter adapter = new WeatherAdapter(this, Veriler.list);
        recycleView.setAdapter(adapter);
        progressBar.dismiss();
    }

    private void loadData(String forecastfile){
        SharedPreferences sharedPref;

        sharedPref=getApplication().getApplicationContext().getSharedPreferences(forecastfile,MODE_PRIVATE);
        String json=sharedPref.getString("key","boş");
        if(json.equalsIgnoreCase("boş")){

        }
        else if(!json.equalsIgnoreCase("null")){
            Gson gson =new Gson();
            Array array =gson.fromJson(json,Array.class);
            goster(array);
        }



    }

    private void goster(Array array) {
        if(array!=null){
            Veriler.list.clear();
            for(String item:array .getDatalist()){
                WeatherPOJO temp =new WeatherPOJO();
                String[]veri =item.split("//");
                temp.setCity(veri[0]);
                temp.setDate(veri[1]);
                temp.setTemp(veri[2]);
                temp.setHumidity(veri[3]);
                temp.setPressure(veri[4]);
                temp.setDescription(veri[5]);
                temp.setSpeed(veri[6]);
                temp.setDeg(veri[7]);
                temp.setIcon(veri[8]);
                Veriler.list.add(temp);
            }
            setAdapter();
        }
    }
}