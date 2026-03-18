package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clazz {
    private Integer id; // ID, 主键
    private String clazzCode; // 班级编号
    private String clazzName; // 班级名称
    private Integer teacherId; // 班主任ID
    private String teacherName; // 班主任姓名（非数据库字段）
    private String description; // 班级描述
    private Integer status; // 状态, 1:启用, 0:停用
    private Integer studentCount; // 学生人数（非数据库字段）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间
}