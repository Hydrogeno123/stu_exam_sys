package com.itheima.service;

import com.itheima.pojo.PageResult;
import com.itheima.pojo.Question;
import com.itheima.pojo.QuestionQueryParam;

import java.util.List;

public interface QuestionService {
    /**
     * 分页查询题目
     */
    PageResult<Question> page(QuestionQueryParam queryParam);

    /**
     * 查询所有题目
     */
    List<Question> findAll();

    /**
     * 新增题目
     */
    void save(Question question);

    /**
     * 根据ID查询题目
     */
    Question getById(Integer id);

    /**
     * 修改题目
     */
    void update(Question question);

    /**
     * 删除题目
     */
    void delete(List<Integer> ids);

    /**
     * 根据题库ID查询题目列表
     */
    List<Question> findByBankId(Integer bankId);

    /**
     * 随机获取题目
     */
    List<Question> getRandomQuestions(Integer bankId, Integer type, Integer count);
}