package com.itheima.mapper;

import com.itheima.pojo.Clazz;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 班级Mapper接口
 */
@Mapper
public interface ClazzMapper {

    /**
     * 分页查询班级列表
     */
    List<Clazz> list(@Param("clazzCode") String clazzCode, 
                     @Param("clazzName") String clazzName,
                     @Param("teacherId") Integer teacherId,
                     @Param("status") Integer status);

    /**
     * 根据ID查询班级
     */
    @Select("SELECT c.*, u.name as teacher_name, " +
            "(SELECT COUNT(*) FROM user WHERE clazz_id = c.id AND role = 2) as student_count " +
            "FROM clazz c " +
            "LEFT JOIN user u ON c.teacher_id = u.id " +
            "WHERE c.id = #{id}")
    Clazz getById(Integer id);

    /**
     * 新增班级
     */
    @Insert("INSERT INTO clazz(clazz_code, clazz_name, teacher_id, description, status, create_time, update_time) " +
            "VALUES(#{clazzCode}, #{clazzName}, #{teacherId}, #{description}, #{status}, #{createTime}, #{updateTime})")
    void insert(Clazz clazz);

    /**
     * 修改班级
     */
    @Update("UPDATE clazz SET clazz_code=#{clazzCode}, clazz_name=#{clazzName}, teacher_id=#{teacherId}, " +
            "description=#{description}, status=#{status}, update_time=#{updateTime} WHERE id=#{id}")
    void update(Clazz clazz);

    /**
     * 批量删除班级
     */
    void deleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据教师ID查询班级列表
     */
    @Select("SELECT c.*, u.name as teacher_name, " +
            "(SELECT COUNT(*) FROM user WHERE clazz_id = c.id AND role = 2) as student_count " +
            "FROM clazz c " +
            "LEFT JOIN user u ON c.teacher_id = u.id " +
            "WHERE c.teacher_id = #{teacherId}")
    List<Clazz> findByTeacherId(Integer teacherId);

    /**
     * 查询所有班级（用于下拉选择）
     */
    @Select("SELECT id, clazz_code, clazz_name FROM clazz WHERE status = 1 ORDER BY clazz_code")
    List<Clazz> findAll();

    /**
     * 根据班级编号查询班级
     */
    @Select("SELECT * FROM clazz WHERE clazz_code = #{clazzCode}")
    Clazz findByCode(String clazzCode);

    /**
     * 统计班级数量
     */
    @Select("SELECT COUNT(*) FROM clazz WHERE status = 1")
    Integer countActiveClazz();
}