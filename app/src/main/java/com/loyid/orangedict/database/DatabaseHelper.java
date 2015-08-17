package com.loyid.orangedict.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Loyid on 2015-08-11.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "orangedict.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ProviderContract.Grammars.TABLE_NAME + " ("
                + ProviderContract.Grammars._ID + " INTEGER PRIMARY KEY,"
                + ProviderContract.Grammars.COLUMN_NAME_GRAMMAR + " TEXT NOT NULL,"
                + ProviderContract.Grammars.COLUMN_NAME_SUMMARY + " TEXT,"
                + ProviderContract.Grammars.COLUMN_NAME_USAGE + " TEXT,"
                + ProviderContract.Grammars.COLUMN_NAME_CREATED_DATE + " INTEGER,"
                + ProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE + " INTEGER,"
                + ProviderContract.Grammars.COLUMN_NAME_SORT_KEY + " TEXT NOT NULL"
                + ");");

        db.execSQL("CREATE TABLE " + ProviderContract.Meanings.TABLE_NAME + " ("
                + ProviderContract.Meanings._ID + " INTEGER PRIMARY KEY,"
                + ProviderContract.Meanings.COLUMN_NAME_WORD + " TEXT NOT NULL,"
                + ProviderContract.Meanings.COLUMN_NAME_TYPE + " INTEGER,"
                + ProviderContract.Meanings.COLUMN_NAME_CREATED_DATE + " INTEGER,"
                + ProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE + " INTEGER,"
                + ProviderContract.Meanings.COLUMN_NAME_REF_COUNT + " INTEGER DEFAULT 0 NOT NULL,"
                + ProviderContract.Meanings.COLUMN_NAME_SORT_KEY + " TEXT NOT NULL"
                + ");");

        db.execSQL("CREATE TABLE " + ProviderContract.Mappings.TABLE_NAME + " ("
                + ProviderContract.Mappings._ID + " INTEGER PRIMARY KEY,"
                + ProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " INTEGER,"
                + ProviderContract.Mappings.COLUMN_NAME_MEANING_ID + " INTEGER"
                + ");");

        db.execSQL("CREATE TRIGGER IF NOT EXISTS decrease_meaning_ref_count AFTER DELETE on "
                + ProviderContract.Mappings.TABLE_NAME + " "
                + "BEGIN "
                + "UPDATE " + ProviderContract.Meanings.TABLE_NAME + " "
                + "SET ref_count = ref_count - 1 "
                + "WHERE _id = old." + ProviderContract.Mappings.COLUMN_NAME_MEANING_ID + ";"
                + "DELETE from " + ProviderContract.Meanings.TABLE_NAME + " WHERE ref_count <= 0;"
                + "END");

        db.execSQL("CREATE TRIGGER IF NOT EXISTS increase_meaning_ref_count AFTER INSERT on "
                + ProviderContract.Mappings.TABLE_NAME + " "
                + "BEGIN "
                + "UPDATE " + ProviderContract.Meanings.TABLE_NAME + " "
                + "SET ref_count = ref_count + 1 "
                + "WHERE _id = new." + ProviderContract.Mappings.COLUMN_NAME_MEANING_ID + ";"
                + "END");

        db.execSQL("CREATE TRIGGER IF NOT EXISTS update_mapping_after_delete AFTER DELETE on "
                + ProviderContract.Grammars.TABLE_NAME + " "
                + "BEGIN "
                + "DELETE from " + ProviderContract.Mappings.TABLE_NAME
                + " WHERE grammar_id = old." + ProviderContract.Grammars._ID + ";"
                + "END");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        // Kills the table and existing data
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.Grammars.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.Meanings.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.Mappings.TABLE_NAME);
        db.execSQL("DROP TRIGGER IF EXISTS decrease_meaning_ref_count");
        db.execSQL("DROP TRIGGER IF EXISTS increase_meaning_ref_count");
        db.execSQL("DROP TRIGGER IF EXISTS update_mapping_after_delete");

        // Recreates the database with a new version
        onCreate(db);
    }
}
