package com.lyl.infrastructure.persistence.submission;

import com.lyl.domain.submission.Submission;
import com.lyl.domain.submission.SubmissionRepository;
import com.lyl.domain.submission.SubmissionStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubmissionRepositoryAdapter implements SubmissionRepository {

    private final SpringDataSubmissionRepository repository;

    @Override
    public Submission save(Submission submission) {
        return repository.save(submission);
    }

    @Override
    public Optional<Submission> findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Optional<Submission> findByIdAndMemberId(Long id, Long memberId) {
        return repository.findByIdAndMemberIdAndDeletedAtIsNull(id, memberId);
    }

    @Override
    public List<Submission> findProblemSubmissionsPage(
            String problemId,
            Long memberId,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int size
    ) {
        List<Long> ids = repository.findProblemSubmissionIdsPage(
                problemId,
                memberId,
                cursorCreatedAt,
                cursorId,
                PageRequest.of(0, size)
        );
        return findAllByIdsKeepingOrder(ids);
    }

    @Override
    public List<Submission> findInProgressSubmissions(int size) {
        List<Long> ids = repository.findInProgressSubmissionIds(
                List.of(SubmissionStatus.PENDING, SubmissionStatus.JUDGING),
                PageRequest.of(0, size)
        );
        return findAllByIdsKeepingOrder(ids);
    }

    @Override
    public void delete(Submission submission) {
        submission.delete();
        repository.save(submission);
    }

    private List<Submission> findAllByIdsKeepingOrder(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Submission> submissionsById = repository.findByIdInAndDeletedAtIsNull(ids).stream()
                .collect(Collectors.toMap(
                        Submission::getId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return ids.stream()
                .map(submissionsById::get)
                .filter(submission -> submission != null)
                .toList();
    }
}
