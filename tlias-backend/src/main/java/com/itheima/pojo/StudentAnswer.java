package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生答题记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswer {
    private Integer id; //ID,主键
    private Integer examId; //考试ID
    private Integer studentId; //学生ID
    private Integer questionId; //题目ID
    private String studentAnswer; //学生答案
    private String correctAnswer; //正确答案
    private Integer isCorrect; //是否正确, 0:错误, 1:正确
    private Integer questionScore; //题目分值
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //修改时间
    
    // 关联字段
    private Question question; //题目详情
}
