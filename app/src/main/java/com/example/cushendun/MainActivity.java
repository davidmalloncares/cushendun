package com.example.cushendun;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // display default info
        updateData(null);
    }

    /** Called when the user taps the Send button */
    public void updateData(View view) {

        // cushendun stats
        String cushendun_long = "-6.041950";
        String cushendun_lat = "55.124710";

        String dms_lat = "55 deg 7 m 28.956 s N";
        String dms_long = "6 deg 2 min 31.02 sec W";

        // set the date
        SimpleDateFormat formatter= new SimpleDateFormat("dd/mm/yyyy 'at' HH:mm a");
        Date date = new Date(System.currentTimeMillis());
        String currentDateTime = formatter.format(date);
        TextView dateView = findViewById(R.id.dateView);
        dateView.setText("Current date/time: "+currentDateTime);

        // set the tide data
        TextView tideInfo = findViewById(R.id.tideInfo);
        String tidal_api_key = BuildConfig.TidalAPIKey;
        String tidal_url = "https://admiraltyapi.azure-api.net/uktidalapi/api/V1/Stations/0644/TidalEvents?duration=1";

        setTideData(tidal_url, tidal_api_key, tideInfo);

        // set weather data
        TextView weatherInfo = findViewById(R.id.weatherInfo);
        ImageView weatherIcon = findViewById(R.id.weatherIcon);
        ImageView moonIcon = findViewById(R.id.moonIcon);

        String dark_sky_api_key = BuildConfig.DarkSkyAPIKey;
        String weather_url = "https://api.darksky.net/forecast/" + dark_sky_api_key + "/"+cushendun_lat+","+cushendun_long+"?units=si&exclude=minutely,hourly,currently,alerts,flags";
        setWeatherData(weather_url, weatherInfo, weatherIcon, moonIcon);

        // test celestial data
        String night_sky_url = "https://aa.usno.navy.mil/cgi-bin/aa_ssconf2.pl?form=2&year=2019&month=4&day=16&hr=0&min=0&sec=0.0&intv_mag=1.0&intv_unit=1&reps=1&place=&lon_sign=-1&lon_deg=6&lon_min=2&lon_sec=31&lat_sign=1&lat_deg=55&lat_min=7&lat_sec=28&height=0";
        setNightData(night_sky_url);
    }

    private void setNightData(String url) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(this);

        // Request a response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        String[] data = response.split("\\(UT1\\)");
                        String tableData = data[1];
                        String[] data2 = tableData.split("</pre>");

                        System.out.println("\n\ndata[1]="+data2[0]);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void setTideData(String url, final String tidal_api_key, final TextView tideInfo) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(this);

        // Request a json response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                String tidalData = "";

                for (int i=0; i<response.length(); i++)
                {
                    try {
                        JSONObject tideInfo = response.getJSONObject(i);
                        String type = tideInfo.getString("EventType");
                        String date = tideInfo.getString("DateTime");

                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss");
                        Date tideData = formatter.parse(date);
                        SimpleDateFormat shortTime = new SimpleDateFormat("hh:mm a");

                        tidalData += type + " occurs at " + shortTime.format(tideData) + "\n";
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }

                tideInfo.setText(tidalData);
            }
            }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        System.out.println("bombed out gathering tide data - "+error.getMessage());
                    }
                }) {

            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("Ocp-Apim-Subscription-Key", tidal_api_key);
                return headers;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }

    private void setWeatherData(String url, final TextView weatherTextView, final ImageView weatherIcon, final ImageView moonIcon) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a json response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject dailyWeather = null;
                        String weatherData = "";
                        try {
                            dailyWeather = response.getJSONObject("daily");
                            JSONArray dataa = dailyWeather.getJSONArray("data");

                            String weatherIconValue = dailyWeather.getString("icon");
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

                            System.out.println("weatherIconValue="+weatherIconValue+".");
                            dailyWeather = dataa.getJSONObject(0);
                            weatherData += "Summary: " + dailyWeather.getString("summary") + "\n\n";

                            // format date/times
                            long sunRiseEpoch = Long.parseLong( dailyWeather.getString("sunriseTime") );
                            Date sunriseTime = new Date( sunRiseEpoch * 1000 );
                            long sunSetEpoch = Long.parseLong( dailyWeather.getString("sunsetTime") );
                            Date sunsetTime = new Date( sunSetEpoch * 1000 );

                            SimpleDateFormat formatter= new SimpleDateFormat("hh:mm:ss a");

                            weatherData += "Sunrise Time: " + formatter.format(sunriseTime) + "\n";
                            weatherData += "Sunset Time: " + formatter.format(sunsetTime) + "\n\n";

                            System.out.println("test changes");

                            // set moon icon
                            String moonPhaseName = "Full Moon";
                            float moonPhase = Float.parseFloat(dailyWeather.getString("moonPhase"));
                            int phaseIconId = R.drawable.waning_crescent;
                            if ((moonPhase >= 0 && moonPhase < 0.125) || moonPhase == 1.0) {
                                phaseIconId = R.drawable.newmoon;
                                moonPhaseName = "New Moon";
                            } else if (moonPhase >= 0.125 && moonPhase < 0.25) {
                                phaseIconId = R.drawable.waxing_crescent;
                                moonPhaseName = "Waxing Crescent";
                            } else if (moonPhase >= 0.25 && moonPhase < 0.36) {
                                phaseIconId = R.drawable.first_quarter;
                                moonPhaseName = "First Quarter";
                            } else if (moonPhase >= 0.36 && moonPhase < 0.5) {
                                phaseIconId = R.drawable.waxing_gibbous;
                                moonPhaseName = "Waxing Gibbous";
                            } else if (moonPhase >= 0.5  && moonPhase < 0.625) {
                                phaseIconId = R.drawable.full_moon;
                                moonPhaseName = "Full Monn";
                            } else if (moonPhase >= 0.625 && moonPhase < 0.75) {
                                phaseIconId = R.drawable.waning_gibbous;
                                moonPhaseName = "Waning Gibbous";
                            } else if (moonPhase >= 0.75  && moonPhase < 0.875) {
                                phaseIconId = R.drawable.third_quarter;
                                moonPhaseName = "Last Quarter";
                            } else {
                                phaseIconId = R.drawable.waning_crescent;
                                moonPhaseName = "Waning Crescent";
                            }
                            System.out.println("numberic moonPhase="+moonPhase+", moonPhaseName="+moonPhaseName);
                            moonIcon.setImageResource(phaseIconId);
                            weatherData += "Moon Phase: " + moonPhaseName + "\n\n";

                            // chance of precipitation
                            float chanceOfPrec = Float.parseFloat(dailyWeather.getString("precipProbability"));
                            chanceOfPrec = chanceOfPrec * 100;
                            weatherData += "Chance of precipitation (" + dailyWeather.getString("precipType") + "): " + chanceOfPrec + "%\n\n";

                            weatherData += "High temp: " + dailyWeather.getString("temperatureHigh") + (char) 0x00B0 + "C\n";
                            weatherData += "Low temp: " + dailyWeather.getString("temperatureLow") + (char) 0x00B0 + "C\n";
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        weatherTextView.setText(weatherData);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}
