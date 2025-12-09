package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.ExamMapper;
import com.itheima.mapper.ExamPaperMapper;
import com.itheima.mapper.AnswerRecordMapper;
import com.itheima.mapper.ExamScoreMapper;
import com.itheima.mapper.QuestionMapper;
import com.itheima.pojo.*;
import com.itheima.service.AnswerRecordService;
import com.itheima.service.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamPaperMapper examPaperMapper;
    
    @Autowired
    private AnswerRecordService answerRecordService;
    
    @Autowired
    private ExamScoreMapper examScoreMapper;

    @Override
    public PageResult<Exam> page(ExamQueryParam queryParam) {
        PageHelper.startPage(queryParam.getPage(), queryParam.getPageSize());
        Page<Exam> p = (Page<Exam>) examMapper.list(queryParam);
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public void save(Exam exam) {
        exam.setCreateTime(LocalDateTime.now());
        exam.setUpdateTime(LocalDateTime.now());
        exam.setStatus(0); // 0: 未开始
        examMapper.insert(exam);
    }

    @Override
    public Exam getById(Integer id) {
        return examMapper.getById(id);
    }

    @Override
    public void update(Exam exam) {
        // 获取原考试信息
        Exam originalExam = examMapper.getById(exam.getId());
        if (originalExam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        // 如果是已发布的考试（状态为1），检查是否已经开始
        if (originalExam.getStatus() == 1) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(originalExam.getStartTime())) {
                throw new RuntimeException("考试已开始，无法编辑");
            }
        }
        
        // 进行中或已结束的考试不能编辑
        if (originalExam.getStatus() == 2 || originalExam.getStatus() == 3) {
            throw new RuntimeException("该考试已进行中或已结束，无法编辑");
        }
        
        exam.setUpdateTime(LocalDateTime.now());
        examMapper.update(exam);
    }

    @Override
    public void delete(List<Integer> ids) {
        examMapper.deleteByIds(ids);
    }

    @Override
    public List<Exam> findByTeacherId(Integer teacherId) {
        return examMapper.findByTeacherId(teacherId);
    }

    @Override
    public List<Exam> findAvailableExams(Integer studentId) {
        return examMapper.findAvailableExams(studentId);
    }

    @Override
    @Transactional
    public void generateExamPaper(Integer examId) {
        Exam exam = examMapper.getById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        // 验证试卷中是否已经有足够的题目
        List<ExamPaper> existingPapers = examPaperMapper.findByExamId(examId);
        int totalExpected = (exam.getChoiceCount() != null ? exam.getChoiceCount() : 0) + 
                           (exam.getFillCount() != null ? exam.getFillCount() : 0);
        
        if (existingPapers.size() != totalExpected) {
            throw new RuntimeException("试卷题目数量不符，需要 " + totalExpected + " 道题，实际有 " + existingPapers.size() + " 道");
        }
    }

    @Override
    public void updateExamStatus(Integer examId, Integer status) {
        examMapper.updateStatus(examId, status);
    }
    
    @Override
    public Map<String, Object> getExamPaperForStudent(Integer examId, Integer studentId) {
        log.info("获取学生考试试卷: examId={}, studentId={}", examId, studentId);
        
        // 获取考试信息
        Exam exam = examMapper.getById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        // 获取试卷题目
        List<ExamPaper> papers = examPaperMapper.findByExamId(examId);
        if (papers.isEmpty()) {
            throw new RuntimeException("试卷题目不存在，请先生成试卷");
        }
        
        // 为每个ExamPaper填充Question对象
        for (ExamPaper paper : papers) {
            Question question = questionMapper.getById(paper.getQuestionId());
            paper.setQuestion(question);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("papers", papers);
        result.put("questionCount", papers.size());
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> submitExam(ExamSubmitDTO submitDTO) {
        log.info("学生提交答题: examId={}, studentId={}", submitDTO.getExamId(), submitDTO.getStudentId());
        
        Integer examId = submitDTO.getExamId();
        Integer studentId = submitDTO.getStudentId();
        List<ExamSubmitDTO.AnswerItem> answers = submitDTO.getAnswers();
        
        // 获取考试信息
        Exam exam = examMapper.getById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        // 获取试卷题目
        List<ExamPaper> papers = examPaperMapper.findByExamId(examId);
        if (papers.isEmpty()) {
            throw new RuntimeException("试卷题目不存在");
        }
        
        // 为每个ExamPaper填充Question对象
        for (ExamPaper paper : papers) {
            Question question = questionMapper.getById(paper.getQuestionId());
            paper.setQuestion(question);
        }
        
        // 删除之前的答题记录（如果存在）
        answerRecordService.deleteByExamAndStudent(examId, studentId);
        
        // 批量保存答题记录并自动判题
        List<AnswerRecord> answerRecords = new ArrayList<>();
        int totalScore = 0;
        int correctCount = 0;
        
        Map<Integer, String> answerMap = new HashMap<>();
        for (ExamSubmitDTO.AnswerItem item : answers) {
            answerMap.put(item.getQuestionId(), item.getAnswer());
        }
        
        for (ExamPaper paper : papers) {
            Question question = paper.getQuestion();
            String correctAnswer = question.getAnswer();
            String studentAnswer = answerMap.getOrDefault(question.getId(), "");
            
            // 判题
            int isCorrect = 0;
            int score = 0;
            // 对于选择题（type=1），比较选项序号；对于填空题（type=2），比较答案内容
            if (studentAnswer != null && !studentAnswer.isEmpty()) {
                boolean answerMatch = false;
                
                if (question.getType() == 1) {
                    // 选择题：学生答案是选项序号（1、2、3、4），需要转换为字母（A、B、C、D）后再比较
                    String convertedAnswer = convertAnswerToLetter(studentAnswer);
                    answerMatch = convertedAnswer.equals(correctAnswer);
                } else {
                    // 填空题：比较答案内容（忽略空格和大小写）
                    answerMatch = studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
                }
                
                if (answerMatch) {
                    isCorrect = 1;
                    score = question.getScore();
                    correctCount++;
                    totalScore += question.getScore();
                }
            }
            
            // ... existing code ...
            AnswerRecord answerRecord = new AnswerRecord();
            answerRecord.setExamId(examId);
            answerRecord.setStudentId(studentId);
            answerRecord.setQuestionId(question.getId());
            answerRecord.setStudentAnswer(studentAnswer);
            answerRecord.setIsCorrect(isCorrect);
            answerRecord.setScore(score);
            
            answerRecords.add(answerRecord);
        }
        
        // 批量保存答题记录
        answerRecordService.batchSave(answerRecords);
        
        // 保存成绩
        ExamScore examScore = new ExamScore();
        examScore.setExamId(examId);
        examScore.setStudentId(studentId);
        examScore.setTotalScore(totalScore);
        examScore.setCorrectCount(correctCount);
        examScore.setSubmitTime(LocalDateTime.now());
        examScore.setStatus(2); // 2: 已提交
        examScore.setCreateTime(LocalDateTime.now());
        examScore.setUpdateTime(LocalDateTime.now());
        
        examScoreMapper.insert(examScore);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", totalScore);
        result.put("correctCount", correctCount);
        result.put("totalQuestions", papers.size());
        result.put("passScore", exam.getTotalScore() >= 60 ? exam.getTotalScore() : 60);
        result.put("isPassed", totalScore >= 60);
        
        return result;
    }
    
    /**
     * 将选项序号（1、2、3、4）转换为字母（A、B、C、D）
     */
    private String convertAnswerToLetter(String answerNumber) {
        try {
            int num = Integer.parseInt(answerNumber);
            if (num >= 1 && num <= 4) {
                return String.valueOf((char) ('A' + num - 1));
            }
        } catch (NumberFormatException e) {
            // 如果本身已经是字母，直接返回
            if (answerNumber.length() == 1 && answerNumber.matches("[A-D]")) {
                return answerNumber.toUpperCase();
            }
        }
        return answerNumber;
    }
}