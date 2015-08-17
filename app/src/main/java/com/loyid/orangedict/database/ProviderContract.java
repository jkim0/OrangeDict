package com.loyid.orangedict.database;

import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Loyid on 2015-08-11.
 */
public final class ProviderContract {
    public static final String AUTHORITY = "com.loyid.orangedict.provider";

    public static final String UNKNOWN_STRING = "<unknown>";

    public static final String GRAMMAR_BOOK_INDEX_EXTRAS = "grammar_book_index_extras";

    public static final String EXTRA_GRAMMAR_BOOK_INDEX_TITLES = "grammar_book_index_titles";
    public static final String EXTRA_GRAMMAR_BOOK_INDEX_COUNTS = "grammar_book_index_counts";

    public static String keyFor(String name) {
        if (name != null)  {
            boolean sortfirst = false;
            if (name.equals(UNKNOWN_STRING)) {
                return "\001";
            }

            // Check if the first character is \001. We use this to
            // force sorting of certain special files, like the silent ringtone.
            if (name.startsWith("\001")) {
                sortfirst = true;
            }

            name = name.trim().toLowerCase();
            if (name.startsWith("the ")) {
                name = name.substring(4);
            }

            if (name.startsWith("an ")) {
                name = name.substring(3);
            }

            if (name.startsWith("a ")) {
                name = name.substring(2);
            }

            if (name.endsWith(", the") || name.endsWith(",the") ||
                    name.endsWith(", an") || name.endsWith(",an") ||
                    name.endsWith(", a") || name.endsWith(",a")) {
                name = name.substring(0, name.lastIndexOf(','));
            }

            name = name.replaceAll("[\\[\\]\\(\\)\"'.,?!]", "").trim();
            if (name.length() > 0) {
                // Insert a separator between the characters to avoid
                // matches on a partial character. If we ever change
                // to start-of-word-only matches, this can be removed.
                StringBuilder b = new StringBuilder();
                b.append('.');
                int nl = name.length();
                for (int i = 0; i < nl; i++) {
                    b.append(name.charAt(i));
                    b.append('.');
                }

                name = b.toString();
                String key = DatabaseUtils.getCollationKey(name);
                if (sortfirst) {
                    key = "\001" + key;
                }
                return key;
            } else {
                return "";
            }
        }

        return null;
    }

    public static class OrangeDictBaseColumns implements BaseColumns {
        public static final String SCHEME = "content://";

        public static final String COLUMN_NAME_SORT_KEY = "sort_key";

        public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_SORT_KEY + " ASC"; // ASC or DESC
    }

    public static final class Grammars extends OrangeDictBaseColumns {
        public Grammars() {}

        public static final String TABLE_NAME = "grammars";

        private static final String PATH_GRAMMAR = "/grammars";

        private static final String PATH_GRAMMAR_ID = "/grammars/";

        public static final int GRAMMAR_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_GRAMMAR);

        public static final Uri CONTENT_GRAMMAR_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_GRAMMAR_ID);

        public static final Uri CONTENT_GRAMMAR_ID_URI_PATTERN = Uri.withAppendedPath(CONTENT_URI, "#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_NAME;

        public static final String CONTENT_ITEM_TYPE = "vmd.android.corsor.item/" + AUTHORITY + "." + TABLE_NAME;

        public static final String COLUMN_NAME_GRAMMAR = "grammar";

        public static final String COLUMN_NAME_SUMMARY = "summary";

        public static final String COLUMN_NAME_USAGE = "usage";

        public static final String COLUMN_NAME_NOTE = "note";

        public static final String COLUMN_NAME_CREATED_DATE = "created_date";

        public static final String COLUMN_NAME_MODIFIED_DATE = "modified_date";
    }

    public static final class Meanings extends OrangeDictBaseColumns {
        public Meanings() {}

        public static final String TABLE_NAME = "meanings";

        private static final String PATH_MEANING = "/meanings";

        private static final String PATH_MEANING_ID = "/meanings/";

        public static final int MEANING_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_MEANING);

        public static final Uri CONTENT_MEANING_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_MEANING_ID);

        public static final Uri CONTENT_MEANING_ID_URI_PATTERN = Uri.withAppendedPath(CONTENT_URI, "#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_NAME;

        public static final String CONTENT_ITEM_TYPE = "vmd.android.corsor.item/" + AUTHORITY + "." + TABLE_NAME;

        public static final String COLUMN_NAME_WORD = "word";

        public static final String COLUMN_NAME_TYPE = "type";

        public static final String COLUMN_NAME_CREATED_DATE = "created_date";

        public static final String COLUMN_NAME_MODIFIED_DATE = "modified_date";

        public static final String COLUMN_NAME_REF_COUNT = "ref_count";
    }

    public static final class Mappings extends OrangeDictBaseColumns {
        public Mappings() {}

        public static final String TABLE_NAME = "mappings";

        private static final String PATH_MAPPING = "/mappings";

        private static final String PATH_MAPPING_ID = "/mappings/";

        public static final int MAPPING_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_MAPPING);

        public static final Uri CONTENT_MAPPING_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_MAPPING_ID);

        public static final Uri CONTENT_MAPPING_ID_URI_PATTERN = Uri.withAppendedPath(CONTENT_URI, "#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_NAME;

        public static final String CONTENT_ITEM_TYPE = "vmd.android.corsor.item/" + AUTHORITY + "." + TABLE_NAME;

        public static final String COLUMN_NAME_GRAMMAR_ID = "grammar_id";

        public static final String COLUMN_NAME_MEANING_ID = "meaning_id";

        public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_GRAMMAR_ID + " ASC"; // ASC or DESC
    }
}
