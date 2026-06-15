package com.lyl.application.problem;

import com.lyl.application.common.Cursor;
import com.lyl.application.common.CursorCodec;
import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.Role;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.domain.problem.ProblemPublication;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.exception.ProblemAccessDeniedException;
import com.lyl.domain.problem.exception.ProblemPublicationNotFoundException;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.problem.dto.ProblemPublicationCreateRequest;
import com.lyl.presentation.problem.dto.ProblemPublicationResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemPublicationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProblemPublicationRepository problemPublicationRepository;
    private final MemberRepository memberRepository;
    private final CursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public CursorPageResponse<ProblemPublicationResponse> findPublished(String cursor, Integer size, Long adminId) {
        validateAdmin(adminId);
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
        String nextCursor = hasNext && !pagePublications.isEmpty()
                ? cursorCodec.encode(pagePublications.getLast().getCreatedAt(), pagePublications.getLast().getId())
                : null;
        List<ProblemPublicationResponse> items = pagePublications.stream()
                .map(ProblemPublicationResponse::from)
                .toList();
        return new CursorPageResponse<>(items, nextCursor, hasNext);
    }

    @Transactional
    public ProblemPublicationResponse publish(ProblemPublicationCreateRequest request, Long adminId) {
        validateAdmin(adminId);
        ProblemPublication publication = problemPublicationRepository.findByProblemId(request.problemId())
                .map(existingPublication -> {
                    existingPublication.publish();
                    return existingPublication;
                })
                .orElseGet(() -> new ProblemPublication(request.problemId()));
        ProblemPublication savedPublication = problemPublicationRepository.save(publication);
        return ProblemPublicationResponse.from(savedPublication);
    }

    @Transactional
    public void unpublish(String problemId, Long adminId) {
        validateAdmin(adminId);
        ProblemPublication publication = problemPublicationRepository.findPublishedByProblemId(problemId)
                .orElseThrow(ProblemPublicationNotFoundException::new);
        problemPublicationRepository.unpublish(publication);
    }

    private void validateAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        if (member.getRole() != Role.ADMIN) {
            throw new ProblemAccessDeniedException();
        }
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
