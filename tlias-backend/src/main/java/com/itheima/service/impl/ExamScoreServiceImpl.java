package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.ExamScoreMapper;
import com.itheima.pojo.ExamScore;
import com.itheima.pojo.PageResult;
import com.itheima.service.ExamScoreService;
import com.itheima.service.AnswerRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamScoreServiceImpl implements ExamScoreService {

    @Autowired
    private ExamScoreMapper examScoreMapper;

    @Autowired
    private AnswerRecordService answerRecordService;

    @Override
    public List<ExamScore> findByExamId(Integer examId) {
        return examScoreMapper.findByExamId(examId);
    }

    @Override
    public List<ExamScore> findByStudentId(Integer studentId) {
        return examScoreMapper.findByStudentId(studentId);
    }

    @Override
    public ExamScore findByExamAndStudent(Integer examId, Integer studentId) {
        return examScoreMapper.findByExamAndStudent(examId, studentId);
    }

    @Override
    public void save(ExamScore examScore) {
        examScore.setCreateTime(LocalDateTime.now());
        examScore.setUpdateTime(LocalDateTime.now());
        examScoreMapper.insert(examScore);
    }

    @Override
    public void update(ExamScore examScore) {
        examScore.setUpdateTime(LocalDateTime.now());
        examScoreMapper.update(examScore);
    }

    @Override
    public void calculateAndSaveScore(Integer examId, Integer studentId) {
        // 这个方法在AnswerRecordService中已经实现
        // 这里可以调用AnswerRecordService的submitExam方法
    }

    @Override
    public List<ExamScore> getExamRanking(Integer examId) {
        return examScoreMapper.getExamRanking(examId);
    }

    @Override
    public PageResult<ExamScore> page(Integer examId, Integer studentId, Integer clazzId, String studentName, String examName, Boolean isPass, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<ExamScore> list = examScoreMapper.list(examId, studentId, clazzId, studentName, examName, isPass);
        Page<ExamScore> p = (Page<ExamScore>) list;
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public Map<String, Object> getStatisticsByTeacher(Integer teacherId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取教师所带班级的成绩统计
        List<Map<String, Object>> clazzStats = examScoreMapper.getStatisticsByTeacher(teacherId);
        statistics.put("clazzStats", clazzStats);
        
        // 计算总体统计信息
        if (!clazzStats.isEmpty()) {
            double totalAvgScore = clazzStats.stream()
                    .mapToDouble(stat -> ((Number) stat.get("avgScore")).doubleValue())
                    .average().orElse(0.0);
            int totalStudents = clazzStats.stream()
                    .mapToInt(stat -> ((Number) stat.get("studentCount")).intValue())
                    .sum();
            int totalExams = clazzStats.stream()
                    .mapToInt(stat -> ((Number) stat.get("examCount")).intValue())
                    .sum();
            
            statistics.put("totalAvgScore", Math.round(totalAvgScore * 100.0) / 100.0);
            statistics.put("totalStudents", totalStudents);
            statistics.put("totalExams", totalExams);
            statistics.put("clazzCount", clazzStats.size());
        }
        
        return statistics;
    }

    @Override
    public Map<String, Object> getStatisticsByClazz(Integer clazzId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取班级基本信息
        Map<String, Object> clazzInfo = examScoreMapper.getClazzInfo(clazzId);
        if (clazzInfo != null) {
            statistics.putAll(clazzInfo);
        }
        
        // 获取班级成绩统计
        Map<String, Object> scoreStats = examScoreMapper.getStatisticsByClazz(clazzId);
        if (scoreStats != null) {
            statistics.putAll(scoreStats);
        }
        
        // 获取最近考试统计
        List<Map<String, Object>> recentExams = examScoreMapper.getRecentExamsByClazz(clazzId);
        statistics.put("recentExams", recentExams);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getStatisticsByExam(Integer examId) {
        return examScoreMapper.getStatisticsByExam(examId);
    }

    @Override
    public List<Map<String, Object>> getClazzRanking(Integer clazzId) {
        return examScoreMapper.getClazzRanking(clazzId);
    }

    @Override
    public List<Map<String, Object>> getTeacherClazzComparison(Integer teacherId) {
        return examScoreMapper.getTeacherClazzComparison(teacherId);
    }

    @Override
    public Map<String, Object> getClazzInfo(Integer clazzId) {
        return examScoreMapper.getClazzInfo(clazzId);
    }

    @Override
    public List<Map<String, Object>> getRecentExamsByClazz(Integer clazzId) {
        return examScoreMapper.getRecentExamsByClazz(clazzId);
    }

    @Override
    public List<Map<String, Object>> getExamReportData(String startDate, String endDate, String examName, Integer bankId) {
        return examScoreMapper.getExamReportData(startDate, endDate, examName, bankId);
    }

    @Override
    public List<Map<String, Object>> getExamTrendData(String startDate, String endDate, Integer bankId) {
        return examScoreMapper.getExamTrendData(startDate, endDate, bankId);
    }

    @Override
    public Map<String, Object> getOverallStatistics(String startDate, String endDate, Integer bankId) {
        return examScoreMapper.getOverallStatistics(startDate, endDate, bankId);
    }

    @Override
    public Map<String, Object> getStatistics(Integer examId, String studentName, Boolean isPass) {
        // 获取所有不及格的成绩记录
        List<ExamScore> passScores = examScoreMapper.list(examId, null, null, studentName, null, true);
        // 获取所有不及格的成绩记录
        List<ExamScore> failScores = examScoreMapper.list(examId, null, null, studentName, null, false);
        
        Map<String, Object> statistics = new HashMap<>();
        
        int passCount = passScores.size();
        int failCount = failScores.size();
        int totalStudents = passCount + failCount;
        
        statistics.put("totalStudents", totalStudents);
        statistics.put("passStudents", passCount);
        statistics.put("failStudents", failCount);
        statistics.put("passRate", totalStudents > 0 ? 
            String.format("%.2f", ((double) passCount / totalStudents) * 100) : "0.00");
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getScoreSegments(Integer examId, Integer clazzId) {
        return examScoreMapper.getScoreSegments(examId, clazzId);
    }

    @Override
    public void deleteStudentExamRecord(Integer examId, Integer studentId) {
        // 删除答题记录
        answerRecordService.deleteByExamAndStudent(examId, studentId);
        // 删除成绩记录
        examScoreMapper.deleteByExamAndStudent(examId, studentId);
    }
}