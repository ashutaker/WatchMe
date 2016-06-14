package com.example.android.watchme.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by u0162467 on 5/23/2016.
 */
public class TestDB extends AndroidTestCase {

    public static final String LOG_TAG = TestDB.class.getSimpleName();

    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNameHashSet = new HashSet<String>();

        tableNameHashSet.add(MovieContract.MoviesEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.TrailerEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDBHelper.DATABASE_NAME);

        SQLiteDatabase db = new MovieDBHelper(this.mContext).getWritableDatabase();

        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        assertTrue("Error: Your database was created without both the movie entry and trailer entry tables",
                tableNameHashSet.isEmpty());


        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.TrailerEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        final HashSet<String> trailerColumnHastSet = new HashSet<String>();
        trailerColumnHastSet.add(MovieContract.TrailerEntry._ID);
        trailerColumnHastSet.add(MovieContract.TrailerEntry.COLUMN_MOVIE_ID);
        trailerColumnHastSet.add(MovieContract.TrailerEntry.COLUMN_TRAILER_NAME);
        trailerColumnHastSet.add(MovieContract.TrailerEntry.COLUMN_TRAILER_KEY);


        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            trailerColumnHastSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required trailer entry columns",
                trailerColumnHastSet.isEmpty());
        db.close();

    }

    void deleteDatabase() {
        mContext.deleteDatabase(MovieDBHelper.DATABASE_NAME);
    }


    public void testTrailerDb() throws Throwable{

        MovieDBHelper db = new MovieDBHelper(mContext);

        SQLiteDatabase trailerDb = db.getWritableDatabase();

        long movieRowId = TestUtilities.insertMovieValues(mContext);

        ContentValues testValues = TestUtilities.createTrailerValues(movieRowId);

        long trailerRowId = trailerDb.insert(MovieContract.TrailerEntry.TABLE_NAME,null,testValues);

        assertTrue("Error : Failed to insert values on Trailer Table.",trailerRowId != -1);


        Cursor cursor = trailerDb.query(MovieContract.TrailerEntry.TABLE_NAME
        ,null,null,null,null,null,null,null);

        assertTrue("Error : No record found in the Trailer Table.",cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error : Data validation failed",cursor,testValues);

        assertFalse("Error : More than one record found.",cursor.moveToNext());

        cursor.close();
        trailerDb.close();

    }

    public void testMovieDb()throws  Throwable{
        MovieDBHelper db = new MovieDBHelper(mContext);
        SQLiteDatabase movieDb = db.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        long movieRowId = movieDb.insert(MovieContract.MoviesEntry.TABLE_NAME,null,testValues);

        assertTrue("Error : Failed to insert values on Movie Table.",movieRowId != -1);

        Cursor cursor = movieDb.query(MovieContract.MoviesEntry.TABLE_NAME
        ,null,null,null,null,null,null,null);

        assertTrue("Error : No recored found in the Movie Table.",cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error : Data validation failed",cursor,testValues);

        assertFalse("Error : More than one record found.",cursor.moveToNext());

        cursor.close();
        movieDb.close();

    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteDatabase();
    }


}



