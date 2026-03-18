package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaper {
    private Integer id; //ID,主键
    private Integer examId; //考试ID
    private Integer questionId; //题目ID
    private Integer questionOrder; //题目顺序
    private LocalDateTime createTime; //创建时间
    
    // 关联字段
    private Question question; //题目详情
}