package com.example.cushendun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment implements View.OnClickListener {

    View masterView = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        masterView = inflater.inflate(R.layout.homelayout, viewGroup, false);
        Button refreshButton = masterView.findViewById(R.id.button);
        refreshButton.setOnClickListener(this);

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

        // set the date
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/YYYY 'at' HH:mm a");
        Date date = new Date(System.currentTimeMillis());
        String currentDateTime = formatter.format(date);
        TextView dateView = view.findViewById(R.id.dateView);
        dateView.setText("Current date/time: "+currentDateTime);

        // set weather data
        TextView weatherInfo = view.findViewById(R.id.weatherInfo);
        ImageView weatherIcon = view.findViewById(R.id.weatherIcon);

        String dark_sky_api_key = BuildConfig.DarkSkyAPIKey;
        String weather_url = "https://api.darksky.net/forecast/" + dark_sky_api_key + "/"+cushendun_lat+","+cushendun_long+"?units=si&exclude=minutely,currently,alerts,flags";
        setWeatherData(weather_url, weatherInfo, weatherIcon);
    }

    private void setWeatherData(String url, final TextView weatherTextView, final ImageView weatherIcon) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        // Request a json response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject dailyWeather = null;
                        JSONObject hourlyWeather = null;
                        String weatherData = "";
                        try {
                            dailyWeather = response.getJSONObject("daily");
                            hourlyWeather = response.getJSONObject("hourly");
                            System.out.println("hourlyWeather="+hourlyWeather+"<");
                            JSONArray dataa = dailyWeather.getJSONArray("data");

                            String weatherIconValue = hourlyWeather.getString("icon");
                            switch(weatherIconValue) {
                                case "cloud":
                                    weatherIcon.setImageResource(R.drawable.cloud);
                                    break;
                                case "fog":
                                    weatherIcon.setImageResource(R.drawable.fog);
                                    break;
                                case "wind":
                                    weatherIcon.setImageResource(R.drawable.wind);
                                    break;
                                case "snow":
                                    weatherIcon.setImageResource(R.drawable.snowflake);
                                    break;
                                case "sleet":
                                    weatherIcon.setImageResource(R.drawable.sleet);
                                    break;
                                case "rain":
                                    weatherIcon.setImageResource(R.drawable.rain_cloud);
                                    break;
                                case "clear-day":
                                    weatherIcon.setImageResource(R.drawable.hot_sun_day);
                                    break;
                                case "clear-night":
                                    weatherIcon.setImageResource(R.drawable.cloud);
                                    break;
                                case "partly-cloudy-day":
                                    weatherIcon.setImageResource(R.drawable.sunny_sun_cloudy);
                                    break;
                                case "partly-cloudy-night":
                                    weatherIcon.setImageResource(R.drawable.cloud);
                                    break;
                                default:
                                    weatherIcon.setImageResource(R.drawable.cloud);
                            }

                            dailyWeather = dataa.getJSONObject(0);
                            System.out.println("dailyWeather="+dailyWeather);
                            String summary = hourlyWeather.getString("summary");
                            weatherData += "Summary: " + summary + "\n\n";
                            System.out.println("weatherIconValue="+weatherIconValue+", summary="+summary);

                            // format date/times
                            long sunRiseEpoch = Long.parseLong( dailyWeather.getString("sunriseTime") );
                            Date sunriseTime = new Date( sunRiseEpoch   * 1000 );
                            long sunSetEpoch = Long.parseLong( dailyWeather.getString("sunsetTime") );
                            Date sunsetTime = new Date( sunSetEpoch * 1000 );

                            SimpleDateFormat formatter= new SimpleDateFormat("hh:mm:ss a");

                            weatherData += "Sunrise Time: " + formatter.format(sunriseTime) + "\n";
                            weatherData += "Sunset Time: " + formatter.format(sunsetTime) + "\n\n";

                            // chance of precipitation
                            float chanceOfPrec = Float.parseFloat(dailyWeather.getString("precipProbability"));
                            chanceOfPrec = chanceOfPrec * 100;
                            weatherData += "Chance of precipitation (" + dailyWeather.getString("precipType") + "): " + chanceOfPrec + "%\n\n";

                            weatherData += "High temp: " + dailyWeather.getString("temperatureHigh") + (char) 0x00B0 + "C\n";
                            weatherData += "Low temp: " + dailyWeather.getString("temperatureLow") + (char) 0x00B0 + "C\n\n";

                            String windGust = dailyWeather.getString("windGust");
                            String windBearing = dailyWeather.getString("windBearing");
                            String compassBearing = getCompassBearing(windBearing);

                            weatherData += "Wind speed: "+windGust+" mph from " + compassBearing + "\n";
                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println("Bombed out getting weather data - "+e.getMessage());
                        }
                        weatherTextView.setText(weatherData);
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

    private String getCompassBearing(String windBearing) {
        int numWindBearing = Integer.parseInt(windBearing);

        String compassBearing = "N";
        if ((numWindBearing >= 348.76 && numWindBearing <= 360) || (numWindBearing >= 0 && numWindBearing <= 11.25)) {
            compassBearing = "N";
        } else if (numWindBearing >= 11.26 && numWindBearing < 33.75) {
            compassBearing = "NNE";
        } else if (numWindBearing >= 33.75 && numWindBearing < 56.25) {
            compassBearing = "NE";
        } else if (numWindBearing >= 56.25 && numWindBearing < 78.75) {
            compassBearing = "ENE";
        } else if (numWindBearing >= 78.75  && numWindBearing < 101.25) {
            compassBearing = "E";
        } else if (numWindBearing >= 101.25 && numWindBearing < 123.75) {
            compassBearing = "ESE";
        } else if (numWindBearing >= 123.75  && numWindBearing < 146.25) {
            compassBearing = "SE";
        } else if (numWindBearing >= 146.75  && numWindBearing < 146.25) {
            compassBearing = "SE";
        } else if (numWindBearing >= 123.75  && numWindBearing < 168.75) {
            compassBearing = "SSE";
        } else if (numWindBearing >= 168.75  && numWindBearing < 191.25) {
            compassBearing = "S";
        } else if (numWindBearing >= 191.25  && numWindBearing < 213.75) {
            compassBearing = "SSW";
        } else if (numWindBearing >= 213.75  && numWindBearing < 236.25) {
            compassBearing = "SW";
        } else if (numWindBearing >= 236.25  && numWindBearing < 258.75) {
            compassBearing = "WSW";
        } else if (numWindBearing >= 258.75  && numWindBearing < 281.25) {
            compassBearing = "W";
        } else if (numWindBearing >= 281.25  && numWindBearing < 303.75) {
            compassBearing = "WNW";
        } else if (numWindBearing >= 303.75  && numWindBearing < 326.25) {
            compassBearing = "NW";
        } else if (numWindBearing >= 326.25  && numWindBearing < 348.75) {
            compassBearing = "NNW";
        } else {
            compassBearing = "Unknown bearing - " + numWindBearing;
        }

        return compassBearing;
    }
}
