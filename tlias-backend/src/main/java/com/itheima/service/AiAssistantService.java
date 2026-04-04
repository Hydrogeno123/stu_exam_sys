package com.itheima.service;

public interface AiAssistantService {
    
    String chat(String message, Integer userId, Integer userRole);
    
    String analyzeExamData(Integer examId);
    
    String analyzeStudentData(Integer studentId);
    
    String answerQuestion(Integer questionId);
}
