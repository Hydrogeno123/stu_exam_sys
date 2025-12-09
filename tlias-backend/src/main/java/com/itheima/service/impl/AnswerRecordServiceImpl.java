package com.itheima.service.impl;

import com.itheima.mapper.AnswerRecordMapper;
import com.itheima.mapper.QuestionMapper;
import com.itheima.pojo.AnswerRecord;
import com.itheima.pojo.Question;
import com.itheima.service.AnswerRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnswerRecordServiceImpl implements AnswerRecordService {
    
    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Override
    public void save(AnswerRecord record) {
        record.setSubmitTime(LocalDateTime.now());
        answerRecordMapper.insert(record);
    }
    
    @Override
    public void batchSave(List<AnswerRecord> records) {
        LocalDateTime now = LocalDateTime.now();
        for (AnswerRecord record : records) {
            record.setSubmitTime(now);
        }
        answerRecordMapper.batchInsert(records);
    }
    
    @Override
    public List<AnswerRecord> findByExamAndStudent(Integer examId, Integer studentId) {
        List<AnswerRecord> records = answerRecordMapper.findByExamAndStudent(examId, studentId);
        // 为每个AnswerRecord填充Question对象
        for (AnswerRecord record : records) {
            Question question = questionMapper.getById(record.getQuestionId());
            record.setQuestion(question);
        }
        return records;
    }
    
    @Override
    public void deleteByExamAndStudent(Integer examId, Integer studentId) {
        answerRecordMapper.deleteByExamAndStudent(examId, studentId);
    }
}
