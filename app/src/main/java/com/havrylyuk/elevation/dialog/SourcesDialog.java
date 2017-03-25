package com.havrylyuk.elevation.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.havrylyuk.elevation.R;
import com.havrylyuk.elevation.util.SourceType;
import com.havrylyuk.elevation.util.PreferencesHelper;

/**
 * Created by Igor Havrylyuk on 25.03.2017.
 */

public class SourcesDialog extends DialogFragment {

    public static final String SOURCE_DIALOG_TAG = "SOURCE_DIALOG_TAG" ;
    private int checkedItem;

    public static SourcesDialog newInstance() {
        return new SourcesDialog() ;
    }

    public SourcesDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final PreferencesHelper prefHelper = PreferencesHelper.getInstance();
        checkedItem = prefHelper.getApiType(getActivity());
        builder.setTitle(R.string.dialog_source_title)
        .setSingleChoiceItems(SourceType.names(), checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedItem = which;
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prefHelper.setApiType(getActivity(), checkedItem);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }

}
