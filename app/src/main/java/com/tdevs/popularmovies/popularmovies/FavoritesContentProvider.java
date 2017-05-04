package com.tdevs.popularmovies.popularmovies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Tomasz on 04.05.2017.
 */

public class FavoritesContentProvider extends ContentProvider {

    public static final int FAVORITES = 100;
    public static final int FAVORITE_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatabaseWrapper databaseWrapper;


    public static UriMatcher buildUriMatcher()
    {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(Contract.AUTHORITY, Contract.PATH_FAVORITES, FAVORITES);
        uriMatcher.addURI(Contract.AUTHORITY, Contract.PATH_FAVORITES + "/#", FAVORITE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {

        Context context = getContext();
        databaseWrapper = new DatabaseWrapper(context);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = databaseWrapper.getReDatabase();

        int match = sUriMatcher.match(uri);

        Cursor retCursor;

        switch(match)
        {
            case FAVORITES:
            {
                retCursor = db.query(Contract.FavoriteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case FAVORITE_WITH_ID:
            {
                //TODO really needed?
                retCursor = db.query(Contract.FavoriteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Unsupported uri match when reading");
            }
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        throw new UnsupportedOperationException("getTYPE NOT YET IMPLEMENTED");

        //return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        SQLiteDatabase db = databaseWrapper.getWrDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch(match)
        {
            case FAVORITES:
            {
                System.out.println("table name: " + Contract.FavoriteEntry.TABLE_NAME);
                System.out.println("cv: " + contentValues);

                databaseWrapper.open();
                long id = db.insert(Contract.FavoriteEntry.TABLE_NAME, null, contentValues);
                System.out.println("id = " + id);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(Contract.FavoriteEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into "  + uri);
                }

                databaseWrapper.close();

                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = databaseWrapper.getWrDatabase();

        int match = sUriMatcher.match(uri);
        int favoritesDeleted = 0;

        switch(match)
        {
            case FAVORITE_WITH_ID:
            {
                String id = uri.getPathSegments().get(1);
                databaseWrapper.open();
                favoritesDeleted = db.delete(Contract.FavoriteEntry.TABLE_NAME, Contract.FavoriteEntry.COLUMN_MOVID + "=?", new String[]{id});
                databaseWrapper.close();
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (favoritesDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return favoritesDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        throw new UnsupportedOperationException("update not yet implemented");

        //return 0;
    }
}
