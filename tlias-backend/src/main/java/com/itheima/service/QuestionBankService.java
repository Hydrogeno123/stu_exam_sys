package com.itheima.service;

import com.itheima.pojo.PageResult;
import com.itheima.pojo.QuestionBank;

import java.util.List;

public interface QuestionBankService {
    /**
     * 分页查询题库
     */
    PageResult<QuestionBank> page(String bankName, Integer page, Integer pageSize);

    /**
     * 新增题库
     */
    void save(QuestionBank questionBank);

    /**
     * 根据ID查询题库
     */
    QuestionBank getById(Integer id);

    /**
     * 修改题库
     */
    void update(QuestionBank questionBank);

    /**
     * 删除题库
     */
    void delete(List<Integer> ids);

    /**
     * 查询所有题库
     */
    List<QuestionBank> findAll();
}