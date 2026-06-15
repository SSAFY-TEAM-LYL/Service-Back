package com.lyl.domain.problem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProblemPublicationRepository {

    ProblemPublication save(ProblemPublication publication);

    Optional<ProblemPublication> findByProblemId(String problemId);

    Optional<ProblemPublication> findPublishedByProblemId(String problemId);

    List<ProblemPublication> findPublishedPage(LocalDateTime cursorCreatedAt, Long cursorId, int size);

    void unpublish(ProblemPublication publication);
}
