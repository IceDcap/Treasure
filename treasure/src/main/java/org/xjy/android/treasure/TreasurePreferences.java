package org.xjy.android.treasure;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xjy.android.treasure.provider.TreasureContract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TreasurePreferences implements SharedPreferences {
    public static final int TYPE_STRING = 1;
    public static final int TYPE_STRING_SET = 2;
    public static final int TYPE_INT = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_BOOLEAN = 6;

    private Context mContext;
    private String mName;

    public TreasurePreferences(Context context, String name) {
        mContext = context.getApplicationContext();
        mName = name;
    }

    @Override
    public Map<String, ?> getAll() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET_ALL, null), null, null, null, null);
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject(cursor.getString(0));
                for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
                    String key = it.next();
                    map.put(key, jsonObject.get(key));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return map;
    }

    @Nullable
    @Override
    public String getString(String key, String defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, null, null, TYPE_STRING + "");
            while (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, null, null, TYPE_STRING_SET + "");
            while (cursor.moveToNext()) {
                HashSet<String> set = new HashSet<String>();
                JSONArray jsonArray = new JSONArray(cursor.getString(0));
                for (int i = 0, len = jsonArray.length(); i < len; i++) {
                    set.add(jsonArray.getString(i));
                }
                return set;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, defValue + "", null, TYPE_INT + "");
            while (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, defValue + "", null, TYPE_LONG + "");
            while (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, defValue + "", null, TYPE_FLOAT + "");
            while (cursor.moveToNext()) {
                return cursor.getFloat(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, defValue + "", null, TYPE_BOOLEAN + "");
            while (cursor.moveToNext()) {
                return cursor.getInt(0) == 1;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public boolean contains(String key) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_CONTAINS, null), new String[]{key}, null, null, null);
            while (cursor.moveToNext()) {
                return cursor.getInt(0) == 1;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return false;
    }

    @Override
    public Editor edit() {
        return new TreasureEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    private Uri buildUri(String path, HashMap<String, String> params) {
        Uri.Builder builder = TreasureContract.AUTHORITY_URI.buildUpon();
        builder.appendPath(mName).appendPath(path);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    private void closeCursorSilently(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable t) {}
        }
    }

    public final class TreasureEditor implements Editor {
        private final Map<String, Object> mModified = new HashMap<String, Object>();
        private boolean mClear = false;

        @Override
        public Editor putString(String key, String value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            synchronized (this) {
                mModified.put(key, (values == null) ? null : new HashSet<String>(values));
                return this;
            }
        }

        @Override
        public Editor putInt(String key, int value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putLong(String key, long value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putFloat(String key, float value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor remove(String key) {
            synchronized (this) {
                mModified.put(key, null);
                return this;
            }
        }

        @Override
        public Editor clear() {
            synchronized (this) {
                mClear = true;
                return this;
            }
        }

        @Override
        public boolean commit() {
            update(true);
            return true;
        }

        @Override
        public void apply() {
            update(false);
        }

        private void update(boolean immediately) {
            synchronized (this) {
                ContentValues contentValues = new ContentValues();
                String stringSetKey = null;
                String[] stringSetValue = null;
                for (Map.Entry<String, Object> entry : mModified.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value == null) {
                        contentValues.putNull(key);
                    } else if (value instanceof String) {
                        contentValues.put(key, (String) value);
                    } else if (value instanceof HashSet) {
                        stringSetKey = key;
                        HashSet<String> stringSet = (HashSet<String>) value;
                        stringSetValue = new String[stringSet.size()];
                        int i = 0;
                        for (String s : stringSet) {
                            stringSetValue[i++] = s;
                        }
                    } else if (value instanceof Integer) {
                        contentValues.put(key, (Integer) value);
                    } else if (value instanceof Long) {
                        contentValues.put(key, (Long) value);
                    } else if (value instanceof Float) {
                        contentValues.put(key, (Float) value);
                    } else if (value instanceof Boolean) {
                        contentValues.put(key, (Boolean) value);
                    }
                }
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("clear", mClear + "");
                params.put("immediately", immediately + "");
                mContext.getContentResolver().update(buildUri(TreasureContract.UPDATE, params), contentValues, stringSetKey, stringSetValue);
            }
        }
    }
}