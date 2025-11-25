package com.emergency.enrollment.repository.Enrollment;

import com.emergency.enrollment.domain.Enrollment;
import com.emergency.enrollment.domain.EnrollmentListItem;
import com.emergency.enrollment.domain.MyStudyCourseItem;
import com.emergency.enrollment.domain.MyStudyStatusCount;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * enrollments 테이블 JdbcTemplate 구현체
 *
 * 사용처
 *  - EnrollmentService.enroll() : existsByUserAndCourse(), insert()
 *  - EnrollmentService.getMyEnrollments() : findMyEnrollments()
 *  - 영상 진도 반영 : updateProgressPercent()
 */
@Repository("EnrollmentRepository")
@RequiredArgsConstructor
public class JdbcEnrollmentRepository implements EnrollmentRepository {

    private final JdbcTemplate jdbc;

    @Override
    public boolean existsByUserAndCourse(Long userId, Long courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE user_id = ? AND course_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId, courseId);
        return count != null && count > 0;
    }

    @Override
    public Long insert(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps =
                    con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, enrollment.getUserId());
            ps.setLong(2, enrollment.getCourseId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        Long id = (key != null) ? key.longValue() : null;
        enrollment.setEnrollmentId(id);
        return id;
    }

    @Override
    public List<EnrollmentListItem> findMyEnrollments(Long userId,
                                                      String statusFilter,
                                                      String midCategory) {

        StringBuilder sb = new StringBuilder("""
            SELECT e.enrollment_id,
                   e.course_id,
                   c.title,
                   c.top_category,
                   c.mid_category,
                   c.summary,
                   c.image_path,
                   e.progress_percent,
                   e.status,
                   e.enrolled_at
              FROM enrollments e
              JOIN courses c ON c.course_id = e.course_id
             WHERE e.user_id = ?
            """);

        List<Object> params = new ArrayList<>();
        params.add(userId);

        // 상태 필터 (수강중 / 미수료 / 수료)
        if (statusFilter != null && !statusFilter.isBlank()) {
            sb.append(" AND e.status = ? ");
            params.add(statusFilter);
        }

        // 중간 카테고리 필터 (출혈/기도막힘/심정지/화상)
        if (midCategory != null && !midCategory.isBlank()) {
            sb.append(" AND c.mid_category = ? ");
            params.add(midCategory);
        }

        sb.append(" ORDER BY e.enrolled_at DESC, e.enrollment_id DESC ");

        String sql = sb.toString();

        return jdbc.query(sql, (rs, rowNum) -> {
            Long enrollmentId = rs.getLong("enrollment_id");
            Long courseId = rs.getLong("course_id");
            String title = rs.getString("title");
            String topCat = rs.getString("top_category");
            String midCat = rs.getString("mid_category");
            String summary = rs.getString("summary");
            String imagePath = rs.getString("image_path");
            BigDecimal progress = rs.getBigDecimal("progress_percent");
            String status = rs.getString("status");
            Timestamp enrolledTs = rs.getTimestamp("enrolled_at");
            LocalDateTime enrolledAt =
                    (enrolledTs != null ? enrolledTs.toLocalDateTime() : null);

            return new EnrollmentListItem(
                    enrollmentId,
                    courseId,
                    title,
                    topCat,
                    midCat,
                    summary,
                    imagePath,
                    progress,
                    status,
                    enrolledAt
            );
        }, params.toArray());
    }

    // === [추가] 진도율 계산/조회용 ===

    @Override
    public Enrollment findById(Long enrollmentId) {
        String sql = """
                SELECT enrollment_id,
                       user_id,
                       course_id,
                       progress_percent,
                       status,
                       enrolled_at,
                       passed_at
                  FROM enrollments
                 WHERE enrollment_id = ?
                """;

        return jdbc.queryForObject(sql, (rs, rowNum) -> {
            Enrollment e = new Enrollment();
            e.setEnrollmentId(rs.getLong("enrollment_id"));
            e.setUserId(rs.getLong("user_id"));
            e.setCourseId(rs.getLong("course_id"));
            e.setProgressPercent(rs.getBigDecimal("progress_percent"));
            e.setStatus(rs.getString("status"));

            Timestamp enrolledAtTs = rs.getTimestamp("enrolled_at");
            if (enrolledAtTs != null) {
                e.setEnrolledAt(enrolledAtTs.toLocalDateTime());
            }

            Timestamp passedAtTs = rs.getTimestamp("passed_at");
            if (passedAtTs != null) {
                e.setPassedAt(passedAtTs.toLocalDateTime());
            }

            return e;
        }, enrollmentId);
    }

    /**
     * 진도율 업데이트
     *
     * - progress_percent 는 최대 100 으로 캡
     * - 진도율이 100% 이상이 되면
     *      -> status 가 '수료'가 아닌 경우 모두 '미수료' 로 변경
     * - 100% 미만일 때는 status 를 건드리지 않음
     */
    @Override
    public void updateProgressPercent(Long enrollmentId,
                                      BigDecimal progressPercent) {

        if (progressPercent == null) {
            return;
        }

        BigDecimal hundred = new BigDecimal("100");

        // 0 미만 방지 + 100 초과 방지 (선택)
        if (progressPercent.compareTo(BigDecimal.ZERO) < 0) {
            progressPercent = BigDecimal.ZERO;
        }
        if (progressPercent.compareTo(hundred) > 0) {
            progressPercent = hundred;
        }

        boolean isFullProgress = (progressPercent.compareTo(hundred) >= 0);

        if (isFullProgress) {
            // 🔹 100% 이상이면: 진도율 100 저장 + '수료'가 아닌 상태는 전부 '미수료'
            String sql = """
                    UPDATE enrollments
                       SET progress_percent = ?,
                           status = CASE
                                      WHEN status <> '수료' THEN '미수료'
                                      ELSE status
                                   END
                     WHERE enrollment_id = ?
                    """;
            jdbc.update(sql, progressPercent, enrollmentId);
        } else {
            // 🔹 100% 미만이면: 진도율만 갱신
            String sql = """
                    UPDATE enrollments
                       SET progress_percent = ?
                     WHERE enrollment_id = ?
                    """;
            jdbc.update(sql, progressPercent, enrollmentId);
        }
    }
    /*나의학습활동 추가 부분*/
    @Override
    public MyStudyStatusCount countMyStudyStatus(Long userId) {
        String sql = """
                SELECT status, COUNT(*) AS cnt
                FROM enrollments
                WHERE user_id = ?
                GROUP BY status
                """;

        return jdbc.query(sql, rs -> {
            long studying = 0L;      // 수강중
            long completed = 0L;     // 수료
            long notCompleted = 0L;  // 미수료

            while (rs.next()) {
                String status = rs.getString("status");
                long cnt = rs.getLong("cnt");

                if ("수강중".equals(status)) {
                    studying = cnt;
                } else if ("수료".equals(status)) {
                    completed = cnt;
                } else if ("미수료".equals(status)) {
                    notCompleted = cnt;
                }
            }

            return new MyStudyStatusCount(studying, completed, notCompleted);
        }, userId);
    }
    // === 나의 학습활동 - 강의 이어보기용 쿼리 ===
    @Override
    public List<MyStudyCourseItem> findMyOngoingCourses(Long userId, int limit) {
        String sql = """
                SELECT e.enrollment_id,
                       e.course_id,
                       c.title,
                       c.top_category,
                       c.mid_category,
                       c.summary,
                       c.image_path,
                       e.progress_percent
                FROM enrollments e
                JOIN courses c ON e.course_id = c.course_id
                WHERE e.user_id = ?
                  AND e.status = '수강중'
                ORDER BY e.enrolled_at DESC
                LIMIT ?
                """;

        return jdbc.query(sql,
                (rs, rowNum) -> new MyStudyCourseItem(
                        rs.getLong("enrollment_id"),
                        rs.getLong("course_id"),
                        rs.getString("title"),
                        rs.getString("top_category"),
                        rs.getString("mid_category"),
                        rs.getString("summary"),
                        rs.getString("image_path"),
                        rs.getBigDecimal("progress_percent")
                ),
                userId, limit
        );
    }


}
