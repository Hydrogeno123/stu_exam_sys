package com.itheima.controller;

import com.itheima.pojo.*;
import com.itheima.service.ExamService;
import com.itheima.service.AnswerRecordService;
import com.itheima.mapper.ExamPaperMapper;
import com.itheima.mapper.QuestionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/exams")
public class ExamController {

    @Autowired
    private ExamService examService;
    
    @Autowired
    private AnswerRecordService answerRecordService;
    
    @Autowired
    private ExamPaperMapper examPaperMapper;
    
    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 分页查询考试列表
     */
    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer pageSize,
                      String examName, Integer status, String startTime, String endTime) {
        log.info("分页查询考试列表: page={}, pageSize={}, examName={}, status={}, startTime={}, endTime={}",
                page, pageSize, examName, status, startTime, endTime);
        
        ExamQueryParam queryParam = new ExamQueryParam();
        queryParam.setPage(page);
        queryParam.setPageSize(pageSize);
        queryParam.setExamName(examName);
        queryParam.setStatus(status);
        
        // 转换开始时间
        if (startTime != null && !startTime.isEmpty()) {
            // 如果仅有日期没有时间（長度为10），则追加空时间
            if (startTime.length() == 10) {
                startTime = startTime + " 00:00:00";
            }
            queryParam.setStartTime(LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 转换结束时间
        if (endTime != null && !endTime.isEmpty()) {
            // 如果仅有日期没有时间（長度为10），则追加空时间
            if (endTime.length() == 10) {
                endTime = endTime + " 23:59:59";
            }
            queryParam.setEndTime(LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        PageResult<Exam> pageResult = examService.page(queryParam);
        return Result.success(pageResult);
    }

    /**
     * 新增考试
     */
    @PostMapping
    public Result save(@RequestBody Exam exam) {
        log.info("新增考试: {}", exam);
        examService.save(exam);
        return Result.success();
    }

    /**
     * 根据ID查询考试
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        log.info("根据ID查询考试: {}", id);
        Exam exam = examService.getById(id);
        return Result.success(exam);
    }

    /**
     * 修改考试
     */
    @PutMapping
    public Result update(@RequestBody Exam exam) {
        log.info("修改考试: {}", exam);
        examService.update(exam);
        return Result.success();
    }

    /**
     * 删除考试
     */
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Integer> ids) {
        log.info("批量删除考试: {}", ids);
        examService.delete(ids);
        return Result.success();
    }

    /**
     * 根据教师ID查询考试列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result findByTeacherId(@PathVariable Integer teacherId) {
        log.info("根据教师ID查询考试列表: teacherId={}", teacherId);
        List<Exam> exams = examService.findByTeacherId(teacherId);
        return Result.success(exams);
    }

    /**
     * 根据学生ID查询可参加的考试
     */
    @GetMapping("/student/{studentId}")
    public Result findAvailableExams(@PathVariable Integer studentId) {
        log.info("根据学生ID查询可参加的考试: studentId={}", studentId);
        List<Exam> exams = examService.findAvailableExams(studentId);
        return Result.success(exams);
    }

    /**
     * 生成试卷
     */
    @PostMapping("/generate/{examId}")
    public Result generateExamPaper(@PathVariable Integer examId) {
        log.info("生成试卷: examId={}", examId);
        examService.generateExamPaper(examId);
        return Result.success();
    }

    /**
     * 更新考试状态
     */
    @PutMapping("/status/{examId}")
    public Result updateExamStatus(@PathVariable Integer examId, @RequestParam Integer status) {
        log.info("更新考试状态: examId={}, status={}", examId, status);
        
        // 如果是发布（状态改为1），先验证试卷
        if (status == 1) {
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }
            
            // 验证试卷中的题目数量是否符合
            List<ExamPaper> papers = examPaperMapper.findByExamId(examId);
            int totalExpected = (exam.getChoiceCount() != null ? exam.getChoiceCount() : 0) + 
                               (exam.getFillCount() != null ? exam.getFillCount() : 0);
            
            if (papers.size() != totalExpected) {
                return Result.error("试卷题目数量不符，需要 " + totalExpected + " 道题，实际有 " + papers.size() + " 道");
            }
        }
        
        examService.updateExamStatus(examId, status);
        return Result.success();
    }

    /**
     * 查询所有考试
     */
    @GetMapping("/all")
    public Result findAll() {
        log.info("查询所有考试");
        // 创建一个查询参数对象，设置分页为1，每页大小为1000，获取所有考试
        ExamQueryParam queryParam = new ExamQueryParam();
        queryParam.setPage(1);
        queryParam.setPageSize(1000);
        PageResult<Exam> pageResult = examService.page(queryParam);
        return Result.success(pageResult.getRows());
    }
    
    /**
     * 学生开始考试，获取试卷
     */
    @PostMapping("/start/{examId}/student/{studentId}")
    public Result startExam(@PathVariable Integer examId, @PathVariable Integer studentId) {
        log.info("学生开始考试: examId={}, studentId={}", examId, studentId);
        Map<String, Object> examData = examService.getExamPaperForStudent(examId, studentId);
        return Result.success(examData);
    }
    
    /**
     * 学生提交答题
     */
    @PostMapping("/submit")
    public Result submitExam(@RequestBody ExamSubmitDTO submitDTO) {
        log.info("学生提交答题: examId={}, studentId={}, answerCount={}", 
                submitDTO.getExamId(), submitDTO.getStudentId(), submitDTO.getAnswers().size());
        Map<String, Object> result = examService.submitExam(submitDTO);
        return Result.success(result);
    }
    
    /**
     * 获取学生的答题详情
     */
    @GetMapping("/answer/{examId}/student/{studentId}")
    public Result getStudentAnswers(@PathVariable Integer examId, @PathVariable Integer studentId) {
        log.info("获取学生答题详情: examId={}, studentId={}", examId, studentId);
        List<AnswerRecord> answers = answerRecordService.findByExamAndStudent(examId, studentId);
        return Result.success(answers);
    }
    
    /**
     * 获取学生答题记录详情，用于前端答题成绩查看
     */
    @GetMapping("/score-details/{examId}/student/{studentId}")
    public Result getScoreDetails(@PathVariable Integer examId, @PathVariable Integer studentId) {
        log.info("获取学生答题记录详情: examId={}, studentId={}", examId, studentId);
        List<AnswerRecord> records = answerRecordService.findByExamAndStudent(examId, studentId);
        
        // 将答题记录转换为前端需要的格式
        List<Map<String, Object>> details = new ArrayList<>();
        for (AnswerRecord record : records) {
            Question question = record.getQuestion();
            if (question != null) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("questionId", record.getQuestionId());
                detail.put("questionContent", question.getContent());
                detail.put("studentAnswer", record.getStudentAnswer());
                detail.put("correctAnswer", question.getAnswer());
                detail.put("isCorrect", record.getIsCorrect() == 1);
                detail.put("actualScore", record.getScore());
                detail.put("score", question.getScore());
                detail.put("questionType", question.getType());  // 添加题型字段
                details.add(detail);
            }
        }
        
        return Result.success(details);
    }
    
    /**
     * 保存考试的试卷题目（编辑试卷后保存）
     */
    @PostMapping("/{examId}/exam-papers")
    public Result saveExamPapers(@PathVariable Integer examId, @RequestBody List<ExamPaper> papers) {
        log.info("保存考试试卷: examId={}, paperCount={}", examId, papers.size());
        
        try {
            // 清除原有的试卷
            examPaperMapper.deleteByExamId(examId);
            
            // 保存新的试卷
            int order = 1;
            for (ExamPaper paper : papers) {
                paper.setExamId(examId);
                paper.setQuestionOrder(order++);
                paper.setCreateTime(LocalDateTime.now());
                examPaperMapper.insert(paper);
            }
            
            return Result.success();
        } catch (Exception e) {
            log.error("保存试卷失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取考试的试卷题目（用于编辑考试题目）
     */
    @GetMapping("/{examId}/exam-papers")
    public Result getExamPapers(@PathVariable Integer examId) {
        log.info("获取考试的试卷题目: examId={}", examId);
        List<ExamPaper> papers = examPaperMapper.findByExamId(examId);
        
        // 为每个ExamPaper填充Question对象
        for (ExamPaper paper : papers) {
            Question question = questionMapper.getById(paper.getQuestionId());
            paper.setQuestion(question);
        }
        
        return Result.success(papers);
    }
}