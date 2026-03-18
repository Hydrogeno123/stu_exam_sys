package com.itheima.service;

import com.itheima.pojo.Clazz;
import com.itheima.pojo.PageResult;

import java.util.List;

/**
 * 班级Service接口
 */
public interface ClazzService {
    /**
     * 分页查询班级
     */
    PageResult<Clazz> page(String clazzCode, String clazzName, Integer teacherId, Integer status, Integer page, Integer pageSize);

    /**
     * 新增班级
     */
    void save(Clazz clazz);

    /**
     * 根据ID查询班级
     */
    Clazz getById(Integer id);

    /**
     * 修改班级
     */
    void update(Clazz clazz);

    /**
     * 删除班级
     */
    void delete(List<Integer> ids);

    /**
     * 根据教师ID查询班级列表
     */
    List<Clazz> findByTeacherId(Integer teacherId);

    /**
     * 查询所有班级（用于下拉选择）
     */
    List<Clazz> findAll();

    /**
     * 统计班级数量
     */
    Integer countActiveClazz();
}