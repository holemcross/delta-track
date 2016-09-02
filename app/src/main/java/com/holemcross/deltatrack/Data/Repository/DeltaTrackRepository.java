package com.holemcross.deltatrack.data.repository;

import android.database.sqlite.SQLiteDatabase;

import com.holemcross.deltatrack.data.database.DeltaTrackDbHelper;

/**
 * Created by amortega on 8/31/2016.
 */
public class DeltaTrackRepository {
    private DeltaTrackDbHelper mDeltaTrackDbHelper;
    private SQLiteDatabase mDatabase;

    public DeltaTrackRepository(DeltaTrackDbHelper helper){
        mDeltaTrackDbHelper = helper;
    }

    public DeltaTrackRepository(SQLiteDatabase db){
        mDatabase = db;
    }

    protected SQLiteDatabase getDb(){
        if(mDatabase != null){
            return mDatabase;
        }
        return mDeltaTrackDbHelper.getWritableDatabase();
    }

    protected SQLiteDatabase getDb(boolean readOnly){
        if(mDeltaTrackDbHelper == null){
            return mDatabase;
        }

        if(readOnly){
            return mDeltaTrackDbHelper.getReadableDatabase();
        }
        return mDeltaTrackDbHelper.getWritableDatabase();
    }
}
