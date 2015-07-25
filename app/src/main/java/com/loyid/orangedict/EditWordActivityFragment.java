package com.loyid.orangedict;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class EditWordActivityFragment extends Fragment {
    private static final String TAG = EditWordActivityFragment.class.getSimpleName();

    private EditText mEditTextWord = null;
    private LinearLayout mMeanContainer = null;
    private ScrollView mScrollViewGroup = null;

    public EditWordActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout rl = (RelativeLayout)inflater.inflate(R.layout.fragment_edit_word, container, false);

        mEditTextWord = (EditText)rl.findViewById(R.id.edittext_word);
        mMeanContainer = (LinearLayout)rl.findViewById(R.id.mean_container);
        mScrollViewGroup = (ScrollView)rl.findViewById(R.id.scrollview_mean_group);

        Button addButton = (Button)rl.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMeanItem(0, null);
            }
        });

        recalculateIndexes();
        return rl;
    }

    private void addMeanItem(int type, String mean) {
        View item = getActivity().getLayoutInflater().inflate(R.layout.mean_item_container, null);
        final int count = mMeanContainer.getChildCount();
        ImageButton removeButton = (ImageButton)item.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMeanItem((int) v.getTag());
            }
        });
        removeButton.setTag(count);
        mMeanContainer.addView(item);
        EditText editTextMean = (EditText)item.findViewById(R.id.edittext_mean);
        mMeanContainer.requestChildFocus(item, editTextMean);
        editTextMean.requestFocus();
        recalculateIndexes();
    }

    private void removeMeanItem(int index) {
        if (mMeanContainer.getChildCount() == 0) {
            resetMeanItem(mMeanContainer.getChildAt(0));
        } else {
            mMeanContainer.removeViewAt(index);
            recalculateIndexes();
        }
    }

    private void resetMeanItem(View view) {
        EditText mean = (EditText)view.findViewById(R.id.edittext_mean);
        mean.setText("");
        Spinner sp = (Spinner)view.findViewById(R.id.spinner_type);
        sp.setSelection(0);
    }

    private void recalculateIndexes() {
        for (int i = 0; i < mMeanContainer.getChildCount(); i++) {
            View item = mMeanContainer.getChildAt(i);
            ImageButton removeButton = (ImageButton)item.findViewById(R.id.remove_button);
            if (removeButton.getTag() == null) {
                removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeMeanItem((int)v.getTag());
                    }
                });
            }
            removeButton.setTag(i);
            TextView index = (TextView)item.findViewById(R.id.label_no);
            index.setText(String.valueOf(i+1));
        }
    }
}
