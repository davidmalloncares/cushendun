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
import java.util.Iterator;

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
        setMoonPhase(weather_url, currentMoonLabel, moonIcon);

        // set data for future moon phases
        String moonDataUrl = "https://www.timeanddate.com/moon/phases/uk/belfast";
        setMoonData(moonDataUrl);

        return masterView;
    }

    class MoonData {
        String phase = "";
        String datetime = "";

        public void showData() {
            String data = "Phase: "+phase+", Date/Time: "+ datetime;
            System.out.println("Moon data= "+data);
        }
    }

    private void setMoonData(String url) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        // Request a response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Document doc = Jsoup.parse(response);

                        Elements moonPhaseCards = doc.getElementsByClass("moon-phases-card");
                        Iterator<Element> moonPhaseIter = moonPhaseCards.iterator();

                        ArrayList<MoonData> moonDataSet = new ArrayList<MoonData>(moonPhaseCards.size());
                        while (moonPhaseIter.hasNext()) {
                            Element moonPhaseCard = moonPhaseIter.next();
                            MoonData moonData = new MoonData();

                            moonData.phase = moonPhaseCard.select("a").first().text();

                            String moonDate = moonPhaseCard.getElementsByClass("moon-phases-card__date").text();
                            moonData.datetime = moonDate;
                            moonDataSet.add(moonData);
                        }

                        /*
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
                            String moonDate = getMoonDate(cols.get(i).text());
                            moonData.datetime = moonDate;
                        }
*/
                        for (int i = 0; i < moonDataSet.size(); i++) {
                            // set type
                            int moonTypeId = getResources().getIdentifier(
                                    "moonType"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            TextView moonTypeView = masterView.findViewById(moonTypeId);
                            moonTypeView.setText(moonDataSet.get(i).phase);

                            // set icon
                            int moonIconId = getResources().getIdentifier(
                                    "moonIcon"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            ImageView moonIconView = masterView.findViewById(moonIconId);
                            moonIconView.setImageResource(getMoonPhaseByType(moonDataSet.get(i).phase));

                            // set date
                            int moonDateId = getResources().getIdentifier(
                                    "moonDate"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            TextView moonDateView = masterView.findViewById(moonDateId);
                            moonDateView.setText(moonDataSet.get(i).datetime);
                        }
                    }

                    private String getMoonDate(String dateTime) {
                        // drop down to dd mmm
                        int splitPoint = dateTime.indexOf(":")-3;
                        dateTime = dateTime.substring(0, splitPoint);

                        // get the day number and add the suffix
                        splitPoint = dateTime.indexOf(" ");
                        String dayPart = dateTime.substring(0, splitPoint);
                        String suffix = WeatherData.getDayNumberSuffixFromDay(Integer.parseInt(dayPart));
                        dateTime = dateTime.replace(dayPart, dayPart+suffix);
                        return dateTime;
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

    private int getMoonPhaseByType(String phase) {
        int moonPhaseIconId;
        switch(phase) {
            case "Full Moon":
                moonPhaseIconId = R.drawable.full_moon;
                break;
            case "Third Quarter":
                moonPhaseIconId = R.drawable.third_quarter;
                break;
            case "New Moon":
                moonPhaseIconId = R.drawable.newmoon;
                break;
            case "First Quarter":
                moonPhaseIconId = R.drawable.first_quarter;
                break;
            default:
                moonPhaseIconId = R.drawable.full_moon;
        }
        return moonPhaseIconId;
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
                            JSONArray daysArray = dailyWeather.getJSONArray("data");
                            ArrayList<WeatherData> daysWeather = WeatherData.buildWeeklyWeather(daysArray);

                            WeatherData todaysWeather = daysWeather.get(0);

                            // set moon icon
                            moonIcon.setImageResource(todaysWeather.moonPhaseIconId);
                            currentMoonLabel.setText("Current Moon Phase: "+todaysWeather.moonPhaseName);
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
