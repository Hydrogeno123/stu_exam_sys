package com.itheima.service;

import com.itheima.pojo.StudentAnswer;

import java.util.List;

public interface StudentAnswerService {
    
    /**
     * 保存答题记录
     */
    void save(StudentAnswer answer);
    
    /**
     * 批量保存答题记录
     */
    void batchSave(List<StudentAnswer> answers);
    
    /**
     * 根据考试ID和学生ID查询答题记录
     */
    List<StudentAnswer> findByExamAndStudent(Integer examId, Integer studentId);
    
    /**
     * 根据考试ID和学生ID删除答题记录
     */
    void deleteByExamAndStudent(Integer examId, Integer studentId);
}
