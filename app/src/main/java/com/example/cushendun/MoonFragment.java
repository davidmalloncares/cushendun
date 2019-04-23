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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class MoonFragment extends Fragment {

    View masterView = null;
    // cushendun stats
    String cushendun_long = "-6.041950";
    String cushendun_lat = "55.124710";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        masterView = inflater.inflate(R.layout.moonlayout, viewGroup, false);

        // set data for current moon phase
        String dark_sky_api_key = BuildConfig.DarkSkyAPIKey;
        String weather_url = "https://api.darksky.net/forecast/" + dark_sky_api_key + "/"+cushendun_lat+","+cushendun_long+"?units=si&exclude=minutely,currently,alerts,flags";

        TextView currentMoonLabel = masterView.findViewById(R.id.currentMoonLabel);
        ImageView moonIcon = masterView.findViewById(R.id.currentMoonIcon);
        TextView moonPhases = masterView.findViewById(R.id.upcomingMoonPhases);
        setMoonPhase(weather_url, currentMoonLabel, moonIcon);

        // set data for future moon phases
        String moonDataUrl = "https://www.timeanddate.com/moon/phases/uk/belfast";
        setMoonData(moonDataUrl, moonPhases);

        return masterView;
    }

    private void setMoonData(String url, final TextView moonPhases) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        class MoonData {
            String phase = "";
            String datetime = "";

            public void showData() {
                String data = "Phase: "+phase+", Date/Time: "+ datetime;
                System.out.println("Moon data= "+data);
            }
        }
        // Request a response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Document doc = Jsoup.parse(response);
                        Element table = doc.getElementById("mn-cyc");
                        Elements rows = table.select("tr");

                        //System.out.println("table data="+table.toString()+", rows="+rows.size());
                        ArrayList<MoonData> moonDataSet = new ArrayList<MoonData>(rows.size());

                        // initialise list of data - by column
                        Elements cols = rows.get(0).select("td");
                        for (int i = 0; i < cols.size(); i++) {
                            MoonData moonData = new MoonData();
                            moonData.phase = cols.get(i).text();
                            moonDataSet.add(moonData);
                        }
                        cols = rows.get(2).select("td");
                        for (int i = 0; i < cols.size(); i++) {
                            MoonData moonData = moonDataSet.get(i);
                            String dateTime = cols.get(i).text();
                            int splitPoint = dateTime.indexOf(":")+3;
                            dateTime = dateTime.substring(0, splitPoint);
                            moonData.datetime = dateTime;
                        }

                        String moonText = "";
                        for (MoonData moon : moonDataSet) {
                            moonText += "Phase: " + moon.phase + " visible on "+moon.datetime+"\n";
                        }
                        moonPhases.setText(moonText);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Getting future moon data - that didn't work! - " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void setMoonPhase(String url, final TextView currentMoonLabel, final ImageView moonIcon) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        // Request a json response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONObject dailyWeather = null;
                        try {
                            dailyWeather = response.getJSONObject("daily");
                            JSONArray dataa = dailyWeather.getJSONArray("data");

                            dailyWeather = dataa.getJSONObject(0);

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
                                moonPhaseName = "Full Moon";
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
                            moonIcon.setImageResource(phaseIconId);
                            currentMoonLabel.setText("Current Moon Phase: "+moonPhaseName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println("Bombed out getting current moon data - "+e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Failure to get current moon info - "+error.getMessage());
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}
