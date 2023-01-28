package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    ImageView homePageHomeIcon, homePageStatsIcon, homePageProfileIcon;
    static ArrayList<StatisticsActivity.DataItem> dataItems = new ArrayList<>();
    LinearLayout homeLinearLayout;
    static boolean checkUpdatedDataButton = false;
    String loginJsonString = "_";
    boolean initialLoginCheck = true;
    static JSONObject userDataJSON;
    DatabaseReference loginUserReference = StartupActivity.usersRef.child(StartupActivity.user.getUid());

    @Override
    protected void onPause() {
        Map<String, Object> userJSONUpdates = new HashMap<>();
        userJSONUpdates.put(StartupActivity.user.getUid(), new StartupActivity.User(StartupActivity.user.getEmail(), userDataJSON.toString(), null));
        StartupActivity.usersRef.updateChildren(userJSONUpdates);
        Log.d("TAG_1", "Home Activity Paused");
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homePageHomeIcon = findViewById(R.id.id_homePageHomeIconImageView);
        homePageStatsIcon = findViewById(R.id.id_homePageStatsIconImageView);
        homePageProfileIcon = findViewById(R.id.id_homePageProfileIconImageView);
        homeLinearLayout = findViewById(R.id.id_homeLinearLayout);

        homePageHomeIcon.setImageResource(R.drawable.homebuttonicon);
        homePageStatsIcon.setImageResource(R.drawable.statsbuttonicon);
        homePageProfileIcon.setImageResource(R.drawable.profilebuttonicon);

        loginUserReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(initialLoginCheck)
                {
                    loginJsonString = snapshot.child("json").getValue(String.class);
                    Log.d("TAG_A", loginJsonString);
                    if(loginJsonString.equalsIgnoreCase("{}"))
                    {
                        Log.d("TAG_B", "Empty JSON Object Received");
                        userDataJSON = new JSONObject();
                    }
                    else
                    {
                        try {
                            userDataJSON = new JSONObject(loginJsonString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for(int i = 0; i < userDataJSON.length(); i++)
                        {
                            Button savedDataButton = new Button(HomeActivity.this);
                            savedDataButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            try {
                                savedDataButton.setText(userDataJSON.getJSONObject(String.valueOf(i)).get("Date") + ": " + userDataJSON.getJSONObject(String.valueOf(i)).get("County"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            savedDataButton.setId(View.generateViewId());
                            savedDataButton.setTextColor(Color.BLACK);
                            int finalI = i;
                            savedDataButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HomeDataDialog homeDataDialogFragment = null;
                                    try {
                                        homeDataDialogFragment = new HomeDataDialog((String) userDataJSON.getJSONObject(String.valueOf(finalI)).get("Data"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    homeDataDialogFragment.show(getSupportFragmentManager(), "homeDataButtonPressed");
                                }
                            });
                            homeLinearLayout.addView(savedDataButton);
                        }
                    }
                    Log.d("TAG_B", userDataJSON.toString());
                    Log.d("TAG_B", String.valueOf(HomeActivity.userDataJSON.length()));
                    initialLoginCheck = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG_B", "Failed to read value.", error.toException());
            }
        });
        Map<String, Object> loginJSONUpdates = new HashMap<>();
        loginJSONUpdates.put("login", "test");
        loginUserReference.updateChildren(loginJSONUpdates);

        homePageStatsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentHome1 = new Intent(HomeActivity.this, StatisticsActivity.class);
                startActivity(intentHome1);
            }
        });

        homePageProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentHome2 = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intentHome2);
            }
        });

        Thread checkSavedDataUpdate = new Thread(new Runnable() {
            Handler checkSavedDataUpdateHandler = new Handler();
            @Override
            public void run() {
                while(true)
                {
                    try {
                        Thread.sleep(500);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    checkSavedDataUpdateHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(checkUpdatedDataButton)
                            {
                                Button savedDataButton = new Button(HomeActivity.this);
                                savedDataButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                savedDataButton.setText(StatisticsActivity.statisticsDisplayDataItemDate + ": " + StatisticsActivity.statisticsDisplayDataItemCountyName);
                                savedDataButton.setId(dataItems.size()-1);
                                savedDataButton.setTextColor(Color.BLACK);
                                savedDataButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.d("TAG_1", String.valueOf(savedDataButton.getId()));
                                        Log.d("TAG_1", dataItems.get(savedDataButton.getId()).date);
                                        HomeDataDialog homeDataDialogFragment = new HomeDataDialog(dataItems.get(savedDataButton.getId()).data);
                                        homeDataDialogFragment.show(getSupportFragmentManager(), "homeDataButtonPressed");
                                    }
                                });
                                homeLinearLayout.addView(savedDataButton);
                                checkUpdatedDataButton = false;
                            }
                        }
                    });
                }
            }
        });

        checkSavedDataUpdate.start();
    }
}