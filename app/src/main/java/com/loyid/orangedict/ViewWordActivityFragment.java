package com.loyid.orangedict;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loyid.orangedict.database.ProviderContract;
import com.loyid.orangedict.util.Utils;


/**
 * A placeholder fragment containing a simple view.
 */
public class ViewWordActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ViewWordActivityFragment.class.getSimpleName();

    private static final int GRAMMAR_LOADER = 0;

    private long mGrammarId = -1;

    private TextView mGrammar;
    private TextView mSummary;
    private TextView mMeanings;

    public ViewWordActivityFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GRAMMAR_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        Log.d(TAG, "onActivityCreated hasGrammarId = " + getActivity().getIntent().hasExtra("grammar_id") + " grammar_id = " + getActivity().getIntent().getLongExtra("grammar_id", -1));
        mGrammarId = getActivity().getIntent().getLongExtra("grammar_id", -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_word, container, false);
        mGrammar = (TextView)view.findViewById(R.id.textview_word);
        mSummary = (TextView)view.findViewById(R.id.summary_of_meanings);
        mMeanings = (TextView)view.findViewById(R.id.meanings);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            deleteGrammar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = ProviderContract.Grammars._ID + " = ?";
        String[] selectionArgs = { String.valueOf(mGrammarId) };

        String[] projection = {
                ProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
                ProviderContract.Grammars.COLUMN_NAME_SUMMARY
        };
        Uri uri = ContentUris.withAppendedId(ProviderContract.Grammars.CONTENT_URI, mGrammarId);
        return new CursorLoader(getActivity(), ProviderContract.Grammars.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int grammarColumnIndex = cursor.getColumnIndex(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
        int summaryColumnIndex = cursor.getColumnIndex(ProviderContract.Grammars.COLUMN_NAME_SUMMARY);

        Log.d(TAG, "onLoadFinished grammarColumnIndex = " + grammarColumnIndex + " summaryColumnIndex = " + summaryColumnIndex);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String grammar = cursor.getString(grammarColumnIndex);
                String summary = cursor.getString(summaryColumnIndex);

                mGrammar.setText(grammar);
                String summaryStr = summary.replace(Utils.IDENTIFIER_MEANING_GROUP, ", -");
                summaryStr = summaryStr.replace(Utils.IDENTIFIER_MEANING, ":");
                mSummary.setText(summaryStr);

                String[] meaningGroup = summary.split(Utils.IDENTIFIER_MEANING_GROUP);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < meaningGroup.length; i++) {
                    String[] splits = meaningGroup[i].split(Utils.IDENTIFIER_MEANING);
                    int type = Integer.valueOf(splits[0]);
                    String mean = splits[1];
                    sb.append(" - " + splits[0] + " : " + splits[1]);
                    if (i < meaningGroup.length - 1)
                        sb.append("\n");
                }
                mMeanings.setText(sb.toString());
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void deleteGrammar() {
        Log.d(TAG, "deleteGrammar id = " + mGrammarId);

        if (mGrammarId < 0) {
            Log.e(TAG, "failed to deleteGrammar id = " + mGrammarId);
            return;
        }

        String whereClause = ProviderContract.Grammars._ID + " = ?";
        String[] whereArgs = { String.valueOf(mGrammarId) };

        int count = getActivity().getContentResolver().delete(ProviderContract.Grammars.CONTENT_URI, whereClause, whereArgs);
        Log.d(TAG, "deleted count = " + count);
        getActivity().finish();
    }
}
