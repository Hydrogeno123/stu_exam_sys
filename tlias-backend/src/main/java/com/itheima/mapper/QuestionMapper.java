package com.itheima.mapper;

import com.itheima.pojo.Question;
import com.itheima.pojo.QuestionQueryParam;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {

    /**
     * 分页查询题目列表
     */
    List<Question> list(QuestionQueryParam queryParam);

    /**
     * 查询所有题目
     */
    List<Question> findAll();

    /**
     * 新增题目
     */
    @Insert("insert into question(bank_id, type, content, options, answer, score, create_time, update_time) " +
            "values(#{bankId}, #{type}, #{content}, #{options}, #{answer}, #{score}, #{createTime}, #{updateTime})")
    void insert(Question question);

    /**
     * 根据ID查询题目
     */
    @Select("select * from question where id = #{id}")
    Question getById(Integer id);

    /**
     * 修改题目
     */
    @Update("update question set bank_id=#{bankId}, type=#{type}, content=#{content}, options=#{options}, " +
            "answer=#{answer}, score=#{score}, update_time=#{updateTime} where id=#{id}")
    void update(Question question);

    /**
     * 批量删除题目
     */
    void deleteByIds(List<Integer> ids);

    /**
     * 根据题库ID查询题目
     */
    @Select("select * from question where bank_id = #{bankId}")
    List<Question> findByBankId(Integer bankId);

    /**
     * 随机获取题目
     */
    @Select("select * from question where bank_id = #{bankId} and type = #{type} order by rand() limit #{count}")
    List<Question> getRandomQuestions(@Param("bankId") Integer bankId, 
                                    @Param("type") Integer type, 
                                    @Param("count") Integer count);
}