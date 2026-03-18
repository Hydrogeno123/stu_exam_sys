package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.ExamPaperMapper;
import com.itheima.pojo.ExamPaper;
import com.itheima.pojo.PageResult;
import com.itheima.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamPaperServiceImpl implements ExamPaperService {

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Override
    public PageResult<ExamPaper> page(Integer examId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Page<ExamPaper> p = (Page<ExamPaper>) examPaperMapper.pageList(examId);
        return new PageResult<ExamPaper>(p.getTotal(), p.getResult());
    }

    @Override
    public void save(ExamPaper examPaper) {
        if (examPaper.getCreateTime() == null) {
            examPaper.setCreateTime(LocalDateTime.now());
        }
        examPaperMapper.insert(examPaper);
    }

    @Override
    public ExamPaper getById(Integer id) {
        return examPaperMapper.getById(id);
    }

    @Override
    public void update(ExamPaper examPaper) {
        examPaperMapper.update(examPaper);
    }

    @Override
    public void delete(List<Integer> ids) {
        examPaperMapper.deleteByIds(ids);
    }

    @Override
    public List<ExamPaper> findByExamId(Integer examId) {
        return examPaperMapper.findByExamId(examId);
    }
}
