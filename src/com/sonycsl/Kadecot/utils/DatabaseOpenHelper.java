
package com.sonycsl.Kadecot.utils;

import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseOpenHelper.class.getSimpleName();

    public static final String DATA_TYPE_INTEGER = "INTEGER";

    public static final String DATA_TYPE_TEXT = "TEXT";

    public static final String DATA_TYPE_BLOB = "BLOB";

    public static final String OPTION_PRIMARY_KEY_AUTOINCREMENT = " PRIMARY KEY AUTOINCREMENT";

    public static final String OPTION_NOT_NULL = " NOT NULL";

    public String mTableName;

    public Map<String, String> mColumnMap;

    public String[] mColumnNames;

    public DatabaseOpenHelper(Context context, String dbName, int version, String tableName,
        Map<String, String> columns) {
        super(context, dbName, null, version);
        setup(tableName, columns);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    protected void setup(String tableName, Map<String, String> columns) {
        mTableName = tableName;
        mColumnMap = columns;

        Set<String> keySet = mColumnMap.keySet();
        mColumnNames = new String[keySet.size()];
        int i = 0;
        for (String key : keySet) {
            mColumnNames[i] = key;
            i++;
        }
    }

    public void createTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE ");
        buffer.append(mTableName);
        buffer.append(" ( ");
        Set<String> keySet = mColumnMap.keySet();
        int size = keySet.size();
        int i = 0;
        for (String key : keySet) {
            buffer.append(key);
            buffer.append(" ");
            buffer.append(mColumnMap.get(key));
            i++;
            if (i == size) {
                buffer.append(")");
            } else {
                buffer.append(", ");
            }
        }
        String sql = new String(buffer);
        // System.out.println(sql);
        db.execSQL(sql);
    }

    public long insert(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long ret = db.insert(mTableName, null, values);
        return ret;
    }

    public void update(String key, String arg, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(mTableName, values, key + "=?", new String[] {
            arg
        });
    }

    public void update(Where where, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(mTableName, values, where.getClause(), where.args);
    }

    public void delete(String key, String arg) {

        SQLiteDatabase db = getWritableDatabase();
        db.delete(mTableName, key + "=?", new String[] {
            arg
        });
    }

    public void delete(Where where) {
        if (!where.isValid()) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();

        db.delete(mTableName, where.getClause(), where.args);
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(mTableName, null, null);
    }

    public Cursor getCursorByRowId(long rowid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(mTableName, mColumnNames, "rowid=?", new String[] {
            Long.toString(rowid)
        }, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getCursor() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(mTableName, mColumnNames, null, null, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getCursor(String key, String arg) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(mTableName, mColumnNames, key + "=?", new String[] {
            arg
        }, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getCursor(Where where) {
        if (!where.isValid()) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor =
            db.query(mTableName, mColumnNames, where.getClause(), where.args, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getCursor(OrderBy orderBy) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor =
            db.query(mTableName, mColumnNames, null, null, null, null, orderBy.getClause());
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getCursor(Where where, OrderBy orderBy) {

        if (!where.isValid()) {
            return null;
        }
        if (!orderBy.isValid()) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor =
            db.query(mTableName, mColumnNames, where.getClause(), where.args, null, null, orderBy
                .getClause());
        cursor.moveToFirst();
        return cursor;
    }

    public String getString(Cursor cursor, String columnName) {
        if (cursor == null) {
            return null;
        }

        int columnIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(columnIndex)) {
            return null;
        }
        return cursor.getString(columnIndex);
    }

    public Short getShort(Cursor cursor, String columnName) {
        if (cursor == null) {
            return null;
        }

        int columnIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(columnIndex)) {
            return null;
        }
        return cursor.getShort(columnIndex);
    }

    public Integer getInt(Cursor cursor, String columnName) {
        if (cursor == null) {
            return null;
        }

        int columnIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(columnIndex)) {
            return null;
        }
        return cursor.getInt(columnIndex);
    }

    public Long getLong(Cursor cursor, String columnName) {
        if (cursor == null) {
            return null;
        }

        int columnIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(columnIndex)) {
            return null;
        }
        return cursor.getLong(columnIndex);
    }

    public synchronized byte[] getBlob(Cursor cursor, String columnName) {
        if (cursor == null) {
            return null;
        }

        int columnIndex = cursor.getColumnIndex(columnName);
        if (cursor.isNull(columnIndex)) {
            return null;
        }
        return cursor.getBlob(columnIndex);
    }

    public synchronized boolean contains(String key, String arg) {
        Cursor cursor = this.getCursor(key, arg);
        boolean ret = (cursor.getCount() != 0);

        cursor.close();
        return ret;
    }

    public synchronized boolean contains(Where where) {
        Cursor cursor = this.getCursor(where);
        boolean ret = (cursor.getCount() != 0);

        cursor.close();
        return ret;
    }

    public static class Where {
        public final String[] keys;

        public final String[] args;

        public Where(String key, String arg) {
            this.keys = new String[] {
                key
            };
            this.args = new String[] {
                arg
            };
        }

        public Where(String[] keys, String[] args) {
            this.keys = keys;
            this.args = args;
        }

        public String getClause() {

            String clause = "";
            for (int i = 0; i < keys.length; i++) {
                clause += keys[i] + "=?";
                if (i != keys.length - 1) {
                    clause += " and ";
                }
            }
            return clause;
        }

        public boolean isValid() {
            return keys.length == args.length;
        }
    }

    public static class OrderBy {
        public final String[] keys;

        public final String[] orders;

        public static final String ASC = "asc";

        public static final String DESC = "desc";

        public OrderBy(String key, String order) {
            this.keys = new String[] {
                key
            };
            this.orders = new String[] {
                order
            };
        }

        public OrderBy(String[] keys, String[] orders) {
            this.keys = keys;
            this.orders = orders;
        }

        public boolean isValid() {
            return keys.length == orders.length;
        }

        public String getClause() {

            String clause = "";
            for (int i = 0; i < keys.length; i++) {
                clause += keys[i] + " " + orders[i];
                if (i != keys.length - 1) {
                    clause += ",";
                }
            }
            return clause;
        }

    }

}
