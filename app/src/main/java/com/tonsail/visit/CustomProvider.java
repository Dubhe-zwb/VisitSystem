package com.tonsail.visit;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tonsail.visit.dao.BaseDao;
import com.tonsail.visit.dao.db.DBManagement;


/**
 * 自定义ContentProvider
 * @author: Ivan
 * @date: 2020/7/30
 */
public class CustomProvider extends ContentProvider {

    private Context mContext;

    /**
     * ContentProvider唯一认证
     */
    public static final String AUTHORIZE = "com.tonsail.visit.CustomProvider";

    public static final int User_Code = 1;
    //初始化UriMatcher:在ContentProvider 中注册URI
    private static final UriMatcher mMatcher;
    static{
        // 初始化
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 若URI资源路径 = content://com.shiting.loginprovider/user_table ，则返回注册码User_Code
        mMatcher.addURI(AUTHORIZE, DBManagement.USER_TABLE, User_Code);
    }

    /**
     * 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
     */
    private String getTableName(Uri uri){
        String tableName = null;
        switch (mMatcher.match(uri)) {
            case User_Code:
                tableName = DBManagement.USER_TABLE;
                break;
        }
        return tableName;
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        BaseDao.getInstance().initDao(mContext);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String table = getTableName(uri);
        //获取db连接类
        SQLiteDatabase db = BaseDao.getInstance().getDB();
        if (db == null){
            Log.e("CustomProvider query", "db is not open");
            return null;
        }
        //通过ContentUris类从URL中获取ID
//        long personid = ContentUris.parseId(uri);

        return db.query(table,projection,selection,selectionArgs,null,null,sortOrder,null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
//        String table = getTableName(uri);

        // 向该表添加数据
//        UserDao.getInstance().add(table, null, values);

        // 当该URI的ContentProvider数据发生变化时，通知外界（即访问该ContentProvider数据的访问者）
//        mContext.getContentResolver().notifyChange(uri, null);

//        // 通过ContentUris类从URL中获取ID
//        long personid = ContentUris.parseId(uri);

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = getTableName(uri);
        //获取db连接类
        SQLiteDatabase db = BaseDao.getInstance().getDB();

        return db.delete(table, selection, selectionArgs);
//        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
