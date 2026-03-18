package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.exception.BusinessException;
import com.itheima.mapper.UserMapper;
import com.itheima.pojo.PageResult;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String username, String password) {
        return userMapper.findByUsernameAndPassword(username, password);
    }

    @Override
    public PageResult<User> page(String name, String username, Integer role, Integer clazzId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Page<User> p = (Page<User>) userMapper.list(name, username, role, clazzId);
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public void save(User user) {
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public User getById(Integer id) {
        return userMapper.getById(id);
    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void delete(List<Integer> ids) {
        userMapper.deleteByIds(ids);
    }

    @Override
    public List<User> findAllTeachers() {
        return userMapper.findByRole(1); // 1表示教师
    }

    @Override
    public List<User> findAllStudents() {
        return userMapper.findByRole(2); // 2表示学生
    }

    @Override
    public List<User> findStudentsByClazzId(Integer clazzId) {
        return userMapper.findByClazzId(clazzId);
    }

    @Override
    public void updateStudentClazz(Integer studentId, Integer clazzId) {
        userMapper.updateClazz(studentId, clazzId, LocalDateTime.now());
    }

    @Override
    public Integer countStudentsByClazzId(Integer clazzId) {
        return userMapper.countStudentsByClazzId(clazzId);
    }

    @Override
    public void changePassword(User user) {
        // 验证用户是否存在
        User existUser = userMapper.getById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        // 验证原密码是否正确
        if (!user.getPassword().equals(existUser.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        // 更新密码
        existUser.setPassword(user.getNewPassword());
        existUser.setUpdateTime(LocalDateTime.now());
        userMapper.update(existUser);
}
}