package com.loyid.orangedict.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.loyid.orangedict.util.Utils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrangeDictProvider extends ContentProvider {
    private static final String TAG = OrangeDictProvider.class.getSimpleName();

    private DatabaseHelper mDBHelper = null;

    public static final int GRAMMAR = 0;
    public static final int GRAMMAR_ID = 1;
    public static final int MEANING = 2;
    public static final int MEANING_ID = 3;
    public static final int MAPPING = 4;
    public static final int MAPPING_ID = 5;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, ProviderContract.Grammars.TABLE_NAME, GRAMMAR);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, ProviderContract.Grammars.TABLE_NAME + "/#", GRAMMAR_ID);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, ProviderContract.Meanings.TABLE_NAME, MEANING);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, ProviderContract.Meanings.TABLE_NAME + "/#", MEANING_ID);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, ProviderContract.Mappings.TABLE_NAME, MAPPING);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, ProviderContract.Mappings.TABLE_NAME + "/#", MAPPING_ID);
    }

    public OrangeDictProvider() {}

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        Log.d(TAG, "delete uri = " + uri + " selection = " + selection + " selectionArgs = " + selectionArgs);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case GRAMMAR:
                count = db.delete(ProviderContract.Grammars.TABLE_NAME, selection, selectionArgs);
                break;
            case GRAMMAR_ID:
                String grammarId = uri.getPathSegments().get(ProviderContract.Grammars.GRAMMAR_ID_PATH_POSITION);
                if (selection != null) {
                    selection += " AND " + ProviderContract.Grammars._ID + " = " + grammarId;
                }
                count = db.delete(ProviderContract.Grammars.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Invalid URI = " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (sUriMatcher.match(uri)) {
            case GRAMMAR:
                return ProviderContract.Grammars.CONTENT_TYPE;
            case GRAMMAR_ID:
                return ProviderContract.Grammars.CONTENT_ITEM_TYPE;
            case MEANING:
                return ProviderContract.Meanings.CONTENT_TYPE;
            case MEANING_ID:
                return ProviderContract.Meanings.CONTENT_ITEM_TYPE;
            case MAPPING:
                return ProviderContract.Mappings.CONTENT_TYPE;
            case MAPPING_ID:
                return ProviderContract.Mappings.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI = " + uri);
        }
    }

    private void updateMeaningInfo(SQLiteDatabase db, long grammarId, String summary, long now) {
        if (summary != null) {
            String[] meaningGroup = summary.split(Utils.IDENTIFIER_MEANING_GROUP);
            Log.d(TAG, "meanings = " + summary);
            for (int i = 0; i < meaningGroup.length; i++) {
                Log.d(TAG, "###### meaing = " + meaningGroup[i]);
                String[] splits = meaningGroup[i].split(Utils.IDENTIFIER_MEANING);
                int type = Integer.valueOf(splits[0]);
                String meaning = splits[1];

                ContentValues meanValues = new ContentValues();
                meanValues.put(ProviderContract.Meanings.COLUMN_NAME_TYPE, type);
                meanValues.put(ProviderContract.Meanings.COLUMN_NAME_WORD, meaning);
                String initial = Utils.getInitialFromString(meaning, Locale.KOREAN);
                meanValues.put(ProviderContract.Meanings.COLUMN_NAME_SORT_KEY, initial);

                String selection = ProviderContract.Meanings.COLUMN_NAME_WORD + " = ?"
                        + " AND " + ProviderContract.Meanings.COLUMN_NAME_TYPE + " = ?";

                String[] selectionArgs = new String[] { meaning, splits[0] };

                Cursor cursor = db.query(ProviderContract.Meanings.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        ProviderContract.Meanings.DEFAULT_SORT_ORDER);

                long meaningId = -1;
                if (cursor != null) {
                    if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(ProviderContract.Meanings._ID);
                        meaningId = cursor.getLong(index);
                    }

                    cursor.close();
                }

                if (meaningId < 0) {
                    meanValues.put(ProviderContract.Meanings.COLUMN_NAME_CREATED_DATE, now);
                    meanValues.put(ProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, now);
                    meaningId = db.insert(ProviderContract.Meanings.TABLE_NAME, null, meanValues);

                    Log.d(TAG, "insert meaning index = " + meaningId);

                    ContentValues mappingValues = new ContentValues();
                    mappingValues.put(ProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID, grammarId);
                    mappingValues.put(ProviderContract.Mappings.COLUMN_NAME_MEANING_ID, meaningId);
                    db.insert(ProviderContract.Mappings.TABLE_NAME, null, mappingValues);
                } else {
                    Log.d(TAG, "update meaning index = " + meaningId);
                    String whereClause = ProviderContract.Meanings._ID + " = ?";
                    String[] whereArgs = new String[] { String.valueOf(meaningId) };
                    meanValues.put(ProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, now);
                    db.update(ProviderContract.Meanings.TABLE_NAME, meanValues, whereClause, whereArgs);
                }
            }
        }
    }

    private Uri insertGrammar(Uri uri, ContentValues initialValues) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            throw new IllegalArgumentException("ContentValues is null");
        }

        if (values.containsKey(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR) == false) {
            throw new IllegalArgumentException(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR + "is set null in ContentValues");
        }

        String summary = null;
        if (values.containsKey(ProviderContract.Grammars.COLUMN_NAME_SUMMARY) == true) {
            summary = values.getAsString(ProviderContract.Grammars.COLUMN_NAME_SUMMARY);
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        if (values.containsKey(ProviderContract.Grammars.COLUMN_NAME_CREATED_DATE) == false) {
            values.put(ProviderContract.Grammars.COLUMN_NAME_CREATED_DATE, now);
        }

        if (values.containsKey(ProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE) == false) {
            values.put(ProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
        }

        if (values.containsKey(ProviderContract.Grammars.COLUMN_NAME_SORT_KEY) == false) {
            String grammar = values.getAsString(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
            String initial = Utils.getInitialFromString(grammar, Locale.ENGLISH);
            values.put(ProviderContract.Grammars.COLUMN_NAME_SORT_KEY, initial);
        }

        long rowId;
        db.beginTransaction();

        try {
            rowId = db.insert(ProviderContract.Grammars.TABLE_NAME, null, values);

            updateMeaningInfo(db, rowId, summary, now);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (rowId > 0) {
            Uri grammarUri = ContentUris.withAppendedId(ProviderContract.Grammars.CONTENT_GRAMMAR_ID_URI_BASE, rowId);
            return grammarUri;
        }

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        Log.d(TAG, "insert uri = " + uri + " values = " + values.toString());
        Uri returnUri = null;
        switch(sUriMatcher.match(uri)) {
            case GRAMMAR:
                returnUri = insertGrammar(uri, values);
                break;
            case MEANING:
                break;
            case MAPPING:
                break;
            default:
                throw new UnsupportedOperationException("Invalid URI = " + uri);
        }

        if (returnUri != null) {
            getContext().getContentResolver().notifyChange(returnUri, null);
            return returnUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        mDBHelper = new DatabaseHelper(getContext());
        return true;
    }

    private String[] combine(List<String> prepend, String[] userArgs) {
        int presize = prepend.size();
        if (presize == 0) {
            return userArgs;
        }

        int usersize = (userArgs != null) ? userArgs.length : 0;
        String [] combined = new String[presize + usersize];

        for (int i = 0; i < presize; i++) {
            combined[i] = prepend.get(i);
        }

        for (int i = 0; i < usersize; i++) {
            combined[presize + i] = userArgs[i];
        }

        return combined;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Log.d(TAG, "query uri = " + uri + " projection = " + projection + " selection = "
                + selection + " selectionArgs = " + selectionArgs + " sortOrder = " + sortOrder);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        List<String> prependArgs = new ArrayList<String>();
        String groupBy = null;
        String limit = uri.getQueryParameter("limit");

        if (uri.getQueryParameter("distinct") != null) {
            qb.setDistinct(true);
        }

        switch(sUriMatcher.match(uri)) {
            case GRAMMAR:
                qb.setTables(ProviderContract.Grammars.TABLE_NAME);
                break;
            case GRAMMAR_ID:
                qb.setTables(ProviderContract.Grammars.TABLE_NAME);
                qb.appendWhere("_id=?");
                prependArgs.add(uri.getPathSegments().get(ProviderContract.Grammars.GRAMMAR_ID_PATH_POSITION));
                break;
            case MEANING:
                qb.setTables(ProviderContract.Meanings.TABLE_NAME);
                break;
            case MEANING_ID:
                qb.setTables(ProviderContract.Meanings.TABLE_NAME);
                qb.appendWhere("_id=?");
                prependArgs.add(uri.getPathSegments().get(ProviderContract.Meanings.MEANING_ID_PATH_POSITION));
                break;
            case MAPPING:
                qb.setTables(ProviderContract.Mappings.TABLE_NAME);
                break;
            case MAPPING_ID:
                qb.setTables(ProviderContract.Mappings.TABLE_NAME);
                qb.appendWhere("_id=?");
                prependArgs.add(uri.getPathSegments().get(ProviderContract.Mappings.MAPPING_ID_PATH_POSITION));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI = " + uri);
        }

        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        Cursor c = qb.query(db, projection, selection,
                combine(prependArgs, selectionArgs), groupBy, null, sortOrder, limit);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return c;
    }

    private int updateGrammar(long rowId, ContentValues initialValues, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count;

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            throw new IllegalArgumentException("ContentValues is null");
        }

        if (values.containsKey(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR) == false) {
            throw new IllegalArgumentException(ProviderContract.Grammars.COLUMN_NAME_GRAMMAR + "is set null in ContentValues");
        }

        String summary = null;
        if (values.containsKey(ProviderContract.Grammars.COLUMN_NAME_SUMMARY) == true) {
            summary = values.getAsString(ProviderContract.Grammars.COLUMN_NAME_SUMMARY);
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        if (values.containsKey(ProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE) == false) {
            values.put(ProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
        }

        db.beginTransaction();

        try {
            count = db.update(ProviderContract.Grammars.TABLE_NAME, initialValues, selection, selectionArgs);
            updateMeaningInfo(db, rowId, summary, now);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        Log.d(TAG, "update uri = " + uri + " value = " + values.toString()
                + " selection = " + selection + " selectionArgs = " + selectionArgs);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case GRAMMAR_ID:
                String grammarId = uri.getPathSegments().get(ProviderContract.Grammars.GRAMMAR_ID_PATH_POSITION);
                String prependSelection = ProviderContract.Grammars._ID + " = " + grammarId;
                if (selection != null) {
                    selection += " AND " + ProviderContract.Grammars._ID + " = " + grammarId;
                } else {
                    selection = prependSelection;
                }
                count = updateGrammar(Integer.valueOf(grammarId), values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI = " + uri);
        }

        Log.d(TAG, "update count = " + count);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }
}
