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
        String night_sky_url = "https://www.timeanddate.com/astronomy/night/uk/belfast";
        updatePlanetData(night_sky_url);

        return masterView;
    }

    private void updatePlanetData(String url) {
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
                                planet.rise = splitOverTwoLines(cols.get(0).text());
                                planet.set = splitOverTwoLines(cols.get(1).text());
                                planet.meridian = splitOverTwoLines(cols.get(2).text());
                                planet.comment = cols.get(3).text();

                                planets.add(planet);
                            } else {
                                //System.out.println("no cols");
                            }
                        }

                        String planetText = "";
                        for (PlanetData planet : planets) {
                            planetText += planet.name + ":\n\tRises at: " + planet.rise + "\n\tSets at: "+planet.set + "\n\tMeridian at: "+planet.meridian+"\n\tComments: " + planet.comment+"\n\n";
                        }

                        for (int i = 0; i < planets.size(); i++) {
                            // set icon
                            int iconId = getResources().getIdentifier(
                                    "planetIcon"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            ImageView planetIcon = masterView.findViewById(iconId);
                            planetIcon.setImageResource(getPlanetIconId(planets.get(i).name));
                            planetIcon.setMaxHeight(70);
                            planetIcon.setMaxWidth(70);
                            planetIcon.setAdjustViewBounds(true);
                            planetIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            // set name
                            int planetNameId = getResources().getIdentifier(
                                    "planetName"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            TextView planetNameView = masterView.findViewById(planetNameId);
                            planetNameView.setText(planets.get(i).name);

                            // set rise time
                            int planetRiseId = getResources().getIdentifier(
                                    "planetRise"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            TextView planetRiseView = masterView.findViewById(planetRiseId);
                            planetRiseView.setText(planets.get(i).rise);

                            // set set time
                            int planetSetId = getResources().getIdentifier(
                                    "planetSet"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            TextView planetSetView = masterView.findViewById(planetSetId);
                            planetSetView.setText(planets.get(i).set);

                            // set meridian time
                            int planetMeridianId = getResources().getIdentifier(
                                    "planetMeridian"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            TextView planetMeridianView = masterView.findViewById(planetMeridianId);
                            planetMeridianView.setText(planets.get(i).meridian);

                            // set comments
                            int planetCommentId = getResources().getIdentifier(
                                    "planetComments"+i,
                                    "id",
                                    getActivity().getPackageName()
                            );
                            TextView planetCommentView = masterView.findViewById(planetCommentId);
                            planetCommentView.setText(planets.get(i).comment);
                        }
                    }

                    private String splitOverTwoLines(String text) {
                        String[] splitText = text.split(" ");
                        String withNewLines = "";
                        for (String line : splitText) {
                            withNewLines += line + "\n";
                        }
                        return withNewLines;
                    }

                    private int getPlanetIconId(String planetName) {
                        int planetIconId;
                        switch(planetName) {
                            case "Mercury":
                                planetIconId = R.drawable.mercury;
                                break;
                            case "Mars":
                                planetIconId = R.drawable.mars;
                                break;
                            case "Venus":
                                planetIconId = R.drawable.venus;
                                break;
                            case "Jupiter":
                                planetIconId = R.drawable.jupiter;
                                break;
                            case "Saturn":
                                planetIconId = R.drawable.saturn;
                                break;
                            case "Uranus":
                                planetIconId = R.drawable.uranus;
                                break;
                            case "Neptune":
                                planetIconId = R.drawable.neptune;
                                break;
                            default:
                                planetIconId = R.drawable.mars;
                        }
                        return planetIconId;
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
