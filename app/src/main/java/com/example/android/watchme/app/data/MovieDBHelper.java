package com.example.android.watchme.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import static com.example.android.watchme.app.data.MovieContract.*;


/**
 * Created by u0162467 on 5/23/2016.
 */
public class MovieDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE "
                + TrailerEntry.TABLE_NAME + " ("
                + TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL,"
                + TrailerEntry.COLUMN_TRAILER_NAME + " TEXT NOT NULL,"
                + TrailerEntry.COLUMN_TRAILER_KEY + " TEXT NOT NULL,"
                + " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES "
                + TrailerEntry.TABLE_NAME + "(" + MoviesEntry._ID + ")"
                + ");";

        Log.v("Trailers : " , SQL_CREATE_TRAILER_TABLE);

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE "
                + MoviesEntry.TABLE_NAME + " ("
                + MoviesEntry._ID + " INTEGER PRIMARY KEY ,"
                + MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                + MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + MoviesEntry.COLUMN_POSTER + " TEXT NOT NULL, "
                + MoviesEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, "
                + MoviesEntry.COLUMN_RUNTIME + " INTEGER NOT NULL, "
                + MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, "
                + MoviesEntry.COLUMN_RATING + " REAL NOT NULL"
                + ");";


        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_MOVIE_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);

        onCreate(db);

    }
}
