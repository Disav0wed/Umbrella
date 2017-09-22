package com.umbrella.app.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.umbrella.R;

/**
 * Created by ibrahimserpici on 9/22/17.
 */

public class DialogBuilder extends DialogFragment {

    private String title;
    private String message;

    private DialogInterface.OnClickListener positiveButtonOnClickListener;

    public static DialogBuilder newInstance(Context context) {
        return new DialogBuilder();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", positiveButtonOnClickListener);

        return alertDialogBuilder.create();
    }

    public void setTitle(String titleString) {
        this.title = titleString;
    }

    public void setMessage(String messageString) {
        this.message = messageString;
    }

    /** Sets an OnClickListener which is setted from the class which created the dialog instance */
    public void setPositiveButtonOnClickListener(DialogInterface.OnClickListener positiveButtonOnClickListener) {
        this.positiveButtonOnClickListener = positiveButtonOnClickListener;
    }
}
