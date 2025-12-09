package com.itheima.mapper;

import com.itheima.pojo.ExamScore;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamScoreMapper {

    /**
     * 新增成绩
     */
    @Insert("insert into exam_score(exam_id, student_id, total_score, correct_count, " +
            "submit_time, status, create_time, update_time) values(#{examId}, #{studentId}, " +
            "#{totalScore}, #{correctCount}, #{submitTime}, #{status}, #{createTime}, #{updateTime})")
    void insert(ExamScore examScore);

    /**
     * 修改成绩
     */
    @Update("update exam_score set total_score=#{totalScore}, correct_count=#{correctCount}, " +
            "submit_time=#{submitTime}, status=#{status}, update_time=#{updateTime} where id=#{id}")
    void update(ExamScore examScore);

    /**
     * 根据考试ID查询成绩列表
     */
    @Select("select es.*, u.name as student_name, e.exam_name, (e.choice_count + e.fill_count) as total_questions, e.total_score as passScore, es.total_score as score " +
            "from exam_score es left join user u on es.student_id = u.id " +
            "left join exam e on es.exam_id = e.id left join clazz c on u.clazz_id = c.id where es.exam_id = #{examId}")
    List<ExamScore> findByExamId(Integer examId);

    /**
     * 根据学生ID查询成绩列表
     */
    @Select("select es.*, u.name as student_name, e.exam_name, (e.choice_count + e.fill_count) as total_questions, e.total_score as passScore, es.total_score as score, c.clazz_name " +
            "from exam_score es left join user u on es.student_id = u.id " +
            "left join exam e on es.exam_id = e.id left join clazz c on u.clazz_id = c.id where es.student_id = #{studentId}")
    List<ExamScore> findByStudentId(Integer studentId);

    /**
     * 根据考试ID和学生ID查询成绩
     */
    @Select("select es.*, u.name as student_name, e.exam_name, (e.choice_count + e.fill_count) as total_questions, e.total_score as passScore, es.total_score as score, c.clazz_name " +
            "from exam_score es left join user u on es.student_id = u.id " +
            "left join exam e on es.exam_id = e.id left join clazz c on u.clazz_id = c.id where es.exam_id = #{examId} and es.student_id = #{studentId}")
    ExamScore findByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    /**
     * 获取考试排名
     */
    @Select("select es.*, u.name as student_name, e.exam_name, (e.choice_count + e.fill_count) as total_questions, e.total_score as passScore, es.total_score as score, c.clazz_name " +
            "from exam_score es left join user u on es.student_id = u.id " +
            "left join exam e on es.exam_id = e.id left join clazz c on u.clazz_id = c.id where es.exam_id = #{examId} " +
            "order by es.total_score desc, es.submit_time asc")
    List<ExamScore> getExamRanking(Integer examId);

    /**
     * 分页查询成绩列表
     */
    List<ExamScore> list(@Param("examId") Integer examId, @Param("studentId") Integer studentId, @Param("clazzId") Integer clazzId, @Param("studentName") String studentName, @Param("examName") String examName, @Param("isPass") Boolean isPass);

    /**
     * 根据教师ID查询所有班级的成绩统计
     */
    List<Map<String, Object>> getStatisticsByTeacher(@Param("teacherId") Integer teacherId);

    /**
     * 根据班级ID查询成绩统计
     */
    Map<String, Object> getStatisticsByClazz(@Param("clazzId") Integer clazzId);

    /**
     * 根据考试ID查询成绩统计
     */
    Map<String, Object> getStatisticsByExam(@Param("examId") Integer examId);

    /**
     * 查询班级成绩排名
     */
    List<Map<String, Object>> getClazzRanking(@Param("clazzId") Integer clazzId);

    /**
     * 查询教师所带班级的成绩对比
     */
    List<Map<String, Object>> getTeacherClazzComparison(@Param("teacherId") Integer teacherId);

    /**
     * 获取班级基本信息
     */
    Map<String, Object> getClazzInfo(@Param("clazzId") Integer clazzId);

    /**
     * 获取班级最近考试统计
     */
    List<Map<String, Object>> getRecentExamsByClazz(@Param("clazzId") Integer clazzId);

    /**
     * 获取考试统计报告数据
     */
    List<Map<String, Object>> getExamReportData(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("examName") String examName, @Param("bankId") Integer bankId);

    /**
     * 获取考试趋势统计数据
     */
    List<Map<String, Object>> getExamTrendData(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("bankId") Integer bankId);

    /**
     * 获取总体统计数据
     */
    Map<String, Object> getOverallStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("bankId") Integer bankId);

    /**
     * 获取成绩分数段统计
     */
    List<Map<String, Object>> getScoreSegments(@Param("examId") Integer examId, @Param("clazzId") Integer clazzId);

    /**
     * 删除学生的成绩记录
     */
    @Delete("delete from exam_score where exam_id = #{examId} and student_id = #{studentId}")
    void deleteByExamAndStudent(@Param("examId") Integer examId, @Param("studentId") Integer studentId);
}