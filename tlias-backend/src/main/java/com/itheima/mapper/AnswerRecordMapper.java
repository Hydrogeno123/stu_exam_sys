package com.itheima.mapper;

import com.itheima.pojo.AnswerRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnswerRecordMapper {

    /**
     * 新增答题记录
     */
    @Insert("insert into answer_record(exam_id, student_id, question_id, student_answer, is_correct, score, submit_time) " +
            "values(#{examId}, #{studentId}, #{questionId}, #{studentAnswer}, #{isCorrect}, #{score}, #{submitTime})")
    void insert(AnswerRecord record);

    /**
     * 批量新增答题记录
     */
    void batchInsert(List<AnswerRecord> records);

    /**
     * 根据考试ID和学生ID查询答题记录
     */
    @Select("select ar.*, q.type, q.content, q.options, q.answer, q.score " +
            "from answer_record ar left join question q on ar.question_id = q.id " +
            "where ar.exam_id = #{examId} and ar.student_id = #{studentId} " +
            "order by ar.question_id")
    List<AnswerRecord> findByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    /**
     * 根据考试ID和学生ID删除答题记录
     */
    @Delete("delete from answer_record where exam_id = #{examId} and student_id = #{studentId}")
    void deleteByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);
}
