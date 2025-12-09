package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    private Integer id; //ID,主键
    private String examName; //考试名称
    private Integer teacherId; //创建教师ID
    private Integer bankId; //题库ID
    private Integer clazzId; //班级ID
    private LocalDateTime startTime; //开始时间
    private Integer duration; //考试时长（分钟）
    private Integer choiceCount; //选择题数量
    private Integer fillCount; //填空题数量
    private Integer totalScore; //总分
    private Integer status; //状态, 0:未发布, 1:未开始, 2:进行中, 3:已结束
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //修改时间
    
    // 关联字段
    private String teacherName; //教师姓名
    private String bankName; //题库名称
    private String clazzName; //班级名称
}