package ekylibre.zero;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import ekylibre.zero.util.SelectionBuilder;

public class ZeroProvider extends ContentProvider {
    // Routes codes


    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using URI_MATCHER.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through URI_MATCHER, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    public static final int ROUTE_CRUMB_LIST = 100;
    public static final int ROUTE_CRUMB_ITEM = 101;
    public static final int ROUTE_ISSUE_LIST = 200;
    public static final int ROUTE_ISSUE_ITEM = 201;
    public static final int ROUTE_SAMPLING_LIST = 300;
    public static final int ROUTE_SAMPLING_ITEM = 301;
    public static final int ROUTE_SAMPLING_COUNT_LIST = 400;
    public static final int ROUTE_SAMPLING_COUNT_ITEM = 401;
    // UriMatcher, used to decode incoming URIs.
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(ZeroContract.AUTHORITY, "crumbs", ROUTE_CRUMB_LIST);
        URI_MATCHER.addURI(ZeroContract.AUTHORITY, "crumbs/#", ROUTE_CRUMB_ITEM);
        URI_MATCHER.addURI(ZeroContract.AUTHORITY, "issues", ROUTE_ISSUE_LIST);
        URI_MATCHER.addURI(ZeroContract.AUTHORITY, "issues/#", ROUTE_ISSUE_ITEM);
        URI_MATCHER.addURI(ZeroContract.AUTHORITY, "samplings", ROUTE_SAMPLING_LIST);
        URI_MATCHER.addURI(ZeroContract.AUTHORITY, "samplings/#", ROUTE_SAMPLING_ITEM);
        URI_MATCHER.addURI(ZeroContract.AUTHORITY, "sampling_counts", ROUTE_SAMPLING_COUNT_LIST);
        URI_MATCHER.addURI(ZeroContract.AUTHORITY, "sampling_counts/#", ROUTE_SAMPLING_COUNT_ITEM);
    }

    private DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    // Determine the mime type for records returned by a given URI.
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ROUTE_CRUMB_LIST:
                return ZeroContract.Crumbs.CONTENT_TYPE;
            case ROUTE_CRUMB_ITEM:
                return ZeroContract.Crumbs.CONTENT_ITEM_TYPE;
            case ROUTE_ISSUE_LIST:
                return ZeroContract.Issues.CONTENT_TYPE;
            case ROUTE_ISSUE_ITEM:
                return ZeroContract.Issues.CONTENT_ITEM_TYPE;
            case ROUTE_SAMPLING_LIST:
                return ZeroContract.PlantCounting.CONTENT_TYPE;
            case ROUTE_SAMPLING_ITEM:
                return ZeroContract.PlantCounting.CONTENT_ITEM_TYPE;
            case ROUTE_SAMPLING_COUNT_LIST:
                return ZeroContract.PlantCountingItem.CONTENT_TYPE;
            case ROUTE_SAMPLING_COUNT_ITEM:
                return ZeroContract.PlantCountingItem.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all records (/records) and individual records by ID
     * (/records/{ID}).
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        String id;
        Cursor cursor;
        Context context;
        switch (URI_MATCHER.match(uri)) {
            case ROUTE_CRUMB_ITEM:
                // Return a single crumb, by ID.
                id = uri.getLastPathSegment();
                builder.where(ZeroContract.CrumbsColumns._ID + "=?", id);
            case ROUTE_CRUMB_LIST:
                // Return all known crumbs.
                builder.table(ZeroContract.CrumbsColumns.TABLE_NAME)
                        .where(selection, selectionArgs);
                cursor = builder.query(database, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                context = getContext();
                assert context != null;
                cursor.setNotificationUri(context.getContentResolver(), uri);
                return cursor;

            case ROUTE_ISSUE_ITEM:
                // Return a single ISSUE, by ID.
                id = uri.getLastPathSegment();
                builder.where(ZeroContract.IssuesColumns._ID + "=?", id);
            case ROUTE_ISSUE_LIST:
                // Return all known Issue.
                builder.table(ZeroContract.IssuesColumns.TABLE_NAME)
                        .where(selection, selectionArgs);
                cursor = builder.query(database, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                context = getContext();
                assert context != null;
                cursor.setNotificationUri(context.getContentResolver(), uri);
                return cursor;

            case ROUTE_SAMPLING_ITEM:
                // Return a single ISSUE, by ID.
                id = uri.getLastPathSegment();
                builder.where(ZeroContract.PlantCountingColumns._ID + "=?", id);
            case ROUTE_SAMPLING_LIST:
                // Return all known Issue.
                builder.table(ZeroContract.PlantCountingColumns.TABLE_NAME)
                        .where(selection, selectionArgs);
                cursor = builder.query(database, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                context = getContext();
                assert context != null;
                cursor.setNotificationUri(context.getContentResolver(), uri);
                return cursor;

            case ROUTE_SAMPLING_COUNT_ITEM:
                // Return a single ISSUE, by ID.
                id = uri.getLastPathSegment();
                builder.where(ZeroContract.PlantCountingItemColumns._ID + "=?", id);
            case ROUTE_SAMPLING_COUNT_LIST:                // Return all known Issue.
                builder.table(ZeroContract.PlantCountingItemColumns.TABLE_NAME)
                        .where(selection, selectionArgs);
                cursor = builder.query(database, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                context = getContext();
                assert context != null;
                cursor.setNotificationUri(context.getContentResolver(), uri);
                return cursor;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    /**
     * Insert a new record into the database.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        assert database != null;
        final int match = URI_MATCHER.match(uri);
        Uri result;
        long id;
        switch (match) {
            case ROUTE_CRUMB_LIST:
                id = database.insertOrThrow(ZeroContract.CrumbsColumns.TABLE_NAME, null, values);
                result = Uri.parse(ZeroContract.Crumbs.CONTENT_URI + "/" + id);
                break;
            case ROUTE_CRUMB_ITEM:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_ISSUE_LIST:
                id = database.insertOrThrow(ZeroContract.IssuesColumns.TABLE_NAME, null, values);
                result = Uri.parse(ZeroContract.Issues.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ISSUE_ITEM:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_SAMPLING_LIST:
                id = database.insertOrThrow(ZeroContract.PlantCountingColumns.TABLE_NAME, null, values);
                result = Uri.parse(ZeroContract.PlantCounting.CONTENT_URI + "/" + id);
                break;
            case ROUTE_SAMPLING_ITEM:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_SAMPLING_COUNT_LIST:
                id = database.insertOrThrow(ZeroContract.PlantCountingItemColumns.TABLE_NAME, null, values);
                result = Uri.parse(ZeroContract.PlantCountingItem.CONTENT_URI + "/" + id);
                break;
            case ROUTE_SAMPLING_COUNT_ITEM:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete a record by database by URI.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int count;
        String id;
        switch (match) {
            case ROUTE_CRUMB_LIST:
                count = builder.table(ZeroContract.CrumbsColumns.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(database);
                break;
            case ROUTE_CRUMB_ITEM:
                id = uri.getLastPathSegment();
                count = builder.table(ZeroContract.CrumbsColumns.TABLE_NAME)
                        .where(ZeroContract.CrumbsColumns._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(database);
                break;
            case ROUTE_ISSUE_LIST:
                count = builder.table(ZeroContract.IssuesColumns.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(database);
                break;
            case ROUTE_ISSUE_ITEM:
                id = uri.getLastPathSegment();
                count = builder.table(ZeroContract.IssuesColumns.TABLE_NAME)
                        .where(ZeroContract.IssuesColumns._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(database);
                break;
            case ROUTE_SAMPLING_LIST:
                count = builder.table(ZeroContract.PlantCountingColumns.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(database);
                break;
            case ROUTE_SAMPLING_ITEM:
                id = uri.getLastPathSegment();
                count = builder.table(ZeroContract.PlantCountingColumns.TABLE_NAME)
                        .where(ZeroContract.PlantCountingColumns._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(database);
                break;
            case ROUTE_SAMPLING_COUNT_LIST:
                count = builder.table(ZeroContract.PlantCountingItemColumns.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(database);
                break;
            case ROUTE_SAMPLING_COUNT_ITEM:
                id = uri.getLastPathSegment();
                count = builder.table(ZeroContract.PlantCountingItemColumns.TABLE_NAME)
                        .where(ZeroContract.PlantCountingItemColumns._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(database);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update a record in the database by URI.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int count;
        String id;
        switch (match) {
            case ROUTE_CRUMB_LIST:
                count = builder.table(ZeroContract.CrumbsColumns.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(database, values);
                break;
            case ROUTE_CRUMB_ITEM:
                id = uri.getLastPathSegment();
                count = builder.table(ZeroContract.CrumbsColumns.TABLE_NAME)
                        .where(ZeroContract.CrumbsColumns._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(database, values);
                break;
            case ROUTE_ISSUE_LIST:
                count = builder.table(ZeroContract.IssuesColumns.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(database, values);
                break;
            case ROUTE_ISSUE_ITEM:
                id = uri.getLastPathSegment();
                count = builder.table(ZeroContract.IssuesColumns.TABLE_NAME)
                        .where(ZeroContract.IssuesColumns._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(database, values);
                break;

            case ROUTE_SAMPLING_LIST:
                count = builder.table(ZeroContract.PlantCountingColumns.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(database, values);
                break;
            case ROUTE_SAMPLING_ITEM:
                id = uri.getLastPathSegment();
                count = builder.table(ZeroContract.PlantCountingColumns.TABLE_NAME)
                        .where(ZeroContract.PlantCountingColumns._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(database, values);
                break;

            case ROUTE_SAMPLING_COUNT_LIST:
                count = builder.table(ZeroContract.PlantCountingItemColumns.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(database, values);
                break;
            case ROUTE_SAMPLING_COUNT_ITEM:
                id = uri.getLastPathSegment();
                count = builder.table(ZeroContract.PlantCountingItemColumns.TABLE_NAME)
                        .where(ZeroContract.PlantCountingItemColumns._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(database, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

}