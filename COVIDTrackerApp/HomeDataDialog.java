package com.example.finalproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class HomeDataDialog extends DialogFragment {

    TextView homeDataDisplayTextView;
    String data;

    public HomeDataDialog(String dt)
    {
        data = dt;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.activity_home_data_dialog, null);
        homeDataDisplayTextView = view.findViewById(R.id.id_homeDataDialogDisplayTextView);
        homeDataDisplayTextView.setText(data);

        builder.setView(view)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HomeDataDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}