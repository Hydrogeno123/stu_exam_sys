package com.itheima.service.impl;

import com.itheima.ai.AiAssistant;
import com.itheima.mapper.ExamScoreMapper;
import com.itheima.mapper.QuestionMapper;
import com.itheima.pojo.ExamScore;
import com.itheima.pojo.Question;
import com.itheima.service.AiAssistantService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AiAssistantServiceImpl implements AiAssistantService {

    @Autowired(required = false)
    private AiAssistant aiAssistant;

    @Autowired
    private ExamScoreMapper examScoreMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired(required = false)
    private ChatLanguageModel chatLanguageModel;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Override
    public String chat(String message, Integer userId, Integer userRole) {
        if (aiAssistant == null || !aiEnabled) {
            return "AI服务暂未启用，请联系管理员配置API Key。";
        }
        try {
            String response;
            if (userRole != null) {
                switch (userRole) {
                    case 0:
                        response = aiAssistant.chatAsAdmin(message);
                        log.info("AI chatAsAdmin 输出：{}", response);
                        break;
                    case 1:
                        response = aiAssistant.chatAsTeacher(message);
                        log.info("AI chatAsTeacher 输出：{}", response);
                        break;
                    case 2:
                        response = aiAssistant.chatAsStudent(message);
                        log.info("AI chatAsStudent 输出：{}", response);
                        break;
                    default:
                        response = aiAssistant.chatAsStudent(message);
                        log.info("AI chatAsStudent (default) 输出：{}", response);
                        break;
                }
            } else {
                response = aiAssistant.chatAsStudent(message);
                log.info("AI chatAsStudent (no role) 输出：{}", response);
            }
            return response;
        } catch (Exception e) {
            log.error("AI chat 调用失败", e);
            if (chatLanguageModel != null) {
                String fallbackResponse = chatLanguageModel.generate(message);
                log.info("AI fallback 输出：{}", fallbackResponse);
                return fallbackResponse;
            }
            throw e;
        }
    }

    @Override
    public String analyzeExamData(Integer examId) {
        List<ExamScore> scores = examScoreMapper.findByExamId(examId);

        if (scores == null || scores.isEmpty()) {
            return "该考试暂无成绩数据。";
        }

        if (aiAssistant == null || !aiEnabled) {
            return "AI服务暂未启用，请联系管理员配置API Key。";
        }

        double avgScore = scores.stream()
                .mapToInt(ExamScore::getTotalScore)
                .average()
                .orElse(0);

        int maxScore = scores.stream()
                .mapToInt(ExamScore::getTotalScore)
                .max()
                .orElse(0);

        int minScore = scores.stream()
                .mapToInt(ExamScore::getTotalScore)
                .min()
                .orElse(0);

        long passCount = scores.stream()
                .filter(s -> s.getTotalScore() >= 60)
                .count();

        double passRate = (double) passCount / scores.size() * 100;

        String examData = String.format("""
                考试数据：
                - 参考人数：%d 人
                - 平均分：%.1f 分
                - 最高分：%d 分
                - 最低分：%d 分
                - 及格率：%.1f%%
                
                请根据以上数据给出专业的分析报告和教学建议。
                """,
                scores.size(), avgScore, maxScore, minScore, passRate);

        try {
            return aiAssistant.analyzeExamData(examData);
        } catch (Exception e) {
            log.error("AI analyzeExamData 调用失败", e);
            return chatLanguageModel.generate(examData);
        }
    }

    @Override
    public String analyzeStudentData(Integer studentId) {
        List<ExamScore> scores = examScoreMapper.findByStudentId(studentId);

        if (scores == null || scores.isEmpty()) {
            return "该学生暂无考试记录。";
        }

        if (aiAssistant == null || !aiEnabled) {
            return "AI服务暂未启用，请联系管理员配置API Key。";
        }

        double avgScore = scores.stream()
                .mapToInt(ExamScore::getTotalScore)
                .average()
                .orElse(0);

        int totalExams = scores.size();
        long passCount = scores.stream()
                .filter(s -> s.getTotalScore() >= 60)
                .count();

        String studentData = String.format("""
                学生成绩数据：
                - 参加考试次数：%d 次
                - 平均分：%.1f 分
                - 及格次数：%d 次
                - 及格率：%.1f%%
                
                请根据以上数据给出个性化的学习建议。
                """,
                totalExams, avgScore, passCount, (double) passCount / totalExams * 100);

        try {
            return aiAssistant.analyzeStudentData(studentData);
        } catch (Exception e) {
            log.error("AI analyzeStudentData 调用失败", e);
            return chatLanguageModel.generate(studentData);
        }
    }

    @Override
    public String answerQuestion(Integer questionId) {
        if (aiAssistant == null || !aiEnabled) {
            return "AI服务暂未启用，请联系管理员配置API Key。";
        }

        Question question = questionMapper.getById(questionId);

        if (question == null) {
            return "题目不存在。";
        }

        String type = question.getType() == 1 ? "选择题" : "填空题";
        String questionContent = String.format("""
                题目类型：%s
                
                题目内容：
                %s
                
                请给出这道题的解题思路和知识点讲解，不要直接给出答案，而是引导学生思考。
                """, type, question.getContent());

        try {
            return aiAssistant.answerQuestion(questionContent);
        } catch (Exception e) {
            log.error("AI answerQuestion 调用失败", e);
            return chatLanguageModel.generate(questionContent);
        }
    }
}
