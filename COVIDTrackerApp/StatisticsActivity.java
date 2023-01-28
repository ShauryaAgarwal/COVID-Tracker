package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {

    ImageView statsPageHomeIcon, statsPageStatsIcon, statsPageProfileIcon;
    NumberPicker numPicker1, numPicker2, numPicker3;
    String CovidActNowAPIKey = "8320bb4d3ac34682a7145626dcc250e4", checkHistoricString = "";
    static String statisticsDisplayDataItemDate = "", statisticsDisplayDataItemCountyName = "";
    URL getStatesURL, getCountiesURL, getDataURL;
    Button nextButton, getDataButton;
    static Button saveDataButton;
    JSONArray allStatesJSON, allCountiesJSON;
    static JSONObject statsFinalDataJSON;
    String[] numPicker1Array, numPicker2Array, numPicker3Array;
    static TextView statsDataDisplayTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        statsPageHomeIcon = findViewById(R.id.id_statsPageHomeIconImageView);
        statsPageStatsIcon = findViewById(R.id.id_statsPageStatsIconImageView);
        statsPageProfileIcon = findViewById(R.id.id_statsPageProfileIconImageView);
        numPicker1 = findViewById(R.id.id_statisticsNumPicker1);
        numPicker2 = findViewById(R.id.id_statisticsNumPicker2);
        numPicker3 = findViewById(R.id.id_statisticsNumPicker3);
        nextButton = findViewById(R.id.id_statisticsNumPickerNextButton);
        getDataButton = findViewById(R.id.id_statisticsNumPickerFinalButton);
        saveDataButton = findViewById(R.id.id_statisticsSaveDataButton);
        statsDataDisplayTextView = findViewById(R.id.id_statisticsDataDisplayTextView);

        statsPageHomeIcon.setImageResource(R.drawable.homebuttonicon);
        statsPageStatsIcon.setImageResource(R.drawable.statsbuttonicon);
        statsPageProfileIcon.setImageResource(R.drawable.profilebuttonicon);

        numPicker1Array = new String[]{"Current", "Historic"};
        numPicker1.setMinValue(0);
        numPicker1.setMaxValue(numPicker1Array.length - 1);
        numPicker1.setDisplayedValues(numPicker1Array);

        GetCurrentAllStatesDataTask allStatesDataTask = new GetCurrentAllStatesDataTask();
        allStatesDataTask.execute();
        getDataButton.setEnabled(false);
        //saveDataButton.setEnabled(false);

        statsPageHomeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentStats1 = new Intent(StatisticsActivity.this, StartupActivity.class);
                finish();
            }
        });

        statsPageProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentStats2 = new Intent(StatisticsActivity.this, ProfileActivity.class);
                finish();
                startActivity(intentStats2);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetCurrentAllCountiesInStateDataTask allCountiesInStateDataTask = new GetCurrentAllCountiesInStateDataTask();
                allCountiesInStateDataTask.execute();
                getDataButton.setEnabled(true);
            }
        });

        getDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetFinalDataTask finalDataTask = new GetFinalDataTask();
                finalDataTask.execute();
            }
        });

        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if((statsDataDisplayTextView.getText()).length() > 30)
                {
                    HomeActivity.dataItems.add(new DataItem(statisticsDisplayDataItemDate, (String) statsDataDisplayTextView.getText(), statisticsDisplayDataItemCountyName));
                    //Log.d("TAG_1", HomeActivity.dataItems.get(0).date);
                    try {
                        HomeActivity.userDataJSON.put(String.valueOf(HomeActivity.userDataJSON.length()), new JSONObject("{\"Date\": \"" + HomeActivity.dataItems.get(HomeActivity.dataItems.size()-1).date
                                + "\", \"Data\": \"" + HomeActivity.dataItems.get(HomeActivity.dataItems.size()-1).data + "\", \"County\": \"" + HomeActivity.dataItems.get(HomeActivity.dataItems.size()-1).county + "\"}"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    HomeActivity.checkUpdatedDataButton = true;
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid Data -- Cannot Save", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    private class GetCurrentAllStatesDataTask extends AsyncTask<String, Void, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(String... strings) {
            JSONArray jsonArray = null;
            try {
                getStatesURL = new URL("https://api.covidactnow.org/v2/states.json?apiKey=" + CovidActNowAPIKey);
            } catch (MalformedURLException e) {
                Log.d("TAG_1", "URL Error");
                e.printStackTrace();
            }

            try {
                URLConnection connection = getStatesURL.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader inputBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp = inputBufferedReader.readLine();
                jsonArray = new JSONArray(temp);
                Log.d("TAG_1", jsonArray.toString());
                allStatesJSON = jsonArray;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            numPicker2Array = new String[53];
            for(int i = 0; i < 53; i++)
            {
                try {
                    numPicker2Array[i] = (String) allStatesJSON.getJSONObject(i).get("state");
                    Log.d("TAG_1", (String) allStatesJSON.getJSONObject(i).get("state"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            numPicker2.setMinValue(0);
            numPicker2.setMaxValue(numPicker2Array.length - 1);
            numPicker2.setDisplayedValues(numPicker2Array);

            return jsonArray;
        }
    }

    private class GetCurrentAllCountiesInStateDataTask extends AsyncTask<String, Void, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(String... strings) {
            JSONArray jsonArray = null;
            try {
                getCountiesURL = new URL("https://api.covidactnow.org/v2/county/" + numPicker2Array[numPicker2.getValue()] + ".json?apiKey=" + CovidActNowAPIKey);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                URLConnection connection = getCountiesURL.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader inputBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp = inputBufferedReader.readLine();
                jsonArray = new JSONArray(temp);
                //Log.d("TAG_1", jsonArray.toString());
                allCountiesJSON = jsonArray;
                numPicker3Array = new String[allCountiesJSON.length()];
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            for(int i = 0; i < numPicker3Array.length; i++)
            {
                try {
                    numPicker3Array[i] = (String) allCountiesJSON.getJSONObject(i).get("county");
                    Log.d("TAG_1", String.valueOf(i));
                    Log.d("TAG_1", (String) allCountiesJSON.getJSONObject(i).get("county"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            numPicker3.setDisplayedValues(null);
            numPicker3.setMinValue(0);
            numPicker3.setMaxValue(numPicker3Array.length-1);
            numPicker3.setDisplayedValues(numPicker3Array);

            return jsonArray;
        }
    }

    private class GetFinalDataTask extends AsyncTask<String, Void, JSONObject>
    {
        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject jsonObject = null;
            try {
                if(numPicker1Array[numPicker1.getValue()].equalsIgnoreCase("Historic"))
                {
                    checkHistoricString = ".timeseries";
                }
                getDataURL = new URL("https://api.covidactnow.org/v2/county/" + allCountiesJSON.getJSONObject(numPicker3.getValue()).get("fips") + checkHistoricString + ".json?apiKey=" + CovidActNowAPIKey);
            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }

            try {
                URLConnection connection = getDataURL.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader inputBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp = inputBufferedReader.readLine();
                jsonObject = new JSONObject(temp);
                statsFinalDataJSON = jsonObject;
                Log.d("TAG_1", statsFinalDataJSON.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            if(checkHistoricString.equalsIgnoreCase(".timeseries"))
            {
                StartupHistoricDialog statsHistoricDateDialogFragment = new StartupHistoricDialog("Statistics");
                statsHistoricDateDialogFragment.show(getSupportFragmentManager(), "statsHistoricDate");
            }
            else
            {
                statisticsDisplayData(false);
            }

            return jsonObject;
        }
    }

    public static void statisticsDisplayData(boolean checkHistoric)
    {
        JSONObject metricsObject = null, actualsObject = null, riskLevelsObject = null;
        try {
            String country = statsFinalDataJSON.getString("country");
            Log.d("TAG_1", country);
            String state = statsFinalDataJSON.getString("state");
            Log.d("TAG_1", state);
            statisticsDisplayDataItemCountyName = statsFinalDataJSON.getString("county");
            Log.d("TAG_1", statisticsDisplayDataItemCountyName);
            int population = statsFinalDataJSON.getInt("population");
            Log.d("TAG_1", String.valueOf(population));

            if(checkHistoric)
            {
                int historicIndex;
                for(int i = 0; i < statsFinalDataJSON.getJSONArray("metricsTimeseries").length(); i++)
                {
                    if(statsFinalDataJSON.getJSONArray("metricsTimeseries").getJSONObject(i).getString("date").equals(StartupHistoricDialog.startupUserHistoricDateInput))
                    {
                        historicIndex = i;
                        metricsObject = statsFinalDataJSON.getJSONArray("metricsTimeseries").getJSONObject(historicIndex);
                        actualsObject = statsFinalDataJSON.getJSONArray("actualsTimeseries").getJSONObject(historicIndex);
                        riskLevelsObject = statsFinalDataJSON.getJSONArray("riskLevelsTimeseries").getJSONObject(historicIndex);
                        statisticsDisplayDataItemDate = metricsObject.getString("date");
                    }
                }
            }
            else
            {
                metricsObject = statsFinalDataJSON.getJSONObject("metrics");
                actualsObject = statsFinalDataJSON.getJSONObject("actuals");
                riskLevelsObject = statsFinalDataJSON.getJSONObject("riskLevels");
                statisticsDisplayDataItemDate = statsFinalDataJSON.getString("lastUpdatedDate");
            }

            if(metricsObject != null && actualsObject != null && riskLevelsObject != null)
            {
                //Log.d("TAG_1", metricsObject.toString());
                //Log.d("TAG_1", actualsObject.toString());
                //Log.d("TAG_1", riskLevelsObject.toString());

                String generalInfo = "Country: " + country + "\n\nState: " + state + "\n\nCounty: " + statisticsDisplayDataItemCountyName + "\n\nPopulation: " + population + "\n\nDate: " + statisticsDisplayDataItemDate;
                String metricsInfo = "Case Density (# of cases per 100k population calculated using a 7-day rolling average): " + metricsObject.get("caseDensity")
                        + "\n\nInfection Rate (estimated # of infections arising from a typical case): " + metricsObject.get("infectionRate")
                        + "\n\nTest Positivity Ratio (ratio of people who test positive calculated using a 7-day rolling average): " + metricsObject.get("testPositivityRatio");
                String actualsInfo = "Cases (cumulative confirmed or suspected cases): " + actualsObject.get("cases")
                        + "\n\nNew Cases (new confirmed or suspected cases): " + actualsObject.get("newCases")
                        + "\n\nDeaths (cumulative deaths that are suspected or confirmed to have been caused by COVID-19): " + actualsObject.get("deaths")
                        + "\n\nNew Deaths (new confirmed deaths): " + actualsObject.get("newDeaths")
                        + "\n\nPositive Tests (cumulative positive test results to date): " + actualsObject.get("positiveTests")
                        + "\n\nNegative Tests (cumulative negative test results to date): " + actualsObject.get("negativeTests");
                String riskLevelsInfo = "Overall Risk Level: " + riskLevelsObject.get("overall");
                statsDataDisplayTextView.setText(generalInfo + "\n\n" + metricsInfo + "\n\n" + actualsInfo + "\n\n" + riskLevelsInfo);
                //saveDataButton.setEnabled(true);
            }
            else
            {
                statsDataDisplayTextView.setText("Data Not Received");
                //saveDataButton.setEnabled(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class DataItem
    {
        String date, data, county;

        public DataItem(String d1, String d2, String c)
        {
            date = d1;
            data = d2;
            county = c;
        }
    }
}