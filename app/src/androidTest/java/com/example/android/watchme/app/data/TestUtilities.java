package com.example.android.watchme.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.watchme.app.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

import static com.example.android.watchme.app.data.MovieContract.MoviesEntry;
import static com.example.android.watchme.app.data.MovieContract.TrailerEntry;

/**
 * Created by u0162467 on 5/23/2016.
 */
public class TestUtilities extends AndroidTestCase {

    static final String MOVIE_ID = "269149";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }


    static ContentValues createMovieValues (){
        ContentValues movieValues = new ContentValues();

        movieValues.put(MoviesEntry.COLUMN_MOVIE_ID,MOVIE_ID);
        movieValues.put(MoviesEntry.COLUMN_OVERVIEW,"awesome");
        movieValues.put(MoviesEntry.COLUMN_POSTER,"/adfadaasde");
        movieValues.put(MoviesEntry.COLUMN_RATING,7.9);
        movieValues.put(MoviesEntry.COLUMN_RELEASE_DATE,"2015-04-06");
        movieValues.put(MoviesEntry.COLUMN_RUNTIME,110);
        movieValues.put(MoviesEntry.COLUMN_TITLE,"Don't Know");

        return movieValues;
    }

    static ContentValues createTrailerValues(long movieRowId){
        ContentValues trailerValues = new ContentValues();

        trailerValues.put(TrailerEntry.COLUMN_MOVIE_ID,movieRowId);
        trailerValues.put(TrailerEntry.COLUMN_TRAILER_KEY,"adadfaodifadalf");
        trailerValues.put(TrailerEntry.COLUMN_TRAILER_NAME,"Trailer 1");

        return trailerValues;
    }


    static long insertMovieValues(Context context){
        MovieDBHelper movieDBHelper = new MovieDBHelper(context);
        SQLiteDatabase db = movieDBHelper.getWritableDatabase();

        long rowid;

        ContentValues values = createMovieValues();

        rowid = db.insert(MoviesEntry.TABLE_NAME,null,values);

        assertTrue("Error : Failer to insert info in the Trailer Table",rowid != -1);

        return rowid;
    }



    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}




