package com.itheima.service;

import com.itheima.pojo.AnswerRecord;

import java.util.List;

public interface AnswerRecordService {
    
    /**
     * 保存答题记录
     */
    void save(AnswerRecord record);
    
    /**
     * 批量保存答题记录
     */
    void batchSave(List<AnswerRecord> records);
    
    /**
     * 根据考试ID和学生ID查询答题记录
     */
    List<AnswerRecord> findByExamAndStudent(Integer examId, Integer studentId);
    
    /**
     * 根据考试ID和学生ID删除答题记录
     */
    void deleteByExamAndStudent(Integer examId, Integer studentId);
}
