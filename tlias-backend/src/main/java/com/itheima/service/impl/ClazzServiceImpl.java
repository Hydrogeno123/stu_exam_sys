package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.ClazzMapper;
import com.itheima.pojo.Clazz;
import com.itheima.pojo.PageResult;
import com.itheima.service.ClazzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级Service实现类
 */
@Service
public class ClazzServiceImpl implements ClazzService {

    @Autowired
    private ClazzMapper clazzMapper;

    @Override
    public PageResult<Clazz> page(String clazzCode, String clazzName, Integer teacherId, Integer status, Integer page, Integer pageSize) {
        // 设置分页参数
        PageHelper.startPage(page, pageSize);
        
        // 执行查询
        List<Clazz> clazzList = clazzMapper.list(clazzCode, clazzName, teacherId, status);
        Page<Clazz> p = (Page<Clazz>) clazzList;
        
        // 封装分页结果
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public void save(Clazz clazz) {
        // 检查班级编号是否已存在
        Clazz existingClazz = clazzMapper.findByCode(clazz.getClazzCode());
        if (existingClazz != null) {
            throw new RuntimeException("班级编号已存在");
        }
        
        // 设置创建时间和更新时间
        clazz.setCreateTime(LocalDateTime.now());
        clazz.setUpdateTime(LocalDateTime.now());
        
        // 新增班级
        clazzMapper.insert(clazz);
    }

    @Override
    public Clazz getById(Integer id) {
        return clazzMapper.getById(id);
    }

    @Override
    public void update(Clazz clazz) {
        // 检查班级编号是否已存在（排除当前班级）
        Clazz existingClazz = clazzMapper.findByCode(clazz.getClazzCode());
        if (existingClazz != null && !existingClazz.getId().equals(clazz.getId())) {
            throw new RuntimeException("班级编号已存在");
        }
        
        // 设置更新时间
        clazz.setUpdateTime(LocalDateTime.now());
        
        // 更新班级
        clazzMapper.update(clazz);
    }

    @Override
    public void delete(List<Integer> ids) {
        // 检查班级下是否有学生
        for (Integer id : ids) {
            Clazz clazz = clazzMapper.getById(id);
            if (clazz != null && clazz.getStudentCount() != null && clazz.getStudentCount() > 0) {
                throw new RuntimeException("班级" + clazz.getClazzName() + "下还有学生，不能删除");
            }
        }
        
        // 删除班级
        clazzMapper.deleteByIds(ids);
    }

    @Override
    public List<Clazz> findByTeacherId(Integer teacherId) {
        return clazzMapper.findByTeacherId(teacherId);
    }

    @Override
    public List<Clazz> findAll() {
        return clazzMapper.findAll();
    }

    @Override
    public Integer countActiveClazz() {
        return clazzMapper.countActiveClazz();
    }
}