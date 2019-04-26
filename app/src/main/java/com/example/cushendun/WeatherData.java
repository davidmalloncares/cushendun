package com.example.cushendun;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class WeatherData {
    String weatherIcon = "";
    int weatherIconId;
    String summary = "";
    String highTemp;
    String lowTemp;
    String sunRise;
    String sunSet;
    String chanceOfPrecipitation;
    String precipitationType;
    String precipitationTime;
    String windGust;
    String windBearing;
    long windDir = 0;
    String windCompassBearing;
    String shortDate;
    int moonPhaseIconId;
    String moonPhaseName;

    public WeatherData(JSONObject json) {
        try {
            weatherIcon = json.getString("icon");

            // get summary for the day
            summary = json.getString("summary");

            // get the temp range
            highTemp = roundNumber(json.getString("temperatureHigh"));
            lowTemp = roundNumber(json.getString("temperatureLow"));

            // get the time
            long todayEpoch = Long.parseLong( json.getString("time") );
            Date daysDate = new Date( todayEpoch * 1000 );
            SimpleDateFormat dayFormatter = new SimpleDateFormat("EEE dd");
            shortDate = dayFormatter.format(daysDate) + getDayNumberSuffix(daysDate);

            // format date/times
            long sunRiseEpoch = Long.parseLong( json.getString("sunriseTime") );
            Date sunriseTime = new Date( sunRiseEpoch   * 1000 );
            long sunSetEpoch = Long.parseLong( json.getString("sunsetTime") );
            Date sunsetTime = new Date( sunSetEpoch * 1000 );

            SimpleDateFormat formatter= new SimpleDateFormat("h:mm:ss a");
            sunRise = formatter.format(sunriseTime);
            sunSet = formatter.format(sunsetTime);

            // chance of precipitation
            SimpleDateFormat shortTimeFormat= new SimpleDateFormat("hh a");
            long precipEpoch = Long.parseLong( json.getString("precipIntensityMaxTime") );
            Date precipTime = new Date( precipEpoch   * 1000 );
            float chanceOfPrec = Float.parseFloat(json.getString("precipProbability"));
            chanceOfPrec = chanceOfPrec * 100;
            chanceOfPrecipitation = roundNumber(Float.toString(chanceOfPrec));
            precipitationType = json.getString("precipType");
            precipitationTime = shortTimeFormat.format(precipTime);

            // wind
            windGust = roundNumber(json.getString("windGust"));
            windBearing = json.getString("windBearing");
            windDir = Long.parseLong(windBearing);
            if (windDir < 180) {
                windDir = windDir+ 180;
            } else {
                long diff = 360 - windDir;
                windDir = 0 + (180 - diff);
            }

            windCompassBearing = getCompassBearing(windBearing);

            // moon data
            float moonPhase = Float.parseFloat(json.getString("moonPhase"));
            moonPhaseIconId = R.drawable.waning_crescent;
            if ((moonPhase >= 0 && moonPhase < 0.125) || moonPhase == 1.0) {
                moonPhaseIconId = R.drawable.newmoon;
                moonPhaseName = "New Moon";
            } else if (moonPhase >= 0.125 && moonPhase < 0.25) {
                moonPhaseIconId = R.drawable.waxing_crescent;
                moonPhaseName = "Waxing Crescent";
            } else if (moonPhase >= 0.25 && moonPhase < 0.36) {
                moonPhaseIconId = R.drawable.first_quarter;
                moonPhaseName = "First Quarter";
            } else if (moonPhase >= 0.36 && moonPhase < 0.5) {
                moonPhaseIconId = R.drawable.waxing_gibbous;
                moonPhaseName = "Waxing Gibbous";
            } else if (moonPhase >= 0.5  && moonPhase < 0.625) {
                moonPhaseIconId = R.drawable.full_moon;
                moonPhaseName = "Full Moon";
            } else if (moonPhase >= 0.625 && moonPhase < 0.74) {
                moonPhaseIconId = R.drawable.waning_gibbous;
                moonPhaseName = "Waning Gibbous";
            } else if (moonPhase >= 0.74  && moonPhase < 0.86) {
                moonPhaseIconId = R.drawable.third_quarter;
                moonPhaseName = "Last Quarter";
            } else {
                moonPhaseIconId = R.drawable.waning_crescent;
                moonPhaseName = "Waning Crescent";
            }

            //System.out.println("moonPhase="+moonPhase+", moonPhaseName="+moonPhaseName + ", for "+daysDate);
        } catch (JSONException e) {
            System.out.println("Failed getting weather data - " +e.getMessage());
        }

        switch(weatherIcon) {
            case "cloud":
                weatherIconId = R.drawable.cloud;
                break;
            case "fog":
                weatherIconId = R.drawable.fog;
                break;
            case "wind":
                weatherIconId = R.drawable.wind;
                break;
            case "snow":
                weatherIconId = R.drawable.snowflake;
                break;
            case "sleet":
                weatherIconId = R.drawable.sleet;
                break;
            case "rain":
                weatherIconId = R.drawable.rain_cloud;
                break;
            case "clear-day":
                weatherIconId = R.drawable.hot_sun_day;
                break;
            case "clear-night":
                weatherIconId = R.drawable.clear_night;
                break;
            case "partly-cloudy-day":
                weatherIconId = R.drawable.sunny_sun_cloudy;
                break;
            case "partly-cloudy-night":
                weatherIconId = R.drawable.partly_cloudy;
                break;
            default:
                weatherIconId = R.drawable.cloud;
        }
    }

    public void showData() {
        String data = "TODO: output data";
        System.out.println("Weather data= "+data);
    }


    private String getDayNumberSuffix(Date date) {
        // get the day part
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return getDayNumberSuffixFromDay(day);
    }

    public static String getDayNumberSuffixFromDay(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    private String roundNumber(String number) {
        float lngVersion = Float.parseFloat(number);
        int intVersion = Math.round(lngVersion);
        return Integer.toString(intVersion);
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


    static ArrayList<WeatherData> buildWeeklyWeather(JSONArray weeklyData) {
        ArrayList<WeatherData> weeklyList = new ArrayList<>();

        if (weeklyData != null && weeklyData.length() > 0) {
            for (int i = 0; i < weeklyData.length(); i++) {
                JSONObject day = null;
                try {
                    day = (JSONObject) weeklyData.get(i);
                    weeklyList.add(new WeatherData(day));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return weeklyList;
    }
}
