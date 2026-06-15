package com.lyl.infrastructure.persistence.problem;

import com.lyl.domain.problem.ProblemPublication;
import com.lyl.domain.problem.ProblemPublicationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProblemPublicationRepositoryAdapter implements ProblemPublicationRepository {

    private final SpringDataProblemPublicationRepository repository;

    @Override
    public ProblemPublication save(ProblemPublication publication) {
        return repository.save(publication);
    }

    @Override
    public Optional<ProblemPublication> findByProblemId(String problemId) {
        return repository.findByProblemId(problemId);
    }

    @Override
    public Optional<ProblemPublication> findPublishedByProblemId(String problemId) {
        return repository.findByProblemIdAndDeletedAtIsNull(problemId);
    }

    @Override
    public List<String> findPublishedProblemIdsByProblemIds(List<String> problemIds) {
        if (problemIds.isEmpty()) {
            return List.of();
        }
        return repository.findPublishedProblemIds(problemIds);
    }

    @Override
    public List<ProblemPublication> findPublishedPage(LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        return repository.findPublishedCursorPage(cursorCreatedAt, cursorId, PageRequest.of(0, size));
    }

    @Override
    public void unpublish(ProblemPublication publication) {
        publication.unpublish();
        repository.save(publication);
    }
}
