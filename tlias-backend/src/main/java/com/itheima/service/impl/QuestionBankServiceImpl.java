package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.QuestionBankMapper;
import com.itheima.pojo.PageResult;
import com.itheima.pojo.QuestionBank;
import com.itheima.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionBankServiceImpl implements QuestionBankService {

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Override
    public PageResult<QuestionBank> page(String bankName, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Page<QuestionBank> p = (Page<QuestionBank>) questionBankMapper.list(bankName);
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public void save(QuestionBank questionBank) {
        questionBank.setCreateTime(LocalDateTime.now());
        questionBank.setUpdateTime(LocalDateTime.now());
        questionBankMapper.insert(questionBank);
    }

    @Override
    public QuestionBank getById(Integer id) {
        return questionBankMapper.getById(id);
    }

    @Override
    public void update(QuestionBank questionBank) {
        questionBank.setUpdateTime(LocalDateTime.now());
        questionBankMapper.update(questionBank);
    }

    @Override
    public void delete(List<Integer> ids) {
        questionBankMapper.deleteByIds(ids);
    }

    @Override
    public List<QuestionBank> findAll() {
        return questionBankMapper.findAll();
    }
}