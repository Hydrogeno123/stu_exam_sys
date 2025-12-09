package com.itheima.mapper;

import com.itheima.pojo.QuestionBank;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionBankMapper {

    /**
     * 分页查询题库列表
     */
    List<QuestionBank> list(@Param("bankName") String bankName);

    /**
     * 新增题库
     */
    @Insert("insert into question_bank(bank_name, description, create_time, update_time) " +
            "values(#{bankName}, #{description}, #{createTime}, #{updateTime})")
    void insert(QuestionBank questionBank);

    /**
     * 根据ID查询题库
     */
    @Select("select * from question_bank where id = #{id}")
    QuestionBank getById(Integer id);

    /**
     * 修改题库
     */
    @Update("update question_bank set bank_name=#{bankName}, description=#{description}, " +
            "update_time=#{updateTime} where id=#{id}")
    void update(QuestionBank questionBank);

    /**
     * 批量删除题库
     */
    void deleteByIds(List<Integer> ids);

    /**
     * 查询所有题库
     */
    @Select("select * from question_bank")
    List<QuestionBank> findAll();
}