package com.androstock.myweatherapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;

import static android.content.Context.MODE_PRIVATE;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherAppWidget extends AppWidgetProvider {
    static Context con;
    static RemoteViews views;

    static String OPEN_WEATHER_MAP_API="b93fd7d5703b0a73bd479fc5701b4208";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        views=new RemoteViews(context.getPackageName(), R.layout.weather_app_widget);
        taskLoadUp("Gaziantep,TR");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            con=context;
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void taskLoadUp(String query) {
        SharedPreferences sharedPref=con.getSharedPreferences("myfile", MODE_PRIVATE);


        String Date_Widget=sharedPref.getString("updatedField","kayıt yok");
        views.setTextViewText(R.id.date_widget,Date_Widget);
        String City_Widget=sharedPref.getString("city","kayıt yok");
        views.setTextViewText(R.id.city_widget,City_Widget);

        String Current_temperature=sharedPref.getString("temperature", "Kayıt Yok");
        views.setTextViewText(R.id.current_temp, Current_temperature);
        String icon=sharedPref.getString("icon", "Kayıt Yok");
        views.setImageViewResource(R.id.cur_weather_icon,R.drawable.after_noon);

        if(Current_temperature.equalsIgnoreCase("Kayıt Yok")){
            Intent ıntent = new Intent(con,MainActivity.class);
            con.startActivity(ıntent);
        }

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @RequiresApi(api=Build.VERSION_CODES.CUPCAKE)
    static class DownloadWeather extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        protected String doInBackground(String... args) {
            String xml=Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API + "&lang=tr");
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {


            try {
                JSONObject json=new JSONObject(xml);
                if (json != null) {
                    JSONObject details=json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main=json.getJSONObject("main");
                    DateFormat df=DateFormat.getDateTimeInstance();

                    views.setTextViewText(R.id.current_temp, String.format("%.2f", main.getDouble("temp")) + "°");
                    views.setTextViewText(R.id.date_widget, "2.02.2019");
                    views.setTextViewText(R.id.cur_weather_icon, Html.fromHtml(Function.setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000)));
                    /*cityField.setText(json.getString("name").toUpperCase(Locale.getDefault()) + ", " + json.getJSONObject("sys").getString("country"));
                    detailsField.setText(details.getString("description").toUpperCase(Locale.getDefault()));
                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + "°");
                    humidity_field.setText("Humidity: " + main.getString("humidity") + "%");
                    pressure_field.setText("Pressure: " + main.getString("pressure") + " hPa");
                    updatedField.setText(df.format(new Date(json.getLong("dt") * 1000)));
                    weatherIcon.setText(Html.fromHtml(Function.setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000)));*/


                  /*  SharedPreferences sharedPref;

                    sharedPref=con.getApplicationContext().getSharedPreferences("myfile",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("temperature",String.valueOf(currentTemperatureField.getText()));

                    editor.putString("humidity", String.valueOf(humidity_field.getText()));
                    editor.putString("details",String.valueOf(detailsField.getText()));
                    editor.putString("pressure",String.valueOf(pressure_field.getText()));
                    editor.putString("icon",String.valueOf(weatherIcon.getText()));
                    editor.putString("city",String.valueOf(cityField.getText()));
                    editor.putString("updatedField",String.valueOf(updatedField.getText()));
                    editor.commit(); //shared preferences Kayıt*/

                }
            } catch (JSONException e) {
                Toast.makeText(con, "Error, Check City", Toast.LENGTH_SHORT).show();


            }

        }

    }
}

