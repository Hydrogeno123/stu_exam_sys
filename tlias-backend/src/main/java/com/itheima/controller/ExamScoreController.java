package com.itheima.controller;

import com.itheima.pojo.ExamScore;
import com.itheima.pojo.PageResult;
import com.itheima.pojo.Result;
import com.itheima.service.ExamScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/examScores")
public class ExamScoreController {

    @Autowired
    private ExamScoreService examScoreService;

    /**
     * 根据考试ID查询成绩列表
     */
    @GetMapping("/exam/{examId}")
    public Result findByExamId(@PathVariable Integer examId) {
        log.info("根据考试ID查询成绩：{}", examId);
        List<ExamScore> scores = examScoreService.findByExamId(examId);
        return Result.success(scores);
    }

    /**
     * 根据学生ID查询成绩列表
     */
    @GetMapping("/student/{studentId}")
    public Result findByStudentId(@PathVariable Integer studentId) {
        log.info("根据学生ID查询成绩：{}", studentId);
        List<ExamScore> scores = examScoreService.findByStudentId(studentId);
        return Result.success(scores);
    }

    /**
     * 根据考试ID和学生ID查询成绩
     */
    @GetMapping("/exam/{examId}/student/{studentId}")
    public Result findByExamAndStudent(@PathVariable Integer examId, @PathVariable Integer studentId) {
        log.info("查询成绩：examId={}, studentId={}", examId, studentId);
        ExamScore score = examScoreService.findByExamAndStudent(examId, studentId);
        return Result.success(score);
    }

    /**
     * 保存成绩
     */
    @PostMapping
    public Result save(@RequestBody ExamScore examScore) {
        log.info("保存成绩：{}", examScore);
        examScoreService.save(examScore);
        return Result.success();
    }

    /**
     * 修改成绩
     */
    @PutMapping
    public Result update(@RequestBody ExamScore examScore) {
        log.info("修改成绩：{}", examScore);
        examScoreService.update(examScore);
        return Result.success();
    }

    /**
     * 获取考试排名
     */
    @GetMapping("/ranking/{examId}")
    public Result getExamRanking(@PathVariable Integer examId) {
        log.info("获取考试排名: {}", examId);
        List<ExamScore> ranking = examScoreService.getExamRanking(examId);
        return Result.success(ranking);
    }

    /**
     * 分页查询成绩列表
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       Integer examId, Integer studentId, Integer clazzId, String studentName, String examName, Boolean isPass) {
        log.info("分页查询成绩列表: page={}, pageSize={}, examId={}, studentId={}, clazzId={}, studentName={}, examName={}, isPass={}", 
                page, pageSize, examId, studentId, clazzId, studentName, examName, isPass);
        PageResult pageResult = examScoreService.page(examId, studentId, clazzId, studentName, examName, isPass, page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 根据教师ID查询班级成绩统计
     */
    @GetMapping("/statistics/teacher/{teacherId}")
    public Result getStatisticsByTeacher(@PathVariable Integer teacherId) {
        log.info("根据教师ID查询班级成绩统计: {}", teacherId);
        Map<String, Object> statistics = examScoreService.getStatisticsByTeacher(teacherId);
        return Result.success(statistics);
    }

    /**
     * 根据班级ID查询成绩统计
     */
    @GetMapping("/statistics/clazz/{clazzId}")
    public Result getStatisticsByClazz(@PathVariable Integer clazzId) {
        log.info("根据班级ID查询成绩统计: {}", clazzId);
        Map<String, Object> statistics = examScoreService.getStatisticsByClazz(clazzId);
        return Result.success(statistics);
    }

    /**
     * 根据考试ID查询成绩统计
     */
    @GetMapping("/statistics/exam/{examId}")
    public Result getStatisticsByExam(@PathVariable Integer examId) {
        log.info("根据考试ID查询成绩统计: {}", examId);
        Map<String, Object> statistics = examScoreService.getStatisticsByExam(examId);
        return Result.success(statistics);
    }

    /**
     * 查询班级成绩排名
     */
    @GetMapping("/ranking/clazz/{clazzId}")
    public Result getClazzRanking(@PathVariable Integer clazzId) {
        log.info("查询班级成绩排名: {}", clazzId);
        List<Map<String, Object>> ranking = examScoreService.getClazzRanking(clazzId);
        return Result.success(ranking);
    }

    /**
     * 查询教师所带班级成绩对比
     */
    @GetMapping("/comparison/teacher/{teacherId}")
    public Result getTeacherClazzComparison(@PathVariable Integer teacherId) {
        log.info("查询教师所带班级成绩对比: {}", teacherId);
        List<Map<String, Object>> comparison = examScoreService.getTeacherClazzComparison(teacherId);
        return Result.success(comparison);
    }

    /**
     * 获取班级基本信息
     */
    @GetMapping("/clazz/info/{clazzId}")
    public Result getClazzInfo(@PathVariable Integer clazzId) {
        log.info("获取班级基本信息: {}", clazzId);
        Map<String, Object> clazzInfo = examScoreService.getClazzInfo(clazzId);
        return Result.success(clazzInfo);
    }

    /**
     * 获取班级最近考试统计
     */
    @GetMapping("/clazz/recent-exams/{clazzId}")
    public Result getRecentExamsByClazz(@PathVariable Integer clazzId) {
        log.info("获取班级最近考试统计: {}", clazzId);
        List<Map<String, Object>> recentExams = examScoreService.getRecentExamsByClazz(clazzId);
        return Result.success(recentExams);
    }

    /**
     * 获取考试统计报告数据
     */
    @GetMapping("/report/exam-report")
    public Result getExamReportData(@RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) String examName,
                                   @RequestParam(required = false) Integer bankId) {
        log.info("获取考试统计报告数据: startDate={}, endDate={}, examName={}, bankId={}", startDate, endDate, examName, bankId);
        List<Map<String, Object>> examReportData = examScoreService.getExamReportData(startDate, endDate, examName, bankId);
        return Result.success(examReportData);
    }

    /**
     * 获取考试趋势统计数据
     */
    @GetMapping("/report/exam-trend")
    public Result getExamTrendData(@RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate,
                                  @RequestParam(required = false) Integer bankId) {
        log.info("获取考试趋势统计数据: startDate={}, endDate={}, bankId={}", startDate, endDate, bankId);
        List<Map<String, Object>> examTrendData = examScoreService.getExamTrendData(startDate, endDate, bankId);
        return Result.success(examTrendData);
    }

    /**
     * 获取总体统计数据
     */
    @GetMapping("/report/overall-statistics")
    public Result getOverallStatistics(@RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      @RequestParam(required = false) Integer bankId) {
        log.info("获取总体统计数据: startDate={}, endDate={}, bankId={}", startDate, endDate, bankId);
        Map<String, Object> overallStatistics = examScoreService.getOverallStatistics(startDate, endDate, bankId);
        return Result.success(overallStatistics);
    }

    /**
     * 获取成绩统计信息（不分页，基于搜索条件）
     */
    @GetMapping("/statistics")
    public Result getStatistics(@RequestParam(required = false) Integer examId,
                               @RequestParam(required = false) String studentName,
                               @RequestParam(required = false) Boolean isPass) {
        log.info("获取成绩统计信息: examId={}, studentName={}, isPass={}", examId, studentName, isPass);
        Map<String, Object> statistics = examScoreService.getStatistics(examId, studentName, isPass);
        return Result.success(statistics);
    }

    /**
     * 获取成绩分数段统计
     */
    @GetMapping("/report/score-segments")
    public Result getScoreSegments(@RequestParam(required = false) Integer examId,
                                  @RequestParam(required = false) Integer clazzId) {
        log.info("获取成绩分数段统计: examId={}, clazzId={}", examId, clazzId);
        List<Map<String, Object>> scoreSegments = examScoreService.getScoreSegments(examId, clazzId);
        return Result.success(scoreSegments);
    }

    /**
     * 学生查看班级成绩数据
     */
    @GetMapping("/classExam")
    public Result getClassExamData(@RequestParam Integer examId, @RequestParam Integer clazzId) {
        log.info("学生查看班级成绩数据: examId={}, clazzId={}", examId, clazzId);
        
        // 获取班级所有学生成绩
        List<ExamScore> classScores = examScoreService.findByExamId(examId);
        
        // 筛选出该班级的成绩
        List<ExamScore> clazzScores = classScores.stream()
            .filter(score -> score.getStudentId() != null)
            .collect(Collectors.toList());
        
        // 计算班级统计信息
        Map<String, Object> classStats = new HashMap<>();
        classStats.put("totalStudents", clazzScores.size());
        
        if (!clazzScores.isEmpty()) {
            double avgScore = clazzScores.stream().mapToDouble(ExamScore::getTotalScore).average().orElse(0);
            long passCount = clazzScores.stream().filter(score -> score.getTotalScore() != null && score.getTotalScore() >= 60).count();
            double passRate = (double) passCount / clazzScores.size() * 100;
            
            classStats.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
            classStats.put("passRate", Math.round(passRate * 10.0) / 10.0);
        } else {
            classStats.put("avgScore", 0);
            classStats.put("passRate", 0);
        }
        
        // 假设获取当前登录学生信息（实际应该从token中获取）
        // 这里先返回mock数据，实际实现时需要从前端传studentId参数
        Map<String, Object> myScore = new HashMap<>();
        myScore.put("score", 85);
        myScore.put("rank", 1); // 排名
        myScore.put("studentId", 1); // 假设的学生ID
        
        // 返回班级成绩数据和统计信息
        Map<String, Object> result = new HashMap<>();
        result.put("students", clazzScores);
        result.put("classStats", classStats);
        result.put("myScore", myScore);
        
        return Result.success(result);
    }
    
    /**
     * 删除学生的考试记录（答题记录和成绩记录）
     */
    @DeleteMapping("/student/{studentId}/exam/{examId}")
    public Result deleteStudentExamRecord(@PathVariable Integer examId, @PathVariable Integer studentId) {
        log.info("删除学生考试记录: examId={}, studentId={}", examId, studentId);
        try {
            // 删除答题记录
            examScoreService.deleteStudentExamRecord(examId, studentId);
            return Result.success("考试记录删除成功");
        } catch (Exception e) {
            log.error("删除考试记录失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}