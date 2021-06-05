package com.androstock.myweatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Button;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    TextView selectCity, cityField, detailsField, currentTemperatureField, humidity_field, pressure_field, weatherIcon, updatedField;
    ProgressBar loader;
    Typeface weatherFont;
    String city = "Gaziantep, TR";
    /* Please Put your API KEY here */
    String OPEN_WEATHER_MAP_API = "b93fd7d5703b0a73bd479fc5701b4208";
    /* Please Put your API KEY here */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        FloatingActionButton button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                MainActivity.this.startActivity(intent);


            }
        });

        loader = (ProgressBar) findViewById(R.id.loader);
        selectCity = (TextView) findViewById(R.id.selectCity);
        cityField = (TextView) findViewById(R.id.city_field);
        updatedField = (TextView) findViewById(R.id.updated_field);
        detailsField = (TextView) findViewById(R.id.details_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
        humidity_field = (TextView) findViewById(R.id.humidity_field);
        pressure_field = (TextView) findViewById(R.id.pressure_field);
        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);
        City.city=city;
        taskLoadUp(city);

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Change City");
                final EditText input = new EditText(MainActivity.this);
                input.setText(city);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Change",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                city = input.getText().toString();
                                City.city=city;
                                taskLoadUp(city);
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });

    }


    public void taskLoadUp(String query) {
        if (Function.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
        } else {  SharedPreferences sharedPref = this.getSharedPreferences("myfile",MODE_PRIVATE);


            String Current_temperature= sharedPref.getString("temperature","Kayıt Yok");
            currentTemperatureField.setText(Current_temperature);
            String details = sharedPref.getString("details","Kayıt Yok");
            detailsField.setText(details);
            String humidity = sharedPref.getString("humidity","Kayıt Yok");
            humidity_field.setText(humidity);
            String pressure = sharedPref.getString("pressure","Kayıt Yok");
            pressure_field.setText(pressure);
            String icon= sharedPref.getString("icon","Kayıt Yok");
            weatherIcon.setText(icon);
            String updatedField = sharedPref.getString("updatedField","Kayıt Yok");
            loader.setVisibility(View.GONE);
            String city = sharedPref.getString("city","Kayıt Yok");
             cityField.setText(city);


            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }



    @RequiresApi(api=Build.VERSION_CODES.CUPCAKE)
    class DownloadWeather extends AsyncTask < String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loader.setVisibility(View.VISIBLE);

        }
        protected String doInBackground(String...args) {
            String xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API+ "&lang=tr");
            return xml;
        }
        @Override
        protected void onPostExecute(String xml) {


            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    DateFormat df = DateFormat.getDateTimeInstance();

                    cityField.setText(json.getString("name").toUpperCase(Locale.getDefault()) + ", " + json.getJSONObject("sys").getString("country"));
                    detailsField.setText(details.getString("description").toUpperCase(Locale.getDefault()));
                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + "°");
                    humidity_field.setText("Humidity: " + main.getString("humidity") + "%");
                    pressure_field.setText("Pressure: " + main.getString("pressure") + " hPa");
                    updatedField.setText(df.format(new Date(json.getLong("dt") * 1000)));
                    weatherIcon.setText(Html.fromHtml(Function.setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000)));

                    loader.setVisibility(View.GONE);
                    SharedPreferences sharedPref;

                    sharedPref=getApplication().getApplicationContext().getSharedPreferences("myfile",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("temperature",String.valueOf(currentTemperatureField.getText()));

                    editor.putString("humidity", String.valueOf(humidity_field.getText()));
                    editor.putString("details",String.valueOf(detailsField.getText()));
                    editor.putString("pressure",String.valueOf(pressure_field.getText()));
                    editor.putString("icon",String.valueOf(weatherIcon.getText()));
                    editor.putString("city",String.valueOf(cityField.getText()));
                    editor.putString("updatedField",String.valueOf(updatedField.getText()));
                    editor.commit(); //Kayıt
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }


        }



    }



}