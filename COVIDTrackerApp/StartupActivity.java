package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StartupActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    static FirebaseUser user;
    Button signIn, nextButton, getDataButton;
    NumberPicker numPicker1, numPicker2, numPicker3;
    String CovidActNowAPIKey = "8320bb4d3ac34682a7145626dcc250e4", checkHistoricString = "";
    URL getStatesURL, getCountiesURL, getDataURL;
    JSONArray allStatesJSON, allCountiesJSON;
    static JSONObject finalDataJSON;
    String[] numPicker1Array, numPicker2Array;
    static TextView dataDisplayTextView;
    static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    static DatabaseReference ref = database.getReference();
    static DatabaseReference usersRef = ref.child("users");
    static Map<String, Object> users = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        signIn = findViewById(R.id.id_startupSignInButton);
        numPicker1 = findViewById(R.id.id_startupNumPicker1);
        numPicker2 = findViewById(R.id.id_startupNumPicker2);
        numPicker3 = findViewById(R.id.id_startupNumPicker3);
        nextButton = findViewById(R.id.id_startupNumPickerNextButton);
        getDataButton = findViewById(R.id.id_startupNumPickerFinalButton);
        dataDisplayTextView = findViewById(R.id.id_startupDataDisplayTextView);

        numPicker1Array = new String[]{"Current", "Historic"};
        numPicker1.setMinValue(0);
        numPicker1.setMaxValue(numPicker1Array.length - 1);
        numPicker1.setDisplayedValues(numPicker1Array);

        GetCurrentAllStatesDataTask allStatesDataTask = new GetCurrentAllStatesDataTask();
        allStatesDataTask.execute();
        getDataButton.setEnabled(false);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSignInIntent();
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
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());

        //new AuthUI.IdpConfig.PhoneBuilder().build(), new AuthUI.IdpConfig.GoogleBuilder().build(), new AuthUI.IdpConfig.FacebookBuilder().build(), new AuthUI.IdpConfig.TwitterBuilder().build()

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();
                if(user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp())
                {
                    /*
                    users.put(user.getUid(), "Saved Items");
                    usersRef.setValue(users);
                    Post testPost = new Post("a", "t");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("el1", "one");
                        jsonObject.put("el2", "oaisgfnhj");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("TAG_1", jsonObject.toString());
                     */
                    users.put(user.getUid(), new User(user.getEmail(), (new JSONObject()).toString()));
                    usersRef.updateChildren(users);
                    Log.d("TAG_1", "New User");
                }
                // ...
                Intent intent = new Intent(StartupActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    public static class User {
        public String email;
        public String json;
        public String test;

        public User(String email, String json) {
            this.email = email;
            this.json = json;
        }

        public User(String email, String json, String test)
        {
            this.email = email;
            this.json = json;
            this.test = test;
        }
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
            String[] numPicker3Array;
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
            } catch (IOException | JSONException e) {
                Log.d("TAG_A", "Error");
                e.printStackTrace();
            }

            numPicker3Array = new String[allCountiesJSON.length()];
            Log.d("TAG_2", String.valueOf(numPicker3Array.length));

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
                finalDataJSON = jsonObject;
                Log.d("TAG_1", finalDataJSON.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            if(checkHistoricString.equalsIgnoreCase(".timeseries"))
            {
                StartupHistoricDialog historicDateDialogFragment = new StartupHistoricDialog("Startup");
                historicDateDialogFragment.show(getSupportFragmentManager(), "historicDate");
            }
            else
            {
                startupDisplayData(false);
            }

            return jsonObject;
        }
    }

    public static void startupDisplayData(boolean checkHistoric)
    {
        JSONObject metricsObject = null, actualsObject = null, riskLevelsObject = null;
        String date = "";
        try {
            String country = finalDataJSON.getString("country");
            Log.d("TAG_1", country);
            String state = finalDataJSON.getString("state");
            Log.d("TAG_1", state);
            String county = finalDataJSON.getString("county");
            Log.d("TAG_1", county);
            int population = finalDataJSON.getInt("population");
            Log.d("TAG_1", String.valueOf(population));

            if(checkHistoric)
            {
                int historicIndex;
                for(int i = 0; i < finalDataJSON.getJSONArray("metricsTimeseries").length(); i++)
                {
                    if(finalDataJSON.getJSONArray("metricsTimeseries").getJSONObject(i).getString("date").equals(StartupHistoricDialog.startupUserHistoricDateInput))
                    {
                        historicIndex = i;
                        metricsObject = finalDataJSON.getJSONArray("metricsTimeseries").getJSONObject(historicIndex);
                        actualsObject = finalDataJSON.getJSONArray("actualsTimeseries").getJSONObject(historicIndex);
                        riskLevelsObject = finalDataJSON.getJSONArray("riskLevelsTimeseries").getJSONObject(historicIndex);
                        date = metricsObject.getString("date");
                    }
                }
            }
            else
            {
                metricsObject = finalDataJSON.getJSONObject("metrics");
                actualsObject = finalDataJSON.getJSONObject("actuals");
                riskLevelsObject = finalDataJSON.getJSONObject("riskLevels");
                date = finalDataJSON.getString("lastUpdatedDate");
            }

            if(metricsObject != null && actualsObject != null && riskLevelsObject != null)
            {
                //Log.d("TAG_1", metricsObject.toString());
                //Log.d("TAG_1", actualsObject.toString());
                //Log.d("TAG_1", riskLevelsObject.toString());

                String generalInfo = "Country: " + country + "\n\nState: " + state + "\n\nCounty: " + county + "\n\nPopulation: " + population + "\n\nDate: " + date;
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
                dataDisplayTextView.setText(generalInfo + "\n\n" + metricsInfo + "\n\n" + actualsInfo + "\n\n" + riskLevelsInfo);
            }
            else
            {
                dataDisplayTextView.setText("Data Not Received");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}