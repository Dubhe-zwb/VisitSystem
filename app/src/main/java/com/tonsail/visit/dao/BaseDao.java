package com.tonsail.visit.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import com.tonsail.visit.dao.db.DBManagement;
import com.tonsail.visit.dao.db.DBOpenHelper;
import com.tonsail.visit.utils.BeanUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Dao基类：管理基本的CRUD
 */
public class BaseDao {
    private final String TAG = "BaseDao";
    private SQLiteDatabase db;
    private Context context;
    private DBOpenHelper dbOpenHelper;

    private static BaseDao baseDao;

    public static BaseDao getInstance(){
        if (baseDao == null){
            baseDao = new BaseDao();
        }
        return baseDao;
    }

    public void initDao(Context context){
        this.context = context;
        open(DBManagement.DB_NAME, DBManagement.getCreateTable(), DBManagement.DB_NAME);
    }

    /**
     * 打开数据库
     * @param dbName 数据库名
     * @param createString 建表语句
     * @param tableName 表名
     * @throws SQLiteException
     */
    public void open(String dbName, String[] createString ,String tableName) throws SQLiteException {
        if (db != null){
            return;
        }
        Log.e(TAG, "dbName: "+ dbName + " createString: "+ createString[0] + " tableName: "+ tableName);
        dbOpenHelper = new DBOpenHelper(context, dbName, createString , tableName);
        try {
            db = dbOpenHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = dbOpenHelper.getReadableDatabase();
        }
    }

    /**
     * 插入数据
     * @param object 数据对象
     * @param except 排除插入的key
     * @return
     */
    public long insert(String DB_TABLE , Object object , String except) {
        ContentValues newValues = new ContentValues();	//类似HashTable，存储键值对
        fillContentValuse(object, newValues,except);
        return db.insert(DB_TABLE, null, newValues);
    }

    /**
     * 清除数据表的内容
     * @return
     */
    public long deleteAllData(String DB_TABLE) {
        return db.delete(DB_TABLE, null, null);
    }

    /**
     * 删除数据
     * @param key
     * @param id
     * @return
     */
    public long deleteOneData(String DB_TABLE, String key, long id) {
        return db.delete(DB_TABLE, key + "=" + id, null);
    }

    public long updateOneData(String DB_TABLE , String key, long id, Object object) {
        ContentValues updateValues = new ContentValues();
        fillContentValuse(object, updateValues,null);
        return db.update(DB_TABLE, updateValues, key + "=" + id, null);
    }


    public ArrayList<Object> queryOneData(String DB_TABLE, Class<?> targetClass, String condition) {
        String[] keys = getTableFieldName(targetClass);
        Cursor results = db.query(DB_TABLE, keys, condition , null, null, null, null);
        ArrayList<Object> list = ConvertToModel(results , targetClass);
        results.close();
        return list;
    }

    /**
     * 将数据库匹配的结果全部映射到对应的实例中返回
     * @param cursor
     * @param targetClass
     * @return
     */
    private ArrayList<Object> ConvertToModel(Cursor cursor, Class<?> targetClass) {
        int resultCounts = cursor.getCount();	//匹配的总数
        //moveToFirst will return false if the cursor is empty
        if (resultCounts == 0 || !cursor.moveToFirst()) {
            return null;
        }
        ArrayList<Object> objectList = new ArrayList<>();
        Field[] fields = targetClass.getDeclaredFields();	//字段
        Method[] methods = targetClass.getDeclaredMethods();	//方法

        try {
            for (int i = 0; i < resultCounts; i++) {
                Object result = targetClass.newInstance();

                for (Field tempFields : fields) {
                    //根据字段名得到对应下标
                    int columnIndex = cursor.getColumnIndex(tempFields.getName());
                    Class<?> type = tempFields.getType();	//获取一个实例的type

                    Object[] objects = new Object[1];

                    //根据字段下标获取对应的值
                    if(type.getSimpleName().equalsIgnoreCase("String")){
                        objects[0] = cursor.getString(columnIndex);
                    }
                    if(type.getSimpleName().equalsIgnoreCase("double") || type.getName().equalsIgnoreCase("Double")){
                        objects[0]  = cursor.getDouble(columnIndex);
                    }
                    if(type.getSimpleName().equalsIgnoreCase("float") || type.getName().equalsIgnoreCase("Float")){
                        objects[0]  = cursor.getFloat(columnIndex);
                    }
                    if(type.getSimpleName().equalsIgnoreCase("int") || type.getName().equalsIgnoreCase("Integer")){
                        objects[0]  = cursor.getInt(columnIndex);
                    }
                    if(type.getSimpleName().equalsIgnoreCase("Long")|| type.getName().equalsIgnoreCase("long") ){
                        objects[0] = cursor.getLong(columnIndex);
                    }
                    if(type.getSimpleName().equalsIgnoreCase("boolean") || type.getName().equalsIgnoreCase("Boolean")){
                        int intresult = cursor.getInt(columnIndex);
                        if(intresult == 0)
                            objects[0] = false;
                        else
                            objects[0] = true;
                    }

                    String setMethodName = "set"+tempFields.getName();
                    //找到set对应参数的方法
                    Method method = BeanUtil.findMethod(setMethodName, methods);

                    method.invoke(result, objects);
                }

                objectList.add(result);
                cursor.moveToNext();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        cursor.close();
        return objectList;
    }


    private String[] getTableFieldName(Class<?> targetClass) {
        Field[] fields = targetClass.getDeclaredFields();
        String[] keys = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            //过滤掉序列化ID和编译器自动生成的$change 或 在IDE中更改设置
//			if(!(("$change".equals(fields[i].getName()))||("serialVersionUID".equals(fields[i].getName())))){
            keys[i] = fields[i].getName();
//			}
        }
        return keys;
    }


    /**
     * 把进行数据的解析
     * @param object
     * @param newValues
     * @param except
     */
    public void fillContentValuse(Object object, ContentValues newValues, String except) {
        Class<? extends Object> oClass = object.getClass();
        Field[] declaredFields = oClass.getDeclaredFields();
        Method[] declaredMethods = oClass.getDeclaredMethods();
        try {
            for (Field field : declaredFields) {
                String fieldName = field.getName();
                if(except != null && fieldName.equalsIgnoreCase(except)){
                    continue;
                }
                String setMethodName;
                if(field.getType().getName().equalsIgnoreCase("boolean") || field.getType().getName().equalsIgnoreCase("Long"))
                {
                    setMethodName = new StringBuffer().append("is")
                            .append(fieldName).toString();
                }
                else
                {
                    setMethodName = new StringBuffer().append("get")
                            .append(fieldName).toString();
                }
                Method method = BeanUtil.findMethod(setMethodName,
                        declaredMethods);	//找到对应参数名的get方法
                String parameType = method.getReturnType().getSimpleName();		//获取get方法返回类型
                Object value = method.invoke(object, new Object[]{});

                setDifferentParameType(fieldName, parameType, value, newValues);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将参数名和其值的正确类型，保存到ContentValues中
     * @param keyName
     * @param parameType
     * @param value
     * @param newValues
     */
    private void setDifferentParameType(String keyName, String parameType,
                                        Object value, ContentValues newValues) {
        if (parameType.equalsIgnoreCase("string")) {
            newValues.put(keyName, String.valueOf(value));
        }
        if (parameType.equalsIgnoreCase("int") || parameType.equalsIgnoreCase("Integer") ) {
            newValues.put(keyName, (Integer) value);
        }
        if (parameType.equalsIgnoreCase("float") || parameType.equalsIgnoreCase("Float") ) {
            newValues.put(keyName, (Float) value);
        }
        if (parameType.equalsIgnoreCase("double") || parameType.equalsIgnoreCase("Double") ) {
            newValues.put(keyName, (Double) value);
        }
        if (parameType.equalsIgnoreCase("long") || parameType.equalsIgnoreCase("Long") ) {
            newValues.put(keyName, (Long) value);
        }
        if (parameType.equalsIgnoreCase("boolen") || parameType.equalsIgnoreCase("Boolean") ) {
            newValues.put(keyName, (Boolean) value);
        }
//		Log.i( logTag, "value: "+  String.valueOf(value));
    }

    /**
     * 获得数据库对象
     * @return
     */
    public SQLiteDatabase getDB(){
        return db;
    }

    /**
     * Close the database
     */
    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

}
