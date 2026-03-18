package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.QuestionMapper;
import com.itheima.pojo.PageResult;
import com.itheima.pojo.Question;
import com.itheima.pojo.QuestionQueryParam;
import com.itheima.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public PageResult<Question> page(QuestionQueryParam queryParam) {
        PageHelper.startPage(queryParam.getPage(), queryParam.getPageSize());
        Page<Question> p = (Page<Question>) questionMapper.list(queryParam);
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public List<Question> findAll() {
        return questionMapper.findAll();
    }

    @Override
    public void save(Question question) {
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        questionMapper.insert(question);
    }

    @Override
    public Question getById(Integer id) {
        return questionMapper.getById(id);
    }

    @Override
    public void update(Question question) {
        question.setUpdateTime(LocalDateTime.now());
        questionMapper.update(question);
    }

    @Override
    public void delete(List<Integer> ids) {
        questionMapper.deleteByIds(ids);
    }

    @Override
    public List<Question> findByBankId(Integer bankId) {
        return questionMapper.findByBankId(bankId);
    }

    @Override
    public List<Question> getRandomQuestions(Integer bankId, Integer type, Integer count) {
        return questionMapper.getRandomQuestions(bankId, type, count);
    }
}