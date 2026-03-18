package com.itheima.mapper;

import com.itheima.pojo.ExamPaper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamPaperMapper {

    /**
     * 新增试卷题目
     */
    @Insert("insert into exam_paper(exam_id, question_id, question_order, create_time) " +
            "values(#{examId}, #{questionId}, #{questionOrder}, #{createTime})")
    void insert(ExamPaper examPaper);

    /**
     * 根据考试ID删除试卷题目
     */
    @Delete("delete from exam_paper where exam_id = #{examId}")
    void deleteByExamId(Integer examId);
    
    /**
     * 根据ID列表批量删除试卷题目
     */
    @Delete("<script>" +
            "delete from exam_paper where id in " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'> " +
            "#{id} " +
            "</foreach>" +
            "</script>")
    void deleteByIds(List<Integer> ids);

    /**
     * 根据考试ID查询试卷题目（分页）
     */
    @Select("select ep.id, ep.exam_id, ep.question_id, ep.question_order, ep.create_time, q.type, q.content, q.options, q.answer, q.score " +
            "from exam_paper ep left join question q on ep.question_id = q.id " +
            "where ep.exam_id = #{examId} order by ep.question_order")
    List<ExamPaper> pageList(Integer examId);

    /**
     * 根据考试ID查询试卷题目
     */
    @Select("select ep.*, q.type, q.content, q.options, q.answer, q.score " +
            "from exam_paper ep left join question q on ep.question_id = q.id " +
            "where ep.exam_id = #{examId} order by ep.question_order")
    List<ExamPaper> findByExamId(Integer examId);
    
    /**
     * 根据ID查询试卷题目
     */
    @Select("select * from exam_paper where id = #{id}")
    ExamPaper getById(Integer id);
    
    /**
     * 修改试卷题目
     */
    @Update("update exam_paper set exam_id = #{examId}, question_id = #{questionId}, question_order = #{questionOrder}, create_time = #{createTime} where id = #{id}")
    void update(ExamPaper examPaper);

    /**
     * 根据考试ID和题目ID查询试卷题目
     */
    @Select("select * from exam_paper where exam_id = #{examId} and question_id = #{questionId}")
    ExamPaper findByExamAndQuestion(@Param("examId") Integer examId, @Param("questionId") Integer questionId);
}