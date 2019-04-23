package com.example.cushendun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TideFragment extends Fragment {

    View masterView = null;
    String waveHeight = null;
    String waterTemp = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        masterView = inflater.inflate(R.layout.tidelayout, viewGroup, false);

        // display tide info
        updateTideData(masterView);

        return masterView;
    }

    public void updateTideData(View view) {
        // cushendun stats
        String cushendun_long = "-6.041950";
        String cushendun_lat = "55.124710";

        // set the tide data
        TextView tideInfo = view.findViewById(R.id.tideInfo);
        String tidal_api_key = BuildConfig.TidalAPIKey;
        String tidal_url = "https://admiraltyapi.azure-api.net/uktidalapi/api/V1/Stations/0644/TidalEvents?duration=1";

        setTideData(tidal_url, tidal_api_key, tideInfo);

        // set wave data
        TextView waveInfo = view.findViewById(R.id.waveInfo);
        String wave_api_key = BuildConfig.StormGlassAPIKey;

        Date currentDate = new Date();
        long startTime = currentDate.getTime() / 1000;
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
        long endTime = cal.getTime().getTime() / 1000;

        String wave_url = "https://api.stormglass.io/v1/weather/point?lat="+cushendun_lat+"&lng="+cushendun_long+"&params=waterTemperature,waveHeight&start="+startTime+"&end="+startTime+"&source=sg";
        if (waveHeight == null || waterTemp == null) {
            setWaveData(wave_url, wave_api_key, waveInfo);
        } else {
            System.out.println("already got wave data");
            waveInfo.setText(getWaveText());
        }
    }

    private void setWaveData(String url, final String wave_api_key, final TextView waveInfo) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String waveData = "";
                        try {
                            JSONArray hoursArray = response.getJSONArray("hours");

                            JSONObject hoursItem = (JSONObject) hoursArray.get(0);
                            JSONArray waterTempArray = hoursItem.getJSONArray("waterTemperature");
                            JSONObject waterTempObject = (JSONObject) waterTempArray.get(0);
                            waterTemp = waterTempObject.getString("value");

                            JSONArray waveHeightArray = hoursItem.getJSONArray("waveHeight");
                            JSONObject waveHeightObject = (JSONObject) waveHeightArray.get(0);
                            waveHeight = waveHeightObject.getString("value");

                            waveInfo.setText(getWaveText());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Failure to get wave info - "+error.getMessage());
                    }
                }) {

            @Override
            public Map getHeaders() {
                HashMap headers = new HashMap();
                headers.put("Authorization", wave_api_key);
                return headers;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private String getWaveText() {
        String waveData = "Current wave height: "+waveHeight+" meters\n";
        waveData += "Current water temp: "+waterTemp+" "+(char) 0x00B0 + "C";
        return waveData;
    }

    private void setTideData(String url, final String tidal_api_key, final TextView tideInfo) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

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
                        System.out.println("Error getting tide data - "+e.getMessage());
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
            public Map getHeaders() {
                HashMap headers = new HashMap();
                headers.put("Ocp-Apim-Subscription-Key", tidal_api_key);
                return headers;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }
}
