package com.itheima.mapper;

import com.itheima.pojo.Exam;
import com.itheima.pojo.ExamQueryParam;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamMapper {

    /**
     * 分页查询考试列表
     */
    List<Exam> list(ExamQueryParam queryParam);

    /**
     * 新增考试
     */
    @Insert("insert into exam(exam_name, teacher_id, bank_id, clazz_id, start_time, duration, " +
            "choice_count, fill_count, total_score, status, create_time, update_time) " +
            "values(#{examName}, #{teacherId}, #{bankId}, #{clazzId}, #{startTime}, #{duration}, " +
            "#{choiceCount}, #{fillCount}, #{totalScore}, #{status}, #{createTime}, #{updateTime})")
    void insert(Exam exam);

    /**
     * 根据ID查询考试
     */
    @Select("select e.*, u.name as teacher_name, qb.bank_name as bank_name, c.clazz_name as clazz_name " +
            "from exam e left join user u on e.teacher_id = u.id " +
            "left join question_bank qb on e.bank_id = qb.id " +
            "left join clazz c on e.clazz_id = c.id where e.id = #{id}")
    Exam getById(Integer id);

    /**
     * 修改考试
     */
    @Update("update exam set exam_name=#{examName}, teacher_id=#{teacherId}, bank_id=#{bankId}, " +
            "clazz_id=#{clazzId}, start_time=#{startTime}, duration=#{duration}, choice_count=#{choiceCount}, " +
            "fill_count=#{fillCount}, total_score=#{totalScore}, status=#{status}, " +
            "update_time=#{updateTime} where id=#{id}")
    void update(Exam exam);

    /**
     * 批量删除考试
     */
    void deleteByIds(List<Integer> ids);

    /**
     * 根据教师ID查询考试
     */
    @Select("select e.*, u.name as teacher_name, qb.bank_name as bank_name " +
            "from exam e left join user u on e.teacher_id = u.id " +
            "left join question_bank qb on e.bank_id = qb.id where e.teacher_id = #{teacherId}")
    List<Exam> findByTeacherId(Integer teacherId);

    /**
     * 查询学生可参加的考试（仅限自己班级的考试）
     */
    @Select("select e.*, u.name as teacher_name, qb.bank_name as bank_name, c.clazz_name as clazz_name " +
            "from exam e left join user u on e.teacher_id = u.id " +
            "left join question_bank qb on e.bank_id = qb.id " +
            "left join clazz c on e.clazz_id = c.id " +
            "where e.status >= 1 and e.clazz_id = (select clazz_id from user where id = #{studentId}) " +
            "and not exists (select 1 from exam_score es where es.exam_id = e.id and es.student_id = #{studentId})")
    List<Exam> findAvailableExams(Integer studentId);

    /**
     * 更新考试状态
     */
    @Update("update exam set status = #{status}, update_time = now() where id = #{examId}")
    void updateStatus(@Param("examId") Integer examId, @Param("status") Integer status);
}