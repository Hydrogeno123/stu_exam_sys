package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 考试提交DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmitDTO {
    private Integer examId; //考试ID
    private Integer studentId; //学生ID
    private List<AnswerItem> answers; //答题项目
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {
        private Integer questionId; //题目ID
        private String answer; //学生答案
    }
}
