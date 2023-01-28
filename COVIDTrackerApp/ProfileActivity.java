package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    ImageView profilePageHomeIcon, profilePageStatsIcon, profilePageProfileIcon, mainAvatar;
    RecyclerView recyclerView;
    int[] profileItemImageIDs = {R.drawable.profileitemnameicon, R.drawable.profileitememailicon, R.drawable.profileitemphoneicon};
    String[] profileItemTitles = {"Name", "Email", "Phone #"};
    String[] profileItemValues = {StartupActivity.user.getDisplayName(), StartupActivity.user.getEmail(), StartupActivity.user.getPhoneNumber()};
    String[] profileItemChangeStrings = {"Input your new name below.", "Input your new email below.", "Input your new phone number below."};
    Button updatePassword, updateProfilePic, signOut, deleteUser;
    URL profilePicURL;
    Drawable profilePicDrawable;
    Bitmap profilePicBitmap;
    static boolean checkDeleteUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePageHomeIcon = findViewById(R.id.id_profilePageHomeIconImageView);
        profilePageStatsIcon = findViewById(R.id.id_profilePageStatsIconImageView);
        profilePageProfileIcon = findViewById(R.id.id_profilePageProfileIconImageView);
        mainAvatar = findViewById(R.id.id_profilePageDisplayAvatarImageView);
        recyclerView = findViewById(R.id.id_profileItemsRecyclerView);
        updatePassword = findViewById(R.id.id_updateProfilePasswordButton);
        updateProfilePic = findViewById(R.id.id_updateProfileImageButton);
        signOut = findViewById(R.id.id_profileSignOutButton);
        deleteUser = findViewById(R.id.id_updateProfileDeleteUserButton);

        profilePageHomeIcon.setImageResource(R.drawable.homebuttonicon);
        profilePageStatsIcon.setImageResource(R.drawable.statsbuttonicon);
        profilePageProfileIcon.setImageResource(R.drawable.profilebuttonicon);

        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < 3; i++)
            list.add(Integer.toString(i));

        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(this, list);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        if(StartupActivity.user.getPhotoUrl() != null)
        {
            GetProfilePictureBitmapTask profilePictureBitmapTask = new GetProfilePictureBitmapTask();
            profilePictureBitmapTask.execute();
            mainAvatar.setImageBitmap(profilePicBitmap);
            Log.d("TAG_1", StartupActivity.user.getPhotoUrl().toString());
        }
        else
        {
            mainAvatar.setImageResource(R.drawable.emptyprofileicon);
        }

        profilePageHomeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentProfile1 = new Intent(ProfileActivity.this, StartupActivity.class);
                finish();
            }
        });

        profilePageStatsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentProfile2 = new Intent(ProfileActivity.this, StatisticsActivity.class);
                finish();
                startActivity(intentProfile2);
            }
        });

        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeProfileDialog changePasswordDialogFragment = new ChangeProfileDialog("Input your new password below.", "Password");
                changePasswordDialogFragment.show(getSupportFragmentManager(), "changePassword");
            }
        });

        updateProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeProfileDialog changePictureDialogFragment = new ChangeProfileDialog("Enter the URL for your new image below.", "Picture");
                changePictureDialogFragment.show(getSupportFragmentManager(), "changePicture");
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                Intent intentProfile3 = new Intent(ProfileActivity.this, StartupActivity.class);
                finish();
                startActivity(intentProfile3);
                Toast signOutToast = Toast.makeText(getApplicationContext(), "Successfully Signed Out", Toast.LENGTH_SHORT);
                signOutToast.show();
            }
        });

        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteUserDialog deleteUserDialogFragment = new DeleteUserDialog();
                deleteUserDialogFragment.show(getSupportFragmentManager(), "deleteUser");
                Thread afterDeleteUserThread = new Thread(new Runnable() {
                    Handler afterDeleteUserHandler = new Handler();
                    @Override
                    public void run() {
                        while(true)
                        {
                            try {
                                Thread.sleep(500);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }

                            afterDeleteUserHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(checkDeleteUser)
                                    {
                                        checkDeleteUser = false;
                                        Intent intentProfile4 = new Intent(ProfileActivity.this, StartupActivity.class);
                                        finish();
                                        startActivity(intentProfile4);
                                        Toast deleteUserToast = Toast.makeText(getApplicationContext(), "Successfully Deleted User", Toast.LENGTH_SHORT);
                                        deleteUserToast.show();
                                    }
                                }
                            });
                        }
                    }
                });
                afterDeleteUserThread.start();
            }
        });
    }

    private class GetProfilePictureBitmapTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            JSONArray jsonArray = null;
            try {
                profilePicURL = new URL(StartupActivity.user.getPhotoUrl().toString());
            } catch (MalformedURLException e) {
                Log.d("TAG_1", "URL Error");
                e.printStackTrace();
            }

            try {
                URLConnection connection = profilePicURL.openConnection();
                InputStream inputStream = connection.getInputStream();
                //profilePicDrawable = Drawable.createFromStream(inputStream, null);
                //profilePicBitmap = ((BitmapDrawable)profilePicDrawable).getBitmap();
                profilePicBitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                Log.d("TAG_1", "URL Error");
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void deleteUser() {
        StartupActivity.user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG_1", "User account deleted.");
                        }
                    }
                });
    }

    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
        // [END auth_fui_signout]
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>
    {
        Context parentContext;
        ArrayList<String> list;

        public RecyclerAdapter(Context context, ArrayList<String> list) {
            parentContext = context;
            this.list = list;
        }

        @NonNull
        @Override
        public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parentContext).inflate(R.layout.holder_layout, parent, false);
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);
            return recyclerViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
            holder.imageView.setImageResource(profileItemImageIDs[position]);
            holder.textView1.setText(profileItemTitles[position] + ":");
            holder.textView2.setText(profileItemValues[position]);

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChangeProfileDialog changeProfileDialogFragment = new ChangeProfileDialog(profileItemChangeStrings[position], profileItemTitles[position]);
                    changeProfileDialogFragment.show(getSupportFragmentManager(), "changeProfileItem");
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class RecyclerViewHolder extends RecyclerView.ViewHolder
        {
            ImageView imageView;
            TextView textView1, textView2;
            Button editButton;

            public RecyclerViewHolder(@NonNull View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.id_profileItemImageView);
                textView1 = itemView.findViewById(R.id.id_profileItemTextView1);
                textView2 = itemView.findViewById(R.id.id_profileItemTextView2);
                editButton = itemView.findViewById(R.id.id_profileItemEditButton);
            }
        }
    }
}