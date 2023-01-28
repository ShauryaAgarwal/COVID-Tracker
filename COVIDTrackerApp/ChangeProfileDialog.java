package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ChangeProfileDialog extends DialogFragment {

    EditText changeProfileEditText;
    TextView changeProfileTextView;
    String textViewString, methodKey, newInformation;

    public ChangeProfileDialog(String tvt, String mk)
    {
        textViewString = tvt;
        methodKey = mk;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.activity_change_profile_dialog, null);
        changeProfileEditText = view.findViewById(R.id.id_changeProfileUserInputEditText);
        changeProfileTextView = view.findViewById(R.id.id_changeProfileTitleTextView);

        changeProfileTextView.setText(textViewString);

        changeProfileEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newInformation = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        builder.setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(methodKey.equalsIgnoreCase("Name"))
                        {
                            StartupActivity.user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newInformation).build())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("TAG_1", "User name updated.");
                                            }
                                        }
                                    });
                        }
                        else if(methodKey.equalsIgnoreCase("Email"))
                        {
                            StartupActivity.user.updateEmail(newInformation)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("TAG_1", "User email address updated.");
                                            }
                                            else {
                                                Log.d("TAG_1", "User email not updated (invalid email)");
                                            }
                                        }
                                    });
                        }
                        else if(methodKey.equalsIgnoreCase("Phone #"))
                        {
                            StartupActivity.user.updatePhoneNumber(PhoneAuthProvider.getCredential(StartupActivity.user.getProviderId(), "phoneNum"))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("TAG_1", "User phone number updated.");
                                            }
                                            else {
                                                Log.d("TAG_1", "User phone number not updated (invalid phone #)");
                                            }
                                        }
                                    });
                        }
                        else if(methodKey.equalsIgnoreCase("Password"))
                        {
                            StartupActivity.user.updatePassword(newInformation)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("TAG_1", "User password updated.");
                                            }
                                            else {
                                                Log.d("TAG_1", "User password not updated.");
                                            }
                                        }
                                    });
                        }
                        else if(methodKey.equalsIgnoreCase("Picture"))
                        {
                            StartupActivity.user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(newInformation)).build())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("TAG_1", "User profile picture updated.");
                                            }
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChangeProfileDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}