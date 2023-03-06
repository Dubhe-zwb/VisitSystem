package com.tonsail.visit.dao.db;

/**
 * 数据库管理类
 */
public class DBManagement {
    public static final String DB_NAME = "user.db";
    public static final String USER_TABLE = "user_table";

    public static final String SCENE_TABLE_CREATE = "create table if not exists "
            + USER_TABLE
            + "("
            + " id integer primary key autoincrement,"
            + " nickName varchar(30),"
            + " account varchar(30),"
            + " password varchar(100),"
            + " token varchar(200)"
            + ")";

    /**
     * 获取要创建的DB
     * @return
     */
    public static String[] getCreateTable() {

        return new String[]{SCENE_TABLE_CREATE};
    }
}
