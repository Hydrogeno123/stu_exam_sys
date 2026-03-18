package com.itheima.service;

import com.itheima.pojo.ExamScore;

import java.util.List;

public interface ExamScoreService {
    /**
     * 根据考试ID查询成绩列表
     */
    List<ExamScore> findByExamId(Integer examId);

    /**
     * 根据学生ID查询成绩列表
     */
    List<ExamScore> findByStudentId(Integer studentId);

    /**
     * 根据考试ID和学生ID查询成绩
     */
    ExamScore findByExamAndStudent(Integer examId, Integer studentId);

    /**
     * 保存成绩
     */
    void save(ExamScore examScore);

    /**
     * 更新成绩
     */
    void update(ExamScore examScore);

    /**
     * 计算并保存成绩
     */
    void calculateAndSaveScore(Integer examId, Integer studentId);

    /**
     * 获取考试排名
     */
    List<ExamScore> getExamRanking(Integer examId);

    /**
     * 分页查询成绩列表
     */
    com.itheima.pojo.PageResult<ExamScore> page(Integer examId, Integer studentId, Integer clazzId, String studentName, String examName, Boolean isPass, Integer page, Integer pageSize);

    /**
     * 根据教师ID查询所有班级的成绩统计
     */
    java.util.Map<String, Object> getStatisticsByTeacher(Integer teacherId);

    /**
     * 根据班级ID查询成绩统计
     */
    java.util.Map<String, Object> getStatisticsByClazz(Integer clazzId);

    /**
     * 根据考试ID查询成绩统计
     */
    java.util.Map<String, Object> getStatisticsByExam(Integer examId);

    /**
     * 查询班级成绩排名
     */
    java.util.List<java.util.Map<String, Object>> getClazzRanking(Integer clazzId);

    /**
     * 查询教师所带班级的成绩对比
     */
    java.util.List<java.util.Map<String, Object>> getTeacherClazzComparison(Integer teacherId);

    /**
     * 获取班级基本信息
     */
    java.util.Map<String, Object> getClazzInfo(Integer clazzId);

    /**
     * 获取班级最近考试统计
     */
    java.util.List<java.util.Map<String, Object>> getRecentExamsByClazz(Integer clazzId);

    /**
     * 获取考试统计报告数据
     */
    java.util.List<java.util.Map<String, Object>> getExamReportData(String startDate, String endDate, String examName, Integer bankId);

    /**
     * 获取考试趋势统计数据
     */
    java.util.List<java.util.Map<String, Object>> getExamTrendData(String startDate, String endDate, Integer bankId);

    /**
     * 获取总体统计数据
     */
    java.util.Map<String, Object> getOverallStatistics(String startDate, String endDate, Integer bankId);

    /**
     * 获取成绩统计信息（不分页，基于搜索条件）
     */
    java.util.Map<String, Object> getStatistics(Integer examId, String studentName, Boolean isPass);

    /**
     * 获取成绩分数段统计
     */
    java.util.List<java.util.Map<String, Object>> getScoreSegments(Integer examId, Integer clazzId);

    /**
     * 删除学生的考试记录（答题记录和成绩记录）
     */
    void deleteStudentExamRecord(Integer examId, Integer studentId);
}