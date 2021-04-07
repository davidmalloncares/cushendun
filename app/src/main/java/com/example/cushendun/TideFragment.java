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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
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
    String seaLevel = null;

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
        String tidal_api_key = BuildConfig.TidalAPIKey;
        String tidal_url = "https://admiraltyapi.azure-api.net/uktidalapi/api/V1/Stations/0644/TidalEvents?duration=1";

        setTideData(tidal_url, tidal_api_key);

        // set wave data
        String wave_api_key = BuildConfig.StormGlassAPIKey;

        Date currentDate = new Date();
        long startTime = currentDate.getTime() / 1000;
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
        long endTime = cal.getTime().getTime() / 1000;

        String wave_url = "https://api.stormglass.io/v1/weather/point?lat="+cushendun_lat+"&lng="+cushendun_long+"&params=waterTemperature,waveHeight,seaLevel&start="+startTime+"&end="+startTime+"&source=sg";
        if (waveHeight == null || waterTemp == null|| seaLevel == null) {
            setWaveData(wave_url, wave_api_key);
        } else {
            //System.out.println("already got wave data");
            displaySeaData();
        }
    }

    private void setWaveData(String url, final String wave_api_key) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray hoursArray = response.getJSONArray("hours");

                            JSONObject hoursItem = (JSONObject) hoursArray.get(0);
                            JSONArray waterTempArray = hoursItem.getJSONArray("waterTemperature");
                            JSONObject waterTempObject = (JSONObject) waterTempArray.get(0);
                            waterTemp = roundNumber(waterTempObject.getString("value"));

                            JSONArray waveHeightArray = hoursItem.getJSONArray("waveHeight");
                            JSONObject waveHeightObject = (JSONObject) waveHeightArray.get(0);
                            waveHeight = waveHeightObject.getString("value");

                            JSONArray seaLevelArray = hoursItem.getJSONArray("seaLevel");
                            JSONObject seaLevelObject = (JSONObject) seaLevelArray.get(0);
                            seaLevel = seaLevelObject.getString("value");

                            // display values
                            displaySeaData();
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

    private void displaySeaData() {
        // set values
        TextView seaLevelView = masterView.findViewById(R.id.seaLevelView);
        seaLevelView.setText("Sea level at "+ seaLevel + " m");

        TextView waveHeightView = masterView.findViewById(R.id.waveHeightView);
        waveHeightView.setText("Wave height at "+waveHeight + " m");

        TextView waterTempView = masterView.findViewById(R.id.seaTempView);
        waterTempView.setText("Water temperature is "+waterTemp+" "+(char) 0x00B0 + "C");
    }

    private void setTideData(String url, final String tidal_api_key) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        // Request a json response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                for (int i=0; i<response.length(); i++)
                {
                    try {
                        JSONObject tideInfo = response.getJSONObject(i);
                        String type = tideInfo.getString("EventType");
                        String date = tideInfo.getString("DateTime");
                        String height = tideInfo.getString("Height");

                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss");
                        Date tideData = formatter.parse(date);

                        // tide icon
                        int tideIconId = getResources().getIdentifier(
                                "tideIcon"+i,
                                "id",
                                getActivity().getPackageName()
                        );
                        ImageView tideIcon = masterView.findViewById(tideIconId);
                        tideIcon.setImageResource(getTideIcon(type));

                        // tide type
                        int tideTypeId = getResources().getIdentifier(
                                "tideType"+i,
                                "id",
                                getActivity().getPackageName()
                        );
                        TextView tideType = masterView.findViewById(tideTypeId);
                        type = type.replace("Water", " Tide");
                        tideType.setText(type);

                        // tide time
                        int tideTimeId = getResources().getIdentifier(
                                "tideTime"+i,
                                "id",
                                getActivity().getPackageName()
                        );
                        TextView tideTime = masterView.findViewById(tideTimeId);
                        tideTime.setText(getTideTimeString(tideData));

                        // tide height
                        int tideHeightId = getResources().getIdentifier(
                                "tideHeight"+i,
                                "id",
                                getActivity().getPackageName()
                        );
                        TextView tideHeight = masterView.findViewById(tideHeightId);
                        tideHeight.setText(getTideHeightText(height));
                    } catch (JSONException | ParseException e) {
                        System.out.println("Error getting tide data - "+e.getMessage());
                    }
                }
            }

            private String getTideHeightText(String height) {
                double dblHeight = Double.parseDouble(height);
                DecimalFormat df2 = new DecimalFormat("#.##");
                height = df2.format(dblHeight);
                return height+" m";
            }

            private String getTideTimeString(Date tideData) {
                Date now = new Date();
                boolean hasHappened = now.after(tideData);
                String occur = "occurs";
                if (hasHappened) {
                    occur = "occurred";
                }

                SimpleDateFormat shortTime = new SimpleDateFormat("h:mm a");
                return occur + " at "+shortTime.format(tideData);
            }

            private int getTideIcon(String tideType) {
                int tideIconId;
                switch(tideType) {
                    case "HighTide":
                        tideIconId = R.drawable.high_tide;
                        break;
                    case "LowWater":
                        tideIconId = R.drawable.low_tide;
                        break;
                    default:
                        tideIconId = R.drawable.high_tide;
                }
                return tideIconId;
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

    private String roundNumber(String number) {
        float lngVersion = Float.parseFloat(number);
        int intVersion = Math.round(lngVersion);
        return Integer.toString(intVersion);
    }
}
