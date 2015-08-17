package com.loyid.orangedict;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loyid.orangedict.database.ProviderContract;
import com.loyid.orangedict.util.Utils;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MainActivityFragment.class.getSimpleName();

    private static final int GRAMMAR_LIST_LOADER = 0;

    private GrammarListAdapter mAdapter = null;
    private ListView mListView = null;

    public MainActivityFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GRAMMAR_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        View emptyView = view.findViewById(R.id.empty_view);
        mListView = (ListView) view.findViewById(R.id.grammar_list);
        mListView.setEmptyView(emptyView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ViewWordActivity.class);
                Log.d(TAG, "item selected position = " + position + " id = " + id);
                intent.putExtra("grammar_id", id);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mAdapter = new GrammarListAdapter(getActivity(), null, 0);

        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] columns = {
                ProviderContract.Grammars._ID,
                ProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
                ProviderContract.Grammars.COLUMN_NAME_SUMMARY
        };

        Uri uri = ProviderContract.Grammars.CONTENT_URI;

        return new CursorLoader(getActivity(), uri, columns, null, null, ProviderContract.Grammars.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }

    public static class ViewHolder {
        public final TextView mGrammar;
        public final TextView mSummary;

        public ViewHolder(View view) {
            mGrammar = (TextView)view.findViewById(R.id.grammar);
            mSummary = (TextView)view.findViewById(R.id.summary);
        }
    }

    public class GrammarListAdapter extends CursorAdapter {

        public GrammarListAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.grammar_list_item, parent, false);
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder)view.getTag();
            int grammarColumnIndex = cursor.getColumnIndex(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
            int summaryColumnIndex = cursor.getColumnIndex(ProviderContract.Grammars.COLUMN_NAME_SUMMARY);

            String grammar = cursor.getString(grammarColumnIndex);
            String summary = cursor.getString(summaryColumnIndex);

            holder.mGrammar.setText(grammar);
            summary = summary.replace(Utils.IDENTIFIER_MEANING_GROUP, ", -");
            summary = summary.replace(Utils.IDENTIFIER_MEANING, ":");
            holder.mSummary.setText(summary);
        }
    }
}
