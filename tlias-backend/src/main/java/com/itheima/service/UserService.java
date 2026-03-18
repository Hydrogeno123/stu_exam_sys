package com.itheima.service;

import com.itheima.pojo.PageResult;
import com.itheima.pojo.User;

import java.util.List;

public interface UserService {
    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 分页查询用户
     */
    PageResult<User> page(String name, String username, Integer role, Integer clazzId, Integer page, Integer pageSize);

    /**
     * 新增用户
     */
    void save(User user);

    /**
     * 根据ID查询用户
     */
    User getById(Integer id);

    /**
     * 修改用户
     */
    void update(User user);

    /**
     * 删除用户
     */
    void delete(List<Integer> ids);

    /**
     * 查询所有教师
     */
    List<User> findAllTeachers();

    /**
     * 查询所有学生
     */
    List<User> findAllStudents();

    /**
     * 根据班级ID查询学生列表
     */
    List<User> findStudentsByClazzId(Integer clazzId);

    /**
     * 更新学生班级信息
     */
    void updateStudentClazz(Integer studentId, Integer clazzId);

    /**
     * 统计班级学生数量
     */
    Integer countStudentsByClazzId(Integer clazzId);

    /**
     * 修改密码
     */
    void changePassword(User user);
}