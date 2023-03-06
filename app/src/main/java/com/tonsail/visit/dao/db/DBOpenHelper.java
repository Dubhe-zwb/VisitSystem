package com.tonsail.visit.dao.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库连接类
 */
public class DBOpenHelper extends SQLiteOpenHelper  {
	private String[] DB_CREATE ;
	private String DB_TABLE ;
	private static int version  = 1 ;

	public DBOpenHelper(Context context) {
		super(context, DBManagement.DB_NAME, null, version);
	}

	public DBOpenHelper(Context context, String dbName , String[] createString, String tableName) {
		super(context, dbName, null, getVersion());
		this.DB_CREATE = createString ;
		this.DB_TABLE = tableName;
	}

	public DBOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

    /**
     * 创建数据库同时创建表
	 * @param _db
     */
	@Override
	public void onCreate(SQLiteDatabase _db) {
		for (String createSql : DB_CREATE) {
			_db.execSQL(createSql);
		}
	}

    /**
     * 升级数据库（要调用到onUpgrade时需要在构造函数中将传入的版本号升级）
	 * @param _db
     * @param _oldVersion
     * @param _newVersion
	 */
	@Override
	public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
		_db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
		onCreate(_db);
	}

	public static int getVersion() {
		return version;
	}

	public static void setVersion(int version) {
		DBOpenHelper.version = version;
	}
}
