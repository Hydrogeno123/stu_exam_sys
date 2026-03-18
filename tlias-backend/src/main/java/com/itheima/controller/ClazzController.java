package com.itheima.controller;

import com.itheima.pojo.Clazz;
import com.itheima.pojo.PageResult;
import com.itheima.pojo.Result;
import com.itheima.service.ClazzService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/clazz")
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    /**
     * 分页查询班级列表
     */
    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer pageSize,
                      String clazzCode, String clazzName, Integer teacherId, Integer status) {
        log.info("分页查询班级列表: page={}, pageSize={}, clazzCode={}, clazzName={}, teacherId={}, status={}",
                page, pageSize, clazzCode, clazzName, teacherId, status);
        
        PageResult<Clazz> pageResult = clazzService.page(clazzCode, clazzName, teacherId, status, page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 新增班级
     */
    @PostMapping
    public Result save(@RequestBody Clazz clazz) {
        log.info("新增班级: {}", clazz);
        clazzService.save(clazz);
        return Result.success();
    }

    /**
     * 根据ID查询班级
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        log.info("根据ID查询班级: id={}", id);
        Clazz clazz = clazzService.getById(id);
        return Result.success(clazz);
    }

    /**
     * 修改班级
     */
    @PutMapping
    public Result update(@RequestBody Clazz clazz) {
        log.info("修改班级: {}", clazz);
        clazzService.update(clazz);
        return Result.success();
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Integer> ids) {
        log.info("删除班级: ids={}", ids);
        clazzService.delete(ids);
        return Result.success();
    }

    /**
     * 根据教师ID查询班级列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result findByTeacherId(@PathVariable Integer teacherId) {
        log.info("根据教师ID查询班级列表: teacherId={}", teacherId);
        List<Clazz> clazzList = clazzService.findByTeacherId(teacherId);
        return Result.success(clazzList);
    }

    /**
     * 查询所有班级（用于下拉选择）
     */
    @GetMapping("/all")
    public Result findAll() {
        log.info("查询所有班级");
        List<Clazz> clazzList = clazzService.findAll();
        return Result.success(clazzList);
    }

    /**
     * 统计班级数量
     */
    @GetMapping("/count")
    public Result countActiveClazz() {
        log.info("统计班级数量");
        Integer count = clazzService.countActiveClazz();
        return Result.success(count);
    }
}