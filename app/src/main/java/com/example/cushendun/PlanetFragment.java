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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class PlanetFragment extends Fragment {

    View masterView = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        masterView = inflater.inflate(R.layout.planetlayout, viewGroup, false);

        // display visible planet info
        TextView planetsView = masterView.findViewById(R.id.planetsVisible);
        String night_sky_url = "https://www.timeanddate.com/astronomy/night/uk/belfast";
        updatePlanetData(night_sky_url, planetsView);

        return masterView;
    }

    private void updatePlanetData(String url, final TextView planetsView) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        class PlanetData {
            String name = "";
            String rise = "";
            String set = "";
            String meridian = "";
            String comment = "";

            public void showData() {
                String data = "Name: "+name+", rise: "+ rise+", set: "+set+", meridian: "+meridian+", comment: "+comment;
                System.out.println("Planet data= "+data);
            }
        }
        // Request a response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Document doc = Jsoup.parse(response);
                        Element table = doc.select("table").get(0); //select the first table.
                        Elements rows = table.select("tr");

                        ArrayList<PlanetData> planets = new ArrayList<PlanetData>();
                        for (int i = 0; i < rows.size(); i++) {
                            Element row = rows.get(i);
                            Elements cols = row.select("td");

                            Elements headers = row.select("th");

                            if ( cols.size() > 0) {
                                PlanetData planet = new PlanetData();
                                planet.name = headers.get(0).text();
                                planet.rise = cols.get(0).text();
                                planet.set = cols.get(1).text();
                                planet.meridian = cols.get(2).text();
                                planet.comment = cols.get(3).text();

                                if (planet.comment.contains("not visible") || planet.comment.contains("ifficult to see")) {
                                    // do not add
                                } else {
                                    planets.add(planet);
                                }
                            } else {
                                //System.out.println("no cols");
                            }
                        }

                        String planetText = "";
                        for (PlanetData planet : planets) {
                            planetText += planet.name + ":\n\tRises at: " + planet.rise + "\n\tSets at: "+planet.set + "\n\tMeridian at: "+planet.meridian+"\n\tComments: " + planet.comment+"\n\n";
                        }
                        planetsView.setText(planetText);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work! Failed getting planet data - "+error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
