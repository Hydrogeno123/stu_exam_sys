package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id; //ID,主键
    private String username; //用户名
    private String password; //密码
    private String name; //姓名
    private Integer role; //角色, 0:管理员, 1:教师, 2:学生
    private String phone; //手机号
    private Integer clazzId; //班级ID（学生所属班级）
    private String clazzName; //班级名称（非数据库字段）
    private String newPassword; //新密码（修改密码或拍无数据库字段）
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //修改时间
}