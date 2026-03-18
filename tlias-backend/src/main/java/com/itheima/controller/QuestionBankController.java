package com.itheima.controller;

import com.itheima.pojo.PageResult;
import com.itheima.pojo.Result;
import com.itheima.pojo.QuestionBank;
import com.itheima.service.QuestionBankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/questionBanks")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    /**
     * 分页查询题库
     */
    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer pageSize,
                      String bankName) {
        log.info("分页查询题库：page={}, pageSize={}, bankName={}", page, pageSize, bankName);
        PageResult<QuestionBank> pageResult = questionBankService.page(bankName, page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 新增题库
     */
    @PostMapping
    public Result save(@RequestBody QuestionBank questionBank) {
        log.info("新增题库：{}", questionBank);
        questionBankService.save(questionBank);
        return Result.success();
    }

    /**
     * 根据ID查询题库
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        log.info("根据ID查询题库：{}", id);
        QuestionBank questionBank = questionBankService.getById(id);
        return Result.success(questionBank);
    }

    /**
     * 修改题库
     */
    @PutMapping
    public Result update(@RequestBody QuestionBank questionBank) {
        log.info("修改题库：{}", questionBank);
        questionBankService.update(questionBank);
        return Result.success();
    }

    /**
     * 删除题库
     */
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Integer> ids) {
        log.info("批量删除题库：{}", ids);
        questionBankService.delete(ids);
        return Result.success();
    }

    /**
     * 查询所有题库
     */
    @GetMapping("/all")
    public Result findAll() {
        log.info("查询所有题库");
        List<QuestionBank> questionBanks = questionBankService.findAll();
        return Result.success(questionBanks);
    }
}