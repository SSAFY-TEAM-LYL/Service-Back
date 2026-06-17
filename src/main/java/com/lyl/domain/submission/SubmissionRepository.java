package com.lyl.domain.submission;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository {

    Submission save(Submission submission);

    Optional<Submission> findById(Long id);

    Optional<Submission> findByIdAndMemberId(Long id, Long memberId);

    List<Submission> findProblemSubmissionsPage(
            String problemId,
            Long memberId,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int size
    );

    List<Submission> findInProgressSubmissions(int size);

    void delete(Submission submission);
}
