package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.service.AiAssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/ai")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url:}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name:}")
    private String modelName;

    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, Object> params) {
        String message = (String) params.get("message");
        Integer userId = params.get("userId") != null ? (Integer) params.get("userId") : null;
        Integer userRole = params.get("userRole") != null ? (Integer) params.get("userRole") : null;

        log.info("AI助手收到消息: {}, userId: {}, userRole: {}", message, userId, userRole);

        try {
            String response = aiAssistantService.chat(message, userId, userRole);
            return Result.success(response);
        } catch (Exception e) {
            log.error("AI助手处理失败", e);
            return Result.error("AI服务暂时不可用，请稍后再试");
        }
    }

    @GetMapping("/config")
    public Result getAiConfig() {
        Map<String, Object> config = Map.of(
                "enabled", aiEnabled,
                "hasApiKey", apiKey != null && !apiKey.isEmpty(),
                "baseUrl", baseUrl,
                "modelName", modelName
        );
        return Result.success(config);
    }

    @PostMapping("/config")
    public Result updateAiConfig(@RequestBody Map<String, String> config) {
        String newApiKey = config.get("apiKey");
        String newBaseUrl = config.get("baseUrl");
        String newModelName = config.get("modelName");
        Boolean enabled = config.get("enabled") != null ? Boolean.parseBoolean(config.get("enabled")) : null;

        log.info("更新AI配置: enabled={}, hasNewKey={}, baseUrl={}, model={}",
                enabled, newApiKey != null && !newApiKey.isEmpty(), newBaseUrl, newModelName);

        return Result.success("配置已更新，重启服务后生效");
    }

    @GetMapping("/analyze/exam/{examId}")
    public Result analyzeExamData(@PathVariable Integer examId) {
        log.info("分析考试数据: examId={}", examId);
        try {
            String analysis = aiAssistantService.analyzeExamData(examId);
            return Result.success(analysis);
        } catch (Exception e) {
            log.error("考试数据分析失败", e);
            return Result.error("分析失败，请稍后再试");
        }
    }

    @GetMapping("/analyze/student/{studentId}")
    public Result analyzeStudentData(@PathVariable Integer studentId) {
        log.info("分析学生数据: studentId={}", studentId);
        try {
            String analysis = aiAssistantService.analyzeStudentData(studentId);
            return Result.success(analysis);
        } catch (Exception e) {
            log.error("学生数据分析失败", e);
            return Result.error("分析失败，请稍后再试");
        }
    }

    @GetMapping("/answer/{questionId}")
    public Result answerQuestion(@PathVariable Integer questionId) {
        log.info("AI解答题目: questionId={}", questionId);
        try {
            String answer = aiAssistantService.answerQuestion(questionId);
            return Result.success(answer);
        } catch (Exception e) {
            log.error("题目解答失败", e);
            return Result.error("解答失败，请稍后再试");
        }
    }
}
