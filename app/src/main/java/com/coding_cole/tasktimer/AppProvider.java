package com.coding_cole.tasktimer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.xml.datatype.Duration;

/**
 * Provider for the TaskTimer app, this is the only class that knows about {@link AppDatabase}
 */

public class AppProvider extends ContentProvider {
	private static final String TAG = "AppProvider";

	private AppDatabase mOpenHelper;

	public static final UriMatcher sUriMatcher = buildUriMatcher();

	static final String CONTENT_AUTHORITY = "com.coding_cole.tasktimer.provider";
	public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content:// " + CONTENT_AUTHORITY);

	private static final int TASKS = 100;
	private static final int TASKS_ID = 101;

	private static final int TIMINGS = 200;
	private static final int TIMINGS_ID  = 201;

	/*
	 * private static final int TASKS_TIMINGS = 300;
	 * private static final int TASKS_TIMINGS_ID = 301;
	 */

	private static final int TASKS_DURATION = 400;
	private static final int TASKS_DURATION_ID = 401;

	public static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS);
		matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME + "/#", TASKS_ID);

//		matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS);
//		matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME + "/#", TIMINGS_ID );
//
//		matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASKS_DURATION);
//		matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME + "/#", TASKS_DURATION_ID);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = AppDatabase.getInstance(getContext());
		return true;
	}

	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
		Log.d(TAG,"query: called with uri " + uri);
		final int match = sUriMatcher.match(uri);
		Log.d(TAG, "query: match is " + match);

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		switch (match) {
		  	case TASKS:
				queryBuilder.setTables(TasksContract.TABLE_NAME);
				break;
			case TASKS_ID:
				queryBuilder.setTables(TasksContract.TABLE_NAME);
				long taskId = TasksContract.getTaskId(uri);
				queryBuilder.appendWhere(TasksContract.Coloumns._ID + " = " + taskId);
				break;

//			case TIMINGS:
//				queryBuilder.setTables(TimingsContract.TABLE_NAME);
//				break;
//			case TIMINGS_ID:
//				queryBuilder.setTables(TimingsContract.TABLE_NAME);
//				long timingsId = TasksContract.getTaskId(uri);
//				queryBuilder.appendWhere(TimingsContract.Coloumns._ID + " = " + timingsId);
//				break;
//
//			case TASKS_DURATION:
//				queryBuilder.setTables(DurationsContract.TABLE_NAME);
//				break;
//			case TASKS_DURATION_ID:
//				queryBuilder.setTables(DurationsContract.TABLE_NAME);
//				long durationId = TasksContract.getTaskId(uri);
//				queryBuilder.appendWhere(DurationsContract.Coloumns._ID + " = " + durationId);
//				break;

			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case TASKS:
				return TasksContract.CONTENT_TYPE;

			case TASKS_ID:
				return TasksContract.CONTENT_ITEM_TYPE;

//			case TIMINGS:
//				return TimingsContract.Timings.CONTENT_TYPE;
//
//			case TIMINGS_ID:
//				return TimingsContract.Timings.CONTENT_TYPE;
//
//			case TASKS_DURATION:
//				return DurationsContract.TaskDurations.CONTENT_TYPE;
//
//			case TASKS_DURATION_ID:
//				return DurationsContract.TaskDurations.CONTENT_TYPE;

			default:
				throw new IllegalArgumentException("unknow uri " + uri);
		}
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
		Log.d(TAG, "Entering insert called with uri = " + uri);
		final int match  = sUriMatcher.match(uri);
		Log.d(TAG, "match is " + match);

		final SQLiteDatabase db;

		Uri returnUri;
		long recordId;

		switch (match) {
			case TASKS:
				db = mOpenHelper.getWritableDatabase();
				recordId = db.insert(TasksContract.TABLE_NAME, null, values);
				if (recordId >= 0) {
					returnUri = TasksContract.buildTaskUri(recordId);
				} else {
					throw new android.database.SQLException("Failed to insert into " + uri.toString());
				}
				break;
			case TIMINGS:
				db = mOpenHelper.getWritableDatabase();
//				recordId = db.insert(TimingsContract.Timings.buildTimingUri(recordId)
//				if (recordId >= 0) {
//					returnUri = TimingsContract.Timings.buildTimingUri(recordId);
//				} else {
//					throw new android.database.SQLException("Failed to insert into " + uri.toString());
//				}
//				break;

			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		Log.d(TAG, "Exiting insert, returning " + returnUri);
		return returnUri;
	}

	@Override
	public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
		Log.d(TAG, "update called with uri " + uri);
		final int match = sUriMatcher.match(uri);
		Log.d(TAG, "match id " + match);

		final SQLiteDatabase db;
		int count;

		String selectionCritarial;

		switch (match) {
			case TASKS:
				db = mOpenHelper.getWritableDatabase();
				count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs);
				break;

			case TASKS_ID:
				db = mOpenHelper.getWritableDatabase();
				long taskId = TasksContract.getTaskId(uri);
				selectionCritarial = TasksContract.Coloumns._ID + " = " + taskId;

				if ((selection != null) && (selection.length()>0)) {
					selectionCritarial += " AND (" + selection + ")";
				}
				count = db.delete(TasksContract.TABLE_NAME, selectionCritarial, selectionArgs);
				break;

//			case TIMINGS:
//				db = mOpenHelper.getWritableDatabase();
//				count = db.delete(TimingsContract.TABLE_NAME, selection, selectionArgs);
//				break;
//
//			case TIMINGS_ID:
//				db = mOpenHelper.getWritableDatabase();
//				long timingsId = TimingsContract .getTaskId();
//				selectionCritarial = TimingsContract.Coloumns._ID + " = " + timingsId;
//
//				if ((selection != null) && (selection.length()>0)) {
//					selectionCritarial += " AND (" + selection + ")";
//				}
//				count = db.delete(TimingsContract.TABLE_NAME, selectionCritarial, selectionArgs);
//				break;

			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		Log.d(TAG, "Exiting update,  returning " + count);
		return count;
	}

	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
		Log.d(TAG, "update called with uri " + uri);
		final int match = sUriMatcher.match(uri);
		Log.d(TAG, "match id " + match);

		final SQLiteDatabase db;
		int count;

		String selectionCritarial;

		switch (match) {
			case TASKS:
				db = mOpenHelper.getWritableDatabase();
				count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs);
				break;

			case TASKS_ID:
				db = mOpenHelper.getWritableDatabase();
				long taskId = TasksContract.getTaskId(uri);
				selectionCritarial = TasksContract.Coloumns._ID + " = " + taskId;

				if ((selection != null) && (selection.length()>0)) {
					selectionCritarial += " AND (" + selection + ")";
				}
				count = db.update(TasksContract.TABLE_NAME, values, selectionCritarial, selectionArgs);
				break;

//			case TIMINGS:
//				db = mOpenHelper.getWritableDatabase();
//				count = db.update(TimingsContract.TABLE_NAME, values, selection, selectionArgs);
//				break;
//
//			case TIMINGS_ID:
//				db = mOpenHelper.getWritableDatabase();
//				long timingsId = TimingsContract .getTaskId();
//				selectionCritarial = TimingsContract.Coloumns._ID + " = " + timingsId;
//
//				if ((selection != null) && (selection.length()>0)) {
//					selectionCritarial += " AND (" + selection + ")";
//				}
//				count = db.update(TimingsContract.TABLE_NAME, values, selectionCritarial, selectionArgs);
//				break;

			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		Log.d(TAG, "Exiting update,  returning " + count);
		return count;
	}
}
