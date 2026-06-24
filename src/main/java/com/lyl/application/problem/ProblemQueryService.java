package com.lyl.application.problem;

import com.lyl.domain.problem.ProblemAlgorithmCatalog;
import com.lyl.domain.problem.ProblemAlgorithmType;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.domain.problem.exception.ProblemNotFoundException;
import com.lyl.domain.submission.SubmissionRepository;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.problem.dto.ProblemAlgorithmResponse;
import com.lyl.presentation.problem.dto.ProblemDetailResponse;
import com.lyl.presentation.problem.dto.ProblemServiceSummaryResponse;
import com.lyl.presentation.problem.dto.ProblemSummaryResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final ProblemPublicationRepository problemPublicationRepository;
    private final ProblemBankProblemRepository problemBankProblemRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<ProblemSummaryResponse> findProblems(
            String cursor,
            Integer size,
            String difficulty,
            String algorithm,
            String query
    ) {
        int pageSize = normalizeSize(size);
        int offset = decodeOffset(cursor);
        List<String> publishedProblemIds = problemPublicationRepository.findAllPublishedProblemIds();
        List<ProblemSummary> summaries = problemBankProblemRepository.findSummariesByIds(
                publishedProblemIds,
                normalizeDifficulty(difficulty),
                normalizeAlgorithm(algorithm),
                normalizeQuery(query),
                offset,
                pageSize + 1
        );
        boolean hasNext = summaries.size() > pageSize;
        List<ProblemSummaryResponse> items = summaries.stream()
                .limit(pageSize)
                .map(ProblemSummaryResponse::from)
                .toList();
        String nextCursor = hasNext ? encodeOffset(offset + pageSize) : null;
        return new CursorPageResponse<>(items, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public List<ProblemAlgorithmResponse> findAlgorithms() {
        return ProblemAlgorithmCatalog.supported().stream()
                .map(ProblemAlgorithmResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProblemServiceSummaryResponse findSummary() {
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        LocalDateTime startInclusive = today.atStartOfDay();
        LocalDateTime endExclusive = today.plusDays(1).atStartOfDay();
        return new ProblemServiceSummaryResponse(
                problemPublicationRepository.countPublishedProblems(),
                submissionRepository.countSubmittedBetween(startInclusive, endExclusive)
        );
    }

    @Transactional(readOnly = true)
    public ProblemDetailResponse findProblem(String problemId) {
        problemPublicationRepository.findPublishedByProblemId(problemId)
                .orElseThrow(ProblemNotFoundException::new);
        ProblemDetail detail = problemBankProblemRepository.findDetailById(problemId)
                .orElseThrow(ProblemNotFoundException::new);
        return ProblemDetailResponse.from(detail);
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private String normalizeDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return null;
        }
        return difficulty.trim();
    }

    private String normalizeAlgorithm(String algorithm) {
        if (algorithm == null || algorithm.isBlank()) {
            return null;
        }
        return ProblemAlgorithmType.fromCode(algorithm)
                .map(ProblemAlgorithmType::code)
                .orElse(algorithm.trim());
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }

    private int decodeOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        if (!cursor.startsWith("offset:")) {
            return 0;
        }
        try {
            return Math.max(Integer.parseInt(cursor.substring("offset:".length())), 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String encodeOffset(int offset) {
        return "offset:" + Math.max(offset, 0);
    }
}
