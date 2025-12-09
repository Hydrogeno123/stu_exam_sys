package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 答题记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRecord {
    private Integer id; //ID,主键
    private Integer examId; //考试ID
    private Integer studentId; //学生ID
    private Integer questionId; //题目ID
    private String studentAnswer; //学生答案
    private Integer isCorrect; //是否正确, 0:未判, 1:正确, 2:错误
    private Integer score; //得分
    private LocalDateTime submitTime; //提交时间
    
    // 关联字段
    private Question question; //题目详情
}
