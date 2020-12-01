package com.example.inspectionreporter.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.inspectionreporter.R;

/**
 * code from Brian Fraser's tutorial
 * Class handles creating, inserting, and updating of database
 */
public class DBAdapter {
	// For logging:
	private static final String TAG = "DBAdapter";
	
	// DB Fields
	public static final String KEY_ROWID = "rid"; //should match index of restaurant in datamanager
	public static final int COL_ROWID = 0;

	//attribute names
	public static final String KEY_RES_NAME = "restaurantName";
	public static final String KEY_RECENT_HAZARD_LVL = "recentHazard";
	public static final String KEY_VIOLATION_IN_YEAR = "violationsInYear";
	public static final String KEY_FAVOURITE = "favourite";

	//column indices
	public static final int COL_RES_NAME = 1;
	public static final int COL_RECENT_HAZARD_LVL = 2;
	public static final int COL_VIOLATIONS_IN_YEAR = 3;
	public static final int COL_FAVOURITE = 4;

	
	public static final String[] ALL_KEYS = new String[] {
		KEY_ROWID, KEY_RES_NAME, KEY_RECENT_HAZARD_LVL, KEY_VIOLATION_IN_YEAR, KEY_FAVOURITE
	};
	
	// DB info: it's name, and the table we are using (just one).
	public static final String DATABASE_NAME = "RestaurantDB";
	public static final String DATABASE_TABLE = "Restaurants";
	// Track DB version if a new version of your app changes the format.
	public static final int DATABASE_VERSION = 8;
	
	private static final String DATABASE_CREATE_SQL = 
			"CREATE TABLE  " + DATABASE_TABLE + " (" +
					KEY_ROWID + " INTEGER PRIMARY KEY, " +
					KEY_RES_NAME + " TEXT NOT NULL, " +
					KEY_RECENT_HAZARD_LVL + " TEXT NOT NULL, " +
					KEY_VIOLATION_IN_YEAR + " INTEGER NOT NULL, " +
					KEY_FAVOURITE + " INTEGER NOT NULL " +
			");";

	
	// Context of application who uses us.
	private final Context context;
	
	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	private static DBAdapter instance;

	//shared preference for queries
	private final String SHARED_PREF_LAST_SEARCH = "Last Search Options";
	private final String SHARED_PREF_SEARCH = "Search";
	private final String SHARED_PREF_SEARCH_HAZARD = "Hazards";
	private final String SHARED_PREF_SEARCH_OPERATOR = "Operator";
	private final String SHARED_PREF_SEARCH_VIOLATION_COUNT = "Violations in last year";
	private final String SHARED_PREF_SEARCH_FAVOURITE = "Favourite";

	//data for query
	private String textEntry;
	private String hazardEntry;
	private String operatorEntry;
	private String violationCountEntry;
	private String favouriteEntry;

	private int primaryKeys;
	private final String SHARED_PREF_PRIMARY_KEY = "sharedPrimaryKey";
	private final String SHARED_PREF_PRIMARY_KEY_KEY = "primaryKey";

	//boolean for updating only at initial startup of MapsActivity
	private boolean initialUpdateDB;

	/////////////////////////////////////////////////////////////////////
	//	Public methods:
	/////////////////////////////////////////////////////////////////////

	private DBAdapter(Context ctx) {
		this.context = ctx;
		myDBHelper = new DatabaseHelper(context);
		SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_PRIMARY_KEY, Context.MODE_PRIVATE);
		primaryKeys = prefs.getInt(SHARED_PREF_PRIMARY_KEY_KEY, 0);
		initialUpdateDB = true;
	}

	public static DBAdapter getInstance(Context context) {
		if (instance == null) {
			instance = new DBAdapter(context);
		}

		return instance;
	}

	public boolean isInitialUpdateDB() {
		return initialUpdateDB;
	}

	public void setInitialUpdateDB(boolean b) {
		initialUpdateDB = b;
	}

	// Open the database connection.
	public DBAdapter open() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}
	
	// Close the database connection.
	public void close() {
		myDBHelper.close();
	}

	private void updateQueryFields() {
		SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_LAST_SEARCH, Context.MODE_PRIVATE);
		textEntry = prefs.getString(SHARED_PREF_SEARCH, "empty");
		hazardEntry = prefs.getString(SHARED_PREF_SEARCH_HAZARD, "empty");
		operatorEntry = prefs.getString(SHARED_PREF_SEARCH_OPERATOR, "empty");
		if (operatorEntry.equals("\u2264")) {
			operatorEntry = "<=";
		}
		if (operatorEntry.equals("\u2265")) {
			operatorEntry = ">=";
		}
		violationCountEntry = prefs.getString(SHARED_PREF_SEARCH_VIOLATION_COUNT, "empty");
		favouriteEntry = prefs.getString(SHARED_PREF_SEARCH_FAVOURITE, "empty");
	}

	public Cursor getRestaurantIDs() {
		updateQueryFields();
		String query = buildQuery();

		Cursor c = db.rawQuery(query, null);
		return c;
	}

	private String buildQuery() {
		boolean initialClause = true;
		String query =
				"SELECT " + KEY_ROWID + " " +
				"FROM " + DATABASE_TABLE + " ";

		if (!textEntry.equals("") && !textEntry.equals("empty")){
			query = query + "WHERE (";
			initialClause = false;
			query = query + " " + KEY_RES_NAME + " LIKE '%" + textEntry + "%' ";
		}

		if (!hazardEntry.equals("") && !hazardEntry.equals("empty")) {
			if(initialClause) {
				query = query + "WHERE (";
				initialClause = false;
			}
			else {
				query = query + " AND " ;
			}
			query = query + " " + KEY_RECENT_HAZARD_LVL  + " LIKE '" + hazardEntry + "' ";
		}

		if (!operatorEntry.equals("") && !operatorEntry.equals("empty") &&
				!violationCountEntry.equals("") && !violationCountEntry.equals("empty")) {
			if(initialClause) {
				query = query + "WHERE (";
				initialClause = false;
			}
			else {
				query = query + " AND " ;
			}
			query = query + " " + KEY_VIOLATION_IN_YEAR + " " + operatorEntry + " " + violationCountEntry + " ";

		}

		if(!favouriteEntry.equals("") && !favouriteEntry.equals("empty")) {
			int fav;
			if (favouriteEntry.equals(context.getString(R.string.True))) {
				fav = 1;
			}
			else {
				fav = 0;
			}

			if(initialClause) {
				query = query + "WHERE (";
				initialClause = false;
			}
			else {
				query = query + " AND " ;
			}
			query = query + " " + KEY_FAVOURITE + " = " + fav + " ";
		}

		if (!initialClause) {
			query = query + ");";
		}

		return query;
	}

	public int getPrimaryKeys() {
		return primaryKeys;
	}

	public void savePrimaryKey() {
		SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_PRIMARY_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(SHARED_PREF_PRIMARY_KEY_KEY, primaryKeys);
		editor.apply();
	}

	// Add a new set of values to the database.
	public long insertRow(int rID, String restaurant, String hazard, int violationNum, boolean favourite) {
		// Create row's data:
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ROWID, rID);
		initialValues.put(KEY_RES_NAME, restaurant);
		initialValues.put(KEY_RECENT_HAZARD_LVL, hazard);
		initialValues.put(KEY_VIOLATION_IN_YEAR, violationNum);
		initialValues.put(KEY_FAVOURITE, boolToInt(favourite));
		primaryKeys++;
		// Insert it into the database.
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	
	// Delete a row from the database, by rowId (primary key)
	public boolean deleteRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		return db.delete(DATABASE_TABLE, where, null) != 0;
	}
	
	public void deleteAll() {
		Cursor c = getAllRows();
		long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
		if (c.moveToFirst()) {
			do {
				deleteRow(c.getLong((int) rowId));				
			} while (c.moveToNext());
		}
		c.close();
	}
	
	// Return all data in the database.
	public Cursor getAllRows() {
		String where = null;
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, 
							where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by rowId)
	public Cursor getRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, 
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	//citation: https://stackoverflow.com/questions/3793650/convert-boolean-to-int-in-java
	private int boolToInt(boolean b) {
		return b ? 1 : 0;
	}

	// Change an existing row to be equal to new data.
	public boolean updateRow(long rowId, String restaurant, String hazard, int violationNum, boolean favourite) {
		String where = KEY_ROWID + "=" + rowId;

		// Create row's data:
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_RES_NAME, restaurant);
		newValues.put(KEY_RECENT_HAZARD_LVL, hazard);
		newValues.put(KEY_VIOLATION_IN_YEAR, violationNum);
		newValues.put(KEY_FAVOURITE, boolToInt(favourite));
		
		// Insert it into the database.
		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
	}

	public boolean updateFavourite(long rowId, boolean favourite) {
		String where = KEY_ROWID + "=" + rowId;

		// Create row's data:
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_FAVOURITE, boolToInt(favourite));

		// Insert it into the database.
		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
	}


	/////////////////////////////////////////////////////////////////////
	//	Private Helper Classes:
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Private class which handles database creation and upgrading.
	 * Used to handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE_SQL);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data!");
			
			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
			// Recreate new database:
			onCreate(_db);
		}
	}
}
