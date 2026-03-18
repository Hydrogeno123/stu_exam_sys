package com.itheima.pojo;

import lombok.Data;

@Data
public class QuestionQueryParam {
    private Integer page = 1; //页码
    private Integer pageSize = 10; //每页展示记录数
    private Integer bankId; //题库ID
    private Integer type; //题目类型
    private String content; //题目内容
}