package com.loyid.orangedict;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by Loyid on 2015-08-18.
 */
public class DialogImplement extends DialogFragment {
    private static final String TAG = DialogImplement.class.getSimpleName();

    private CharSequence mTitle;
    private CharSequence mMessage;
    private CharSequence mPositiveButtonText;
    private DialogInterface.OnClickListener mPositiveButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Default listener;
        }
    };
    private CharSequence mNegativeButtonText;
    private DialogInterface.OnClickListener mNegativeButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Default listener
        }
    };
    private int mIconId = 0;
    private Drawable mIcon;

    public void setTitle(CharSequence text) {
        mTitle = text;
    }

    public void setMessage(CharSequence text) {
        mMessage = text;
    }

    public void setPositiveButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        mPositiveButtonText = text;
        mPositiveButtonListener = listener;
    }

    public void setNegativeButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        mNegativeButtonText = text;
        mNegativeButtonListener = listener;
    }

    public void setIconId(int id) {
        mIconId = id;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (mTitle != null) {
            builder.setTitle(mTitle);
        }
        if (mMessage != null) {
            builder.setMessage(mMessage);
        }
        if (mPositiveButtonText != null) {
            builder.setPositiveButton(mPositiveButtonText, mPositiveButtonListener);
        }
        if (mNegativeButtonText != null) {
            builder.setNegativeButton(mNegativeButtonText, mNegativeButtonListener);
        }
        if (mIcon != null) {
            builder.setIcon(mIcon);
        }
        if (mIconId >= 0) {
            builder.setIcon(mIconId);
        }

        return builder.create();
    }
}
