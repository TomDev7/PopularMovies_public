package com.tdevs.popularmovies.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class DatabaseWrapper {

		private DatabaseHelper mOpenHelper;
		private SQLiteDatabase db;

		private static final String DATABASE_NAME = "popularmovies_database.db";
		private static final String MOVIES_TABLE = "movies_table";
		private static final String RELEASE_DATE_COLUMN = "release_date";
		private static final String POSTER_PATH_COLUMN = "poster_path";
		private static final String ADULT_COLUMN = "adult";
		private static final String OVERVIEW_COLUMN = "overview";
		private static final String GENRE_IDS_COLUMN = "genre_ids";
		private static final String ID_COLUMN = "id";
		private static final String ORIGINAL_TITLE_COLUMN = "original_title";
		private static final String ORIGINAL_LANGUAGE_COLUMN = "original_language";
		private static final String TITLE_COLUMN = "title";
		private static final String BACKDROP_PATH_COLUMN = "backdrop_path";
		private static final String POPULARITY_COLUMN = "popularity";
		private static final String VOTE_COUNT_COLUMN = "vote_count";
		private static final String VIDEO_COLUMN = "video";
		private static final String VOTE_AVERAGE_COLUMN = "vote_average";
		private static final String FAVOURITE_COLUMN = "favourite";


		private static class DatabaseHelper extends SQLiteOpenHelper {

			private static final int DATABASE_VERSION  = 1; //in new app releases, increment this when the data scheme has been changed. Thus it is possible to write a code, that will
															// "recreate" the database when needed, so that it has the new, up to date shape

			DatabaseHelper(Context context) {
				super(context, DATABASE_NAME, null, 2);
			}

			@Override
			public void onCreate(SQLiteDatabase db)
		{

		db.execSQL("CREATE TABLE " + MOVIES_TABLE + " ("
		+ ID_COLUMN + " INTEGER," +
				RELEASE_DATE_COLUMN + " TEXT," +
				ORIGINAL_TITLE_COLUMN + " TEXT," +
				TITLE_COLUMN + " TEXT," +
				POSTER_PATH_COLUMN + " TEXT," +
				OVERVIEW_COLUMN + " TEXT," +
				ADULT_COLUMN + " INTEGER," +
				GENRE_IDS_COLUMN + " TEXT," +
				ORIGINAL_LANGUAGE_COLUMN + " TEXT," +
				BACKDROP_PATH_COLUMN + " TEXT," +
				POPULARITY_COLUMN + " DOUBLE," +
				VOTE_COUNT_COLUMN + " INTEGER," +
				VIDEO_COLUMN + " INTEGER," +
				VOTE_AVERAGE_COLUMN + " DOUBLE," +
				FAVOURITE_COLUMN + " INTEGER" +
				");" );

			db.execSQL("CREATE TABLE " + Contract.FavoriteEntry.TABLE_NAME + " (" +
					Contract.FavoriteEntry._ID + " INTEGER PRIMARY KEY," +
					Contract.FavoriteEntry.COLUMN_MOVID + " TEXT," +
					Contract.FavoriteEntry.COLUMN_TITLE + " TEXT" +
					");" );
		}


			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
				db.execSQL("DROP TABLE IF EXISTS" + MOVIES_TABLE);
				db.execSQL("DROP TABLE IF EXISTS" + Contract.FavoriteEntry.TABLE_NAME);
				onCreate(db);
			}

		}
		
		public DatabaseWrapper(Context ctx) {
			mOpenHelper = new DatabaseHelper(ctx);
		}

		public DatabaseWrapper open() throws SQLException {
			db = mOpenHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			mOpenHelper.close();
			db.close();
		}

		public SQLiteDatabase getWrDatabase() {

			SQLiteDatabase sqldb = mOpenHelper.getWritableDatabase();
			return sqldb;
		}

		public SQLiteDatabase getReDatabase() {

			SQLiteDatabase sqldb = mOpenHelper.getWritableDatabase();
			return sqldb;
		}




		public Cursor getTables() {
			Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'",null);
			return c; }

		public List<OneMovie> getAllMovies() {
			
			List<OneMovie> myList = new ArrayList<OneMovie>();

			Cursor c = db.query(MOVIES_TABLE, null, null, null, null, null, ID_COLUMN +" ASC");
			c.moveToFirst();
			
			while (!c.isAfterLast()) {
				OneMovie mov = new OneMovie();

				mov.setId(c.getInt(0));
				mov.setReleaseDate(c.getString(1));
				mov.setOriginalTitle(c.getString(2));
				mov.setTitle(c.getString(3));
				mov.setPosterPath(c.getString(4));
				mov.setOverview(c.getString(5));
				mov.setAdult(c.getInt(6) == 1);

				String genreIdsString = c.getString(7);
				JSONArray genreIdsJson;
				List<Integer> genreIdsList = null;
				try {
					genreIdsJson = new JSONArray(genreIdsString);

					genreIdsList = new ArrayList<Integer>();

					for (int i = 0; i < genreIdsJson.length(); i++)
					{
						genreIdsList.add(genreIdsJson.getInt(i));
					}

					mov.setGenreIds(genreIdsList);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if (genreIdsList == null)
				{
					mov.setGenreIds(null);
				}

				mov.setOriginalLanguage(c.getString(8));
				mov.setBackdropPath(c.getString(9));
				mov.setPopularity(c.getDouble(10));
				mov.setVoteCount(c.getInt(11));
				mov.setVideo(c.getInt(12) == 1);
				mov.setVoteAverage(c.getDouble(13));

				myList.add(mov);

				c.moveToNext();
			}
			
			c.close();
			
			return myList; 
			}



		public void removeOneMovie(int _id)
	{
		db.delete(MOVIES_TABLE, "id=" + _id, null);
	}

		public void removeAllMovies()
		{
			db.delete(MOVIES_TABLE, "1", null);
		}


		public void setMovieFavourite(OneMovie movie)
		{
			ContentValues cv = new ContentValues();
			cv.put(ID_COLUMN, movie.getId());
			cv.put(RELEASE_DATE_COLUMN, movie.getReleaseDate());
			cv.put(ORIGINAL_TITLE_COLUMN, movie.getOriginalTitle());
			cv.put(TITLE_COLUMN, movie.getTitle());
			cv.put(POSTER_PATH_COLUMN, movie.getPosterPath());
			cv.put(OVERVIEW_COLUMN, movie.getOverview());
			cv.put(ADULT_COLUMN, movie.getAdult());

			JSONArray genreIdsJson = new JSONArray();
			for (int i = 0; i < movie.getGenreIds().size(); i++)
			{
				genreIdsJson.put(movie.getGenreIds().get(i));
			}
			cv.put(GENRE_IDS_COLUMN, genreIdsJson.toString());

			cv.put(ORIGINAL_LANGUAGE_COLUMN, movie.getOriginalLanguage());
			cv.put(BACKDROP_PATH_COLUMN, movie.getBackdropPath());
			cv.put(POPULARITY_COLUMN, movie.getPopularity());
			cv.put(VOTE_COUNT_COLUMN, movie.getVoteCount());
			cv.put(VIDEO_COLUMN, movie.getVideo());
			cv.put(VOTE_AVERAGE_COLUMN, movie.getVoteAverage());
			cv.put(FAVOURITE_COLUMN, movie.getFavourite());

			db.update(MOVIES_TABLE, cv, "id="+movie.getId(), null);
		}

		public void insertOneMovie(OneMovie mov)
	{
		ContentValues cv = new ContentValues();

		cv.put(ID_COLUMN, mov.getId());
		cv.put(RELEASE_DATE_COLUMN, mov.getReleaseDate());
		cv.put(ORIGINAL_TITLE_COLUMN, mov.getOriginalTitle());
		cv.put(TITLE_COLUMN, mov.getTitle());
		cv.put(POSTER_PATH_COLUMN, mov.getPosterPath());
		cv.put(OVERVIEW_COLUMN, mov.getOverview());
		cv.put(ADULT_COLUMN, mov.getAdult());

		JSONArray genreIdsJson = new JSONArray();
		if (mov.getGenreIds() != null) {
			for (int i = 0; i < mov.getGenreIds().size(); i++) {
				genreIdsJson.put(mov.getGenreIds().get(i));
			}
		}
		cv.put(GENRE_IDS_COLUMN, genreIdsJson.toString());
		System.out.println("genresIds to write: " + genreIdsJson.toString());

		cv.put(ORIGINAL_LANGUAGE_COLUMN, mov.getOriginalLanguage());
		cv.put(BACKDROP_PATH_COLUMN, mov.getBackdropPath());
		cv.put(POPULARITY_COLUMN, mov.getPopularity());
		cv.put(VOTE_COUNT_COLUMN, mov.getVoteCount());
		cv.put(VIDEO_COLUMN, mov.getVideo());
		cv.put(VOTE_AVERAGE_COLUMN, mov.getVoteAverage());
		cv.put(FAVOURITE_COLUMN, mov.getFavourite());

		db.insert(MOVIES_TABLE, null, cv);
	}

	public OneMovie getMovie(int id)
	{
		OneMovie mov = new OneMovie();

		Cursor curMov = db.rawQuery("SELECT * FROM " + MOVIES_TABLE + " WHERE " + ID_COLUMN + " = " + id + ";", null);

		if (curMov.getCount() != 0) {

			curMov.moveToFirst();

			if (curMov != null) {
				mov.setId(curMov.getInt(0));
				mov.setReleaseDate(curMov.getString(1));
				mov.setOriginalTitle(curMov.getString(2));
				mov.setTitle(curMov.getString(3));
				mov.setPosterPath(curMov.getString(4));
				mov.setOverview(curMov.getString(5));
				mov.setAdult(curMov.getInt(6) == 1);

				String genreIdsString = curMov.getString(7);
				if (genreIdsString == null)
				{
					genreIdsString = "";
				}

				JSONArray genreIdsJson;
				List<Integer> genreIdsList = null;
				try {
					genreIdsJson = new JSONArray(genreIdsString);

					if (genreIdsJson == null)
					{
						return null;
					}

					genreIdsList = new ArrayList<Integer>();

					for (int i = 0; i < genreIdsJson.length(); i++) {
						genreIdsList.add(genreIdsJson.getInt(i));
					}

					mov.setGenreIds(genreIdsList);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (genreIdsList == null) {
					System.out.println("genreIdsList - failed to be filled");
					mov.setGenreIds(null);
				}

				mov.setOriginalLanguage(curMov.getString(8));
				mov.setBackdropPath(curMov.getString(9));
				mov.setPopularity(curMov.getDouble(10));
				mov.setVoteCount(curMov.getInt(11));
				mov.setVideo(curMov.getInt(12) == 1);
				mov.setVoteAverage(curMov.getDouble(13));
				mov.setFavourite(curMov.getInt(14) == 1);
			}

			return mov;
		}
		else {
			return null;
		}
	}
		
		public int getNumOfMovies()
		{
			Cursor numOfMovies = db.rawQuery("SELECT * FROM " + MOVIES_TABLE, null);
			return numOfMovies.getCount();
		}

}
