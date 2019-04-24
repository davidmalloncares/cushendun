package com.example.cushendun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener {

    View masterView = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        masterView = inflater.inflate(R.layout.homelayout, viewGroup, false);

        // display default info
        updateData(masterView);

        return masterView;
    }

    @Override
    public void onClick(View v) {
        updateData(masterView);
    }

    public void updateData(View view) {
        // cushendun stats
        String cushendun_long = "-6.041950";
        String cushendun_lat = "55.124710";

        // set weather data
        TextView sunInfo = view.findViewById(R.id.sunTimesView);
        ImageView weatherIcon = view.findViewById(R.id.weatherIcon);

        String dark_sky_api_key = BuildConfig.DarkSkyAPIKey;
        String weather_url = "https://api.darksky.net/forecast/" + dark_sky_api_key + "/"+cushendun_lat+","+cushendun_long+"?units=si&exclude=minutely,currently,alerts,flags";
        setWeatherData(weather_url, sunInfo, weatherIcon);
    }

    private void setWeatherData(String url, final TextView sunTimesView, final ImageView weatherIcon) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        // Request a json response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject weeklyWeather = null;
                        JSONObject dailyWeather = null;
                        JSONObject hourlyWeather = null;
                        String sunData = "";
                        try {
                            weeklyWeather = response.getJSONObject("daily");
                            //System.out.println("full weeklyWeather="+weeklyWeather);
                            //hourlyWeather = response.getJSONObject("hourly");
                            //System.out.println("hourlyWeather="+hourlyWeather+"<");
                            JSONArray daysArray = weeklyWeather.getJSONArray("data");
                            ArrayList<WeatherData> daysWeather = WeatherData.buildWeeklyWeather(daysArray);

                            // get first days weather
                            WeatherData todaysWeather = daysWeather.get(0);
                            weatherIcon.setImageResource(todaysWeather.weatherIconId);

                            // get the date
                            TextView todaysSummary = masterView.findViewById(R.id.todaysSummary);
                            TextView highTempView = masterView.findViewById(R.id.highView);
                            TextView lowTempView = masterView.findViewById(R.id.lowView);

                            todaysSummary.setText("Today: "+todaysWeather.summary+ " High of "+ todaysWeather.highTemp + (char) 0x00B0 + "C and a low of "+ todaysWeather.lowTemp + (char) 0x00B0 + "C");
                            highTempView.setText(todaysWeather.highTemp + (char) 0x00B0);
                            lowTempView.setText(todaysWeather.lowTemp + (char) 0x00B0);

                            // set sun times
                            sunData += "Sunrise " + todaysWeather.sunRise + ", setting at "+ todaysWeather.sunSet;

                            // chance of precipitation
                            TextView rainData = masterView.findViewById(R.id.rainDataView);
                            rainData.setText(todaysWeather.chanceOfPrecipitation + "% chance of " + todaysWeather.precipitationType + " around " + todaysWeather.precipitationTime);

                            // wind
                            ImageView windDir = masterView.findViewById(R.id.windDirection);
                            TextView windSpeedView = masterView.findViewById(R.id.windSpeedView);
                            windDir.setRotation(todaysWeather.windDir);
                            windSpeedView.setText("Wind " + todaysWeather.windGust+" mph from " + todaysWeather.windCompassBearing);

                            // add summary for next 3 days below
                            WeatherData day2Weather = daysWeather.get(1);
                            WeatherData day3Weather = daysWeather.get(2);
                            WeatherData day4Weather = daysWeather.get(3);

                            TextView day2Date = masterView.findViewById(R.id.day2DateView);
                            ImageView day2Icon = masterView.findViewById(R.id.day2Icon);
                            TextView day2High = masterView.findViewById(R.id.day2High);
                            TextView day2Low = masterView.findViewById(R.id.day2Low);
                            day2Date.setText(day2Weather.shortDate);
                            day2Icon.setImageResource(day2Weather.weatherIconId);
                            day2High.setText(day2Weather.highTemp + (char) 0x00B0);
                            day2Low.setText(day2Weather.lowTemp + (char) 0x00B0);

                            TextView day3Date = masterView.findViewById(R.id.day3DateView);
                            ImageView day3Icon = masterView.findViewById(R.id.day3Icon);
                            TextView day3High = masterView.findViewById(R.id.day3High);
                            TextView day3Low = masterView.findViewById(R.id.day3Low);
                            day3Date.setText(day3Weather.shortDate);
                            day3Icon.setImageResource(day3Weather.weatherIconId);
                            day3High.setText(day3Weather.highTemp + (char) 0x00B0);
                            day3Low.setText(day3Weather.lowTemp + (char) 0x00B0);

                            TextView day4Date = masterView.findViewById(R.id.day4DateView);
                            ImageView day4Icon = masterView.findViewById(R.id.day4Icon);
                            TextView day4High = masterView.findViewById(R.id.day4High);
                            TextView day4Low = masterView.findViewById(R.id.day4Low);
                            day4Date.setText(day4Weather.shortDate);
                            day4Icon.setImageResource(day4Weather.weatherIconId);
                            day4High.setText(day4Weather.highTemp + (char) 0x00B0);
                            day4Low.setText(day4Weather.lowTemp + (char) 0x00B0);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println("Bombed out getting weather data - "+e.getMessage());
                        }
                        sunTimesView.setText(sunData);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Failure to get weather info - "+error.getMessage());
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}
