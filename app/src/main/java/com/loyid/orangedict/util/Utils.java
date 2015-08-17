package com.loyid.orangedict.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.loyid.orangedict.database.ProviderContract;

import java.util.Locale;

/**
 * Created by Loyid on 2015-08-12.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static final String IDENTIFIER_MEANING_GROUP = "¤";
    public static final String IDENTIFIER_MEANING = "¡";

    private static final String ENGLISH_LANGUAGE = Locale.ENGLISH.getLanguage().toLowerCase();
    private static final String KOREAN_LANGUAGE = Locale.KOREAN.getLanguage().toLowerCase();
    private static final String[] KOREAN_INITIAL = {"ㄱ", "ㄱ"/*"ㄲ"*/, "ㄴ", "ㄷ", "ㄷ"/*"ㄸ"*/,
            "ㄹ", "ㅁ", "ㅂ", "ㅂ"/*"ㅃ"*/, "ㅅ", "ㅅ"/*"ㅆ"*/, "ㅇ", "ㅈ", "ㅈ"/*"ㅉ"*/, "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

    public static String getInitialFromString(String value, Locale locale) {
        String language = locale.getLanguage().toString();
        String initial = "#";
        if (language.equals(ENGLISH_LANGUAGE)) {
            initial = value.substring(0, 1);
        } else if (language.equals(KOREAN_LANGUAGE)) {
            final int index = (value.charAt(0) - 44032) / (21 * 28);
            if (index >= 0 && index < KOREAN_INITIAL.length) {
                initial = KOREAN_INITIAL[index];
            }
        } else {
            throw new IllegalArgumentException("Unsopported language : " + language);
        }

        return initial;
    }

    public static long getGrammarId(Context context, String grammar) {
        long grammarId = -1;

        if (grammar == null || grammar.length() == 0) {
            return grammarId;
        }

        String[] projection = {ProviderContract.Grammars._ID};
        String selection = ProviderContract.Grammars.COLUMN_NAME_GRAMMAR + " = ?";
        String[] selectionArgs = {grammar};

        Cursor cursor = context.getContentResolver().query(ProviderContract.Grammars.CONTENT_URI,
                projection, selection, selectionArgs, ProviderContract.Grammars.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(ProviderContract.Grammars._ID);
                grammarId = cursor.getLong(index);
            }

            cursor.close();
        }

        return grammarId;
    }

}