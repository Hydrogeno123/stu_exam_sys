package com.itheima.mapper;

import com.itheima.pojo.StudentAnswer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentAnswerMapper {

    /**
     * 新增答题记录
     */
    @Insert("insert into student_answer(exam_id, student_id, question_id, student_answer, correct_answer, is_correct, question_score, create_time, update_time) " +
            "values(#{examId}, #{studentId}, #{questionId}, #{studentAnswer}, #{correctAnswer}, #{isCorrect}, #{questionScore}, #{createTime}, #{updateTime})")
    void insert(StudentAnswer answer);

    /**
     * 批量新增答题记录
     */
    void batchInsert(List<StudentAnswer> answers);

    /**
     * 根据考试ID和学生ID查询答题记录
     */
    @Select("select sa.*, q.type, q.content, q.options, q.answer, q.score " +
            "from student_answer sa left join question q on sa.question_id = q.id " +
            "where sa.exam_id = #{examId} and sa.student_id = #{studentId} " +
            "order by sa.question_id")
    List<StudentAnswer> findByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    /**
     * 根据考试ID和学生ID删除答题记录
     */
    @Delete("delete from student_answer where exam_id = #{examId} and student_id = #{studentId}")
    void deleteByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);
}
