package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamScore {
    private Integer id; //ID,主键
    private Integer examId; //考试ID
    private Integer studentId; //学生ID
    private Integer totalScore; //总得分
    private Integer correctCount; //正确题数
    private LocalDateTime submitTime; //提交时间
    private Integer status; //状态, 1:未提交, 2:已提交
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //修改时间
    
    // 关联字段
    private String studentName; //学生姓名
    private String examName; //考试名称
    private Integer totalQuestions; //总题数
    private String clazzName; //班级名称
}

