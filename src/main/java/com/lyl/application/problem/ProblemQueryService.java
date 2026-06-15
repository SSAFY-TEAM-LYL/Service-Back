package com.lyl.application.problem;

import com.lyl.application.common.Cursor;
import com.lyl.application.common.CursorCodec;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemPublication;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.domain.problem.exception.ProblemNotFoundException;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.problem.dto.ProblemDetailResponse;
import com.lyl.presentation.problem.dto.ProblemSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProblemPublicationRepository problemPublicationRepository;
    private final ProblemBankProblemRepository problemBankProblemRepository;
    private final CursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public CursorPageResponse<ProblemSummaryResponse> findProblems(String cursor, Integer size) {
        int pageSize = normalizeSize(size);
        Cursor decodedCursor = cursorCodec.decode(cursor);
        List<ProblemPublication> publications = problemPublicationRepository.findPublishedPage(
                cursorCreatedAt(decodedCursor),
                cursorId(decodedCursor),
                pageSize + 1
        );
        boolean hasNext = publications.size() > pageSize;
        List<ProblemPublication> pagePublications = publications.stream()
                .limit(pageSize)
                .toList();
        List<String> problemIds = pagePublications.stream()
                .map(ProblemPublication::getProblemId)
                .toList();
        Map<String, ProblemSummary> summaries = problemBankProblemRepository.findSummariesByIds(problemIds).stream()
                .collect(Collectors.toMap(ProblemSummary::id, Function.identity()));
        List<ProblemSummaryResponse> items = problemIds.stream()
                .map(summaries::get)
                .filter(summary -> summary != null)
                .map(ProblemSummaryResponse::from)
                .toList();
        String nextCursor = hasNext && !pagePublications.isEmpty()
                ? cursorCodec.encode(pagePublications.getLast().getCreatedAt(), pagePublications.getLast().getId())
                : null;
        return new CursorPageResponse<>(items, nextCursor, hasNext);
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

    private LocalDateTime cursorCreatedAt(Cursor cursor) {
        return cursor == null ? null : cursor.createdAt();
    }

    private Long cursorId(Cursor cursor) {
        return cursor == null ? null : cursor.id();
    }
}
