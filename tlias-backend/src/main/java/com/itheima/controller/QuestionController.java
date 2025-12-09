package com.itheima.controller;

import com.itheima.pojo.PageResult;
import com.itheima.pojo.Question;
import com.itheima.pojo.QuestionQueryParam;
import com.itheima.pojo.Result;
import com.itheima.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 分页查询题目
     */
    @GetMapping
    public Result page(QuestionQueryParam queryParam) {
        log.info("分页查询题目：{}", queryParam);
        PageResult<Question> pageResult = questionService.page(queryParam);
        return Result.success(pageResult);
    }

    /**
     * 查询所有题目
     */
    @GetMapping("/all")
    public Result findAll() {
        log.info("查询所有题目");
        List<Question> questions = questionService.findAll();
        return Result.success(questions);
    }

    /**
     * 新增题目
     */
    @PostMapping
    public Result save(@RequestBody Question question) {
        log.info("新增题目：{}", question);
        questionService.save(question);
        return Result.success();
    }

    /**
     * 根据ID查询题目
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        log.info("根据ID查询题目：{}", id);
        Question question = questionService.getById(id);
        return Result.success(question);
    }

    /**
     * 修改题目
     */
    @PutMapping
    public Result update(@RequestBody Question question) {
        log.info("修改题目：{}", question);
        questionService.update(question);
        return Result.success();
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Integer> ids) {
        log.info("批量删除题目：{}", ids);
        questionService.delete(ids);
        return Result.success();
    }

    /**
     * 根据题库ID查询题目列表
     */
    @GetMapping("/bank/{bankId}")
    public Result findByBankId(@PathVariable Integer bankId) {
        log.info("根据题库ID查询题目：{}", bankId);
        List<Question> questions = questionService.findByBankId(bankId);
        return Result.success(questions);
    }

    /**
     * 随机获取题目
     */
    @GetMapping("/random")
    public Result getRandomQuestions(@RequestParam Integer bankId,
                                   @RequestParam(required = false) Integer type,
                                   @RequestParam Integer count) {
        log.info("随机获取题目：bankId={}, type={}, count={}", bankId, type, count);
        List<Question> questions = questionService.getRandomQuestions(bankId, type, count);
        return Result.success(questions);
    }
}