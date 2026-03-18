package com.itheima.service.impl;

import com.itheima.mapper.StudentAnswerMapper;
import com.itheima.pojo.StudentAnswer;
import com.itheima.service.StudentAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentAnswerServiceImpl implements StudentAnswerService {
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;
    
    @Override
    public void save(StudentAnswer answer) {
        answer.setCreateTime(LocalDateTime.now());
        answer.setUpdateTime(LocalDateTime.now());
        studentAnswerMapper.insert(answer);
    }
    
    @Override
    public void batchSave(List<StudentAnswer> answers) {
        LocalDateTime now = LocalDateTime.now();
        for (StudentAnswer answer : answers) {
            answer.setCreateTime(now);
            answer.setUpdateTime(now);
        }
        studentAnswerMapper.batchInsert(answers);
    }
    
    @Override
    public List<StudentAnswer> findByExamAndStudent(Integer examId, Integer studentId) {
        return studentAnswerMapper.findByExamAndStudent(examId, studentId);
    }
    
    @Override
    public void deleteByExamAndStudent(Integer examId, Integer studentId) {
        studentAnswerMapper.deleteByExamAndStudent(examId, studentId);
    }
}
