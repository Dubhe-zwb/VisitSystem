package com.tonsail.visit.dao;


import com.tonsail.visit.dao.db.DBManagement;
import com.tonsail.visit.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据表
 */
public class UserDao {
    private final String TAG = "UserDao";
    private static UserDao userDao;
    private String tableName = DBManagement.USER_TABLE;

    public static UserDao getInstance() {
        if (userDao == null) {
            userDao = new UserDao();
        }
        return userDao;
    }

    /**
     * 添加一个用户记录
     * @param user
     * @return
     */
    public int add(User user) {
        long insertId = -1;
        if (user != null) {
            insertId = BaseDao.getInstance()
                    .insert(tableName, user, "id");
            if (insertId != -1) {
                user.setId((int) insertId);
            }
        }
        return (int) insertId;
    }

    public boolean delete(User user) {
        boolean result = false;
        if (user != null) {
            long delCode = BaseDao.getInstance()
                    .deleteOneData(tableName, "id", user.getId());
            if (delCode >= 1) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId
     *          为null时，全查询
     * @return
     */
    public List<User> getUserById(Integer userId) {
        String condition = null;
        if (userId != null){
            condition = "id = " + userId;
        }
        List<User> users = new ArrayList<>();
        List<Object> resultList = BaseDao.getInstance()
                .queryOneData(tableName, User.class, condition);
        if (resultList != null && resultList.size() > 0) {
            for (Object object : resultList) {
                users.add((User) object);
            }
        }
        return users;
    }


}
