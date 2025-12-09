package com.itheima.service;

import com.itheima.pojo.ExamPaper;
import com.itheima.pojo.PageResult;

import java.util.List;

public interface ExamPaperService {
    /**
     * 分页查询考试的试卷
     */
    PageResult<ExamPaper> page(Integer examId, Integer page, Integer pageSize);

    /**
     * 保存试卷
     */
    void save(ExamPaper examPaper);

    /**
     * 根据ID查询试卷
     */
    ExamPaper getById(Integer id);

    /**
     * 修改试卷
     */
    void update(ExamPaper examPaper);

    /**
     * 删除试卷
     */
    void delete(List<Integer> ids);

    /**
     * 根据考试ID查询试卷
     */
    List<ExamPaper> findByExamId(Integer examId);
}
