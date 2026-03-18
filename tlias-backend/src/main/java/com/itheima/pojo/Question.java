package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private Integer id; //ID,主键
    private Integer bankId; //题库ID
    private String bankName; //题库名称（非数据库字段）
    private Integer type; //题目类型, 1:选择题, 2:填空题
    private String content; //题目内容
    private String options; //选择题选项（JSON格式）
    private String answer; //正确答案
    private Integer score; //题目分值
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //修改时间
}