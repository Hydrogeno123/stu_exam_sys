package com.itheima.mapper;

import com.itheima.pojo.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名和密码查询用户
     */
    @Select("select * from user where username = #{username} and password = #{password}")
    User findByUsernameAndPassword(String username, String password);

    /**
     * 分页查询用户列表
     */
    List<User> list(@Param("name") String name, @Param("username") String username, @Param("role") Integer role, @Param("clazzId") Integer clazzId);

    /**
     * 新增用户
     */
    @Insert("insert into user(username, password, name, role, phone, clazz_id, create_time, update_time) " +
            "values(#{username}, #{password}, #{name}, #{role}, #{phone}, #{clazzId}, #{createTime}, #{updateTime})")
    void insert(User user);

    /**
     * 根据ID查询用户
     */
    @Select("select * from user where id = #{id}")
    User getById(Integer id);

    /**
     * 修改用户
     */
    @Update("update user set username=#{username}, password=#{password}, name=#{name}, " +
            "role=#{role}, phone=#{phone}, clazz_id=#{clazzId}, update_time=#{updateTime} where id=#{id}")
    void update(User user);

    /**
     * 批量删除用户
     */
    void deleteByIds(List<Integer> ids);

    /**
     * 根据角色查询用户
     */
    @Select("select * from user where role = #{role}")
    List<User> findByRole(Integer role);

    /**
     * 根据班级ID查询学生列表
     */
    @Select("SELECT u.*, c.clazz_name FROM user u LEFT JOIN clazz c ON u.clazz_id = c.id WHERE u.clazz_id = #{clazzId} AND u.role = 2")
    List<User> findByClazzId(Integer clazzId);

    /**
     * 更新学生班级信息
     */
    @Update("UPDATE user SET clazz_id = #{clazzId}, update_time = #{updateTime} WHERE id = #{id}")
    void updateClazz(@Param("id") Integer id, @Param("clazzId") Integer clazzId, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 统计班级学生数量
     */
    @Select("SELECT COUNT(*) FROM user WHERE clazz_id = #{clazzId} AND role = 2")
    Integer countStudentsByClazzId(Integer clazzId);
}