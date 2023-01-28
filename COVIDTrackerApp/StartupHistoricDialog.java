package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class StartupHistoricDialog extends DialogFragment{

    static String startupUserHistoricDateInput = "";
    String tempUserHistoricDateInput, classKey;
    EditText startupHistoricDateInputEditText;

    public StartupHistoricDialog(String key)
    {
        classKey = key;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.activity_startup_historic_dialog, null);
        startupHistoricDateInputEditText = view.findViewById(R.id.id_startupHistoricDialogEditText);

        startupHistoricDateInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tempUserHistoricDateInput = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        builder.setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        startupUserHistoricDateInput = tempUserHistoricDateInput;
                        if(classKey.equalsIgnoreCase("Startup"))
                        {
                            StartupActivity.startupDisplayData(true);
                        }
                        else if(classKey.equalsIgnoreCase("Statistics"))
                        {
                            StatisticsActivity.statisticsDisplayData(true);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        StartupHistoricDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}