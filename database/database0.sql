
create database if not exists stu_exam1 CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
use stu_exam1;



-- 用户表（扩展为教师和学生）
CREATE TABLE user (
    id int unsigned PRIMARY KEY AUTO_INCREMENT COMMENT 'ID, 主键',
    username varchar(20) NOT NULL UNIQUE COMMENT '用户名',
    password varchar(50) DEFAULT '123456' COMMENT '密码',
    name varchar(10) NOT NULL COMMENT '姓名',
    role tinyint unsigned NOT NULL COMMENT '角色, 0:管理员, 1:教师, 2:学生',
    phone char(11) UNIQUE COMMENT '手机号',
    clazz_id int unsigned DEFAULT NULL COMMENT '班级ID（学生所属班级）',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_time datetime DEFAULT NULL COMMENT '修改时间',
    FOREIGN KEY (clazz_id) REFERENCES clazz(id)
) COMMENT '用户表';


-- 班级表
CREATE TABLE clazz (
    id int unsigned PRIMARY KEY AUTO_INCREMENT COMMENT 'ID, 主键',
    clazz_code varchar(20) NOT NULL UNIQUE COMMENT '班级编号',
    clazz_name varchar(50) NOT NULL COMMENT '班级名称',
    teacher_id int unsigned NOT NULL COMMENT '班主任ID',
    description varchar(200) COMMENT '班级描述',
    status tinyint unsigned DEFAULT 1 COMMENT '状态, 1:启用, 0:停用',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_time datetime DEFAULT NULL COMMENT '修改时间',
    FOREIGN KEY (teacher_id) REFERENCES user(id)
) COMMENT '班级表';

-- 题库表
CREATE TABLE question_bank (
    id int unsigned PRIMARY KEY AUTO_INCREMENT COMMENT 'ID, 主键',
    bank_name varchar(50) NOT NULL COMMENT '题库名称',
    description varchar(200) COMMENT '题库描述',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_time datetime DEFAULT NULL COMMENT '修改时间'
) COMMENT '题库表';

-- 题目表
CREATE TABLE question (
    id int unsigned PRIMARY KEY AUTO_INCREMENT COMMENT 'ID, 主键',
    bank_id int unsigned NOT NULL COMMENT '题库ID',
    type tinyint unsigned NOT NULL COMMENT '题目类型, 1:选择题, 2:填空题',
    content text NOT NULL COMMENT '题目内容',
    options text COMMENT '选择题选项（JSON格式）',
    answer varchar(500) NOT NULL COMMENT '正确答案',
    score int unsigned DEFAULT 5 COMMENT '题目分值',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_time datetime DEFAULT NULL COMMENT '修改时间',
    FOREIGN KEY (bank_id) REFERENCES question_bank(id)
) COMMENT '题目表';

-- 考试表
CREATE TABLE exam (
    id int unsigned PRIMARY KEY AUTO_INCREMENT COMMENT 'ID, 主键',
    exam_name varchar(100) NOT NULL COMMENT '考试名称',
    teacher_id int unsigned NOT NULL COMMENT '创建教师ID',
    bank_id int unsigned NOT NULL COMMENT '题库ID',
    clazz_id int unsigned NOT NULL COMMENT '班级ID',
    start_time datetime NOT NULL COMMENT '开始时间',
    duration int unsigned NOT NULL COMMENT '考试时长（分钟）',
    choice_count int unsigned DEFAULT 0 COMMENT '选择题数量',
    fill_count int unsigned DEFAULT 0 COMMENT '填空题数量',
    total_score int unsigned DEFAULT 100 COMMENT '总分',
    status tinyint unsigned DEFAULT 0 COMMENT '状态, 0:未发布, 1:未开始, 2:进行中, 3:已结束',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_time datetime DEFAULT NULL COMMENT '修改时间',
    FOREIGN KEY (teacher_id) REFERENCES user(id),
    FOREIGN KEY (bank_id) REFERENCES question_bank(id),
    FOREIGN KEY (clazz_id) REFERENCES clazz(id)
) COMMENT '考试表';

-- 试卷表（考试与题目的关联）
CREATE TABLE exam_paper (
    id int unsigned PRIMARY KEY AUTO_INCREMENT COMMENT 'ID, 主键',
    exam_id int unsigned NOT NULL COMMENT '考试ID',
    question_id int unsigned NOT NULL COMMENT '题目ID',
    question_order int unsigned NOT NULL COMMENT '题目顺序',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id)
) COMMENT '试卷表';

-- 答题记录表
CREATE TABLE answer_record (
    id int unsigned PRIMARY KEY AUTO_INCREMENT COMMENT 'ID, 主键',
    exam_id int unsigned NOT NULL COMMENT '考试ID',
    student_id int unsigned NOT NULL COMMENT '学生ID',
    question_id int unsigned NOT NULL COMMENT '题目ID',
    student_answer text COMMENT '学生答案',
    is_correct tinyint unsigned DEFAULT 0 COMMENT '是否正确, 0:未判, 1:正确, 2:错误',
    score int unsigned DEFAULT 0 COMMENT '得分',
    submit_time datetime DEFAULT NULL COMMENT '提交时间',
    FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (student_id) REFERENCES user(id),
    FOREIGN KEY (question_id) REFERENCES question(id)
) COMMENT '答题记录表';

-- 成绩表
CREATE TABLE exam_score (
    id int unsigned PRIMARY KEY AUTO_INCREMENT COMMENT 'ID, 主键',
    exam_id int unsigned NOT NULL COMMENT '考试ID',
    student_id int unsigned NOT NULL COMMENT '学生ID',
    total_score int unsigned DEFAULT 0 COMMENT '总得分',
    correct_count int unsigned DEFAULT 0 COMMENT '正确题数',
    submit_time datetime DEFAULT NULL COMMENT '提交时间',
    status tinyint unsigned DEFAULT 1 COMMENT '状态, 1:未提交, 2:已提交',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_time datetime DEFAULT NULL COMMENT '修改时间',
    FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (student_id) REFERENCES user(id)
) COMMENT '成绩表';

