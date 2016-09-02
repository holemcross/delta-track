package com.holemcross.deltatrack.data.repository;

import android.database.sqlite.SQLiteDatabase;

import com.holemcross.deltatrack.data.database.DeltaTrackDbHelper;

/**
 * Created by amortega on 9/2/2016.
 */
public interface IDeltraTrackRepository {
    SQLiteDatabase getDb();
    SQLiteDatabase getDb(boolean readOnly);
}
