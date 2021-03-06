package com.loyid.orangedict;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.loyid.orangedict.database.ProviderContract;
import com.loyid.orangedict.util.Utils;


/**
 * A placeholder fragment containing a simple view.
 */
public class EditWordActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = EditWordActivityFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private AutoCompleteTextView mEditTextWord = null;
    private LinearLayout mMeanContainer = null;
    private ScrollView mScrollViewGroup = null;

    private SimpleCursorAdapter mAdapter;

    private long mInitGrammarId = -1;
    private String mInitGrammar = null;

    private String mOldGrammar = null;

    private static final int GRAMMAR_LOADER = 0;

    public EditWordActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle arg = getArguments();
        if (arg != null) {
            mInitGrammarId = getArguments().getLong("grammar_id", -1);
            mInitGrammar = getArguments().getString("grammar", null);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GRAMMAR_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbar = (Toolbar)getView().findViewById(R.id.toolbar);
        if (inSingleActivity()) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            // this is two-pane mode
            if (toolbar != null) {
                Menu menu = toolbar.getMenu();
                if (menu != null) menu.clear();
                toolbar.inflateMenu(R.menu.menu_view_word);
                //finishCreatingMenu(toolbar.getMenu());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout rl = (RelativeLayout)inflater.inflate(R.layout.fragment_edit_word, container, false);

        mEditTextWord = (AutoCompleteTextView)rl.findViewById(R.id.edittext_word);
        mMeanContainer = (LinearLayout)rl.findViewById(R.id.mean_container);
        mScrollViewGroup = (ScrollView)rl.findViewById(R.id.scrollview_mean_group);

        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line,
                null, new String[] { ProviderContract.Grammars.COLUMN_NAME_GRAMMAR },
                new int[] { android.R.id.text1 }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mEditTextWord.setAdapter(mAdapter);
        mAdapter.setStringConversionColumn(1);
        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String partialValue = constraint.toString().toUpperCase();
                String projection[] = {
                        ProviderContract.Grammars._ID,
                        ProviderContract.Grammars.COLUMN_NAME_GRAMMAR
                };

                Cursor cursor = getActivity().getContentResolver().query(ProviderContract.Grammars.CONTENT_URI,
                        projection,
                        "UPPER(" + ProviderContract.Grammars.COLUMN_NAME_GRAMMAR + ") GLOB ?",
                        new String[]{partialValue + "*"},
                        ProviderContract.Grammars.DEFAULT_SORT_ORDER);

                return cursor;
            }
        });

        mEditTextWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (DEBUG) Log.d(TAG, "Focus changed hasFocus = " + hasFocus);
                if (!hasFocus && v.isEnabled()) {
                    checkGrammar();
                }
            }
        });

        mEditTextWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChagned s = " + s + " start = " + start + " count = " + count + " after = " + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged s = " + s + " start = " + start + " before = " + before + " count = " + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged s = " + s);
            }
        });
        Button addButton = (Button)rl.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMeanItem(0, null);
            }
        });

        if (mInitGrammarId > 0) {
            reloadMeaningRows(mInitGrammarId);
            mEditTextWord.setEnabled(false);
        } else if (mInitGrammar != null){
            mEditTextWord.setText(mInitGrammar);
            checkGrammar();
        } else {
            addMeanItem(0, null);
        }

        return rl;
    }

    private void addMeanItem(int type, String mean) {
        View item = getActivity().getLayoutInflater().inflate(R.layout.mean_item_container, null);
        setItemHolder(item);
        ItemHolder holder = (ItemHolder)item.getTag();
        final int count = mMeanContainer.getChildCount();
        holder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMeanItem((int) v.getTag());
            }
        });

        holder.mEditText.setText(mean);
        holder.mSpinner.setSelection(type);

        mMeanContainer.addView(item);
        if (count != 0) {
            mMeanContainer.requestChildFocus(item, holder.mEditText);
            holder.mEditText.requestFocus();
        }

        recalculateIndexes();
    }

    private class ItemHolder {
        EditText mEditText;
        ImageButton mRemoveButton;
        Spinner mSpinner;
        TextView mLabelNo;
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
        ItemHolder holder = (ItemHolder)view.getTag();
        holder.mEditText.setText("");
        holder.mSpinner.setSelection(0);
    }

    private void setItemHolder(View item) {
        ItemHolder holder = new ItemHolder();
        holder.mEditText = (EditText)item.findViewById(R.id.edittext_mean);
        holder.mRemoveButton = (ImageButton)item.findViewById(R.id.remove_button);
        holder.mSpinner = (Spinner)item.findViewById(R.id.spinner_type);
        holder.mLabelNo = (TextView)item.findViewById(R.id.label_no);

        item.setTag(holder);
    }

    private synchronized void recalculateIndexes() {
        for (int i = 0; i < mMeanContainer.getChildCount(); i++) {
            View item = mMeanContainer.getChildAt(i);

            ItemHolder holder = (ItemHolder) item.getTag();

            holder.mRemoveButton.setTag(i);
            holder.mLabelNo.setText(String.valueOf(i + 1));
        }
    }

    private boolean inSingleActivity() {
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        return activity instanceof EditWordActivity;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        if (inSingleActivity()) {
            inflater.inflate(R.menu.menu_edit_word, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveGrammar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveGrammar() {
        if (DEBUG) Log.d(TAG, "saveGrammar()");
        String grammar = mEditTextWord.getText().toString().trim();
        if (grammar == null || grammar.length() == 0) {
            if (DEBUG) Log.d(TAG, "failed to save Grammar because grammar is not inputted");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR, grammar);

        final int count = mMeanContainer.getChildCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            View item = mMeanContainer.getChildAt(i);
            ItemHolder holder = (ItemHolder)item.getTag();
            long type = holder.mSpinner.getSelectedItemId();
            String mean = holder.mEditText.getText().toString().trim();

            if (mean == null || mean.length() == 0)
                continue;

            sb.append(String.valueOf(type) + Utils.IDENTIFIER_MEANING + mean);
            if (i < count - 1) {
                sb.append(Utils.IDENTIFIER_MEANING_GROUP);
            }
        }
        if (sb.length() == 0) {
            if (DEBUG) Log.d(TAG, "failed to save Grammar because there is no meaings.");
            return;
        }

        values.put(ProviderContract.Grammars.COLUMN_NAME_SUMMARY, sb.toString());

        long grammarId = Utils.getGrammarId(getActivity(), grammar);

        if (grammarId < 0 && count > 0) {
            if (DEBUG) Log.d(TAG, "success to save grammar for new item");
            getActivity().getContentResolver().insert(ProviderContract.Grammars.CONTENT_URI, values);
        } else {
            if (DEBUG) Log.d(TAG, "success to save grammar for update item(id=" + grammarId + ")");
            getActivity().getContentResolver().update(
                    Uri.withAppendedPath(ProviderContract.Grammars.CONTENT_GRAMMAR_ID_URI_BASE, "" + grammarId),
                    values, null, null);
        }

        if (inSingleActivity()) {
            getActivity().finish();
        }
    }

    private void checkGrammar() {
        String grammar = mEditTextWord.getText().toString().trim();

        if (mOldGrammar != null && mOldGrammar.equals(grammar)) {
            Log.d(TAG, "checkGrammar() : new grammar is same with old grammar that was inputted.");
            return;
        }

        mOldGrammar = grammar;

        long grammarId = Utils.getGrammarId(getActivity(), grammar);
        if (grammarId > 0) {
            reloadMeaningRows(grammarId);
        } else if (mMeanContainer.getChildCount() <= 0) {
            addMeanItem(0, null);
        }
    }

    private void reloadMeaningRows(long grammarId) {
        if (grammarId < 0) {
            Log.e(TAG, "failed to getGrammarInfo grammarId = " + grammarId);
            return;
        }

        String selection = ProviderContract.Grammars._ID + " = ?";
        String[] selectionArgs = { String.valueOf(grammarId) };

        String[] projection = {
                ProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
                ProviderContract.Grammars.COLUMN_NAME_SUMMARY
        };

        Cursor cursor = getActivity().getContentResolver().query(ProviderContract.Grammars.CONTENT_URI,
                projection, selection, selectionArgs, ProviderContract.Grammars.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int grammarColumnIndex = cursor.getColumnIndex(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
                int meaningColumnIndex = cursor.getColumnIndex(ProviderContract.Grammars.COLUMN_NAME_SUMMARY);
                mEditTextWord.setText(cursor.getString(grammarColumnIndex));
                String meanings = cursor.getString(meaningColumnIndex);
                String[] meaningGroup = meanings.split(Utils.IDENTIFIER_MEANING_GROUP);
                for (int i = 0; i < meaningGroup.length; i++) {
                    String[] splits = meaningGroup[i].split(Utils.IDENTIFIER_MEANING);
                    int type = Integer.valueOf(splits[0]);
                    String mean = splits[1];
                    addMeanItem(type, mean);
                }
            }

            cursor.close();
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProviderContract.Grammars._ID,
                ProviderContract.Grammars.COLUMN_NAME_GRAMMAR
        };
        return new CursorLoader(getActivity(), ProviderContract.Grammars.CONTENT_URI, projection, null, null, ProviderContract.Grammars.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
