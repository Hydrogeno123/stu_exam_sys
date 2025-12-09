package com.itheima.service;

import com.itheima.pojo.Exam;
import com.itheima.pojo.ExamQueryParam;
import com.itheima.pojo.ExamSubmitDTO;
import com.itheima.pojo.PageResult;

import java.util.List;
import java.util.Map;

public interface ExamService {
    /**
     * 分页查询考试
     */
    PageResult<Exam> page(ExamQueryParam queryParam);

    /**
     * 新增考试
     */
    void save(Exam exam);

    /**
     * 根据ID查询考试
     */
    Exam getById(Integer id);

    /**
     * 修改考试
     */
    void update(Exam exam);

    /**
     * 删除考试
     */
    void delete(List<Integer> ids);

    /**
     * 根据教师ID查询考试列表
     */
    List<Exam> findByTeacherId(Integer teacherId);

    /**
     * 根据学生ID查询可参加的考试
     */
    List<Exam> findAvailableExams(Integer studentId);

    /**
     * 生成试卷
     */
    void generateExamPaper(Integer examId);

    /**
     * 更新考试状态
     */
    void updateExamStatus(Integer examId, Integer status);
    
    /**
     * 获取学生考试试卷
     */
    Map<String, Object> getExamPaperForStudent(Integer examId, Integer studentId);
    
    /**
     * 学生提交答题
     */
    Map<String, Object> submitExam(ExamSubmitDTO submitDTO);
}