package com.lyl.application.submission;

import com.lyl.application.common.Cursor;
import com.lyl.application.common.CursorCodec;
import com.lyl.application.streak.DailyStreakService;
import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemJudgingData;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.ProblemTestCase;
import com.lyl.domain.problem.exception.ProblemNotFoundException;
import com.lyl.domain.submission.Submission;
import com.lyl.domain.submission.SubmissionLanguage;
import com.lyl.domain.submission.SubmissionRepository;
import com.lyl.domain.submission.SubmissionReview;
import com.lyl.domain.submission.SubmissionReviewRepository;
import com.lyl.domain.submission.SubmissionStatus;
import com.lyl.domain.submission.SubmissionTestCaseResult;
import com.lyl.domain.submission.exception.SubmissionAccessDeniedException;
import com.lyl.domain.submission.exception.SubmissionNotFoundException;
import com.lyl.domain.submission.exception.SubmissionReviewNotFoundException;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.submission.dto.SubmissionCreateRequest;
import com.lyl.presentation.submission.dto.SubmissionResponse;
import com.lyl.presentation.submission.dto.SubmissionReviewCreateRequest;
import com.lyl.presentation.submission.dto.SubmissionReviewResponse;
import com.lyl.presentation.submission.dto.SubmissionReviewUpdateRequest;
import com.lyl.presentation.submission.dto.SubmissionUpdateRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_TIME_LIMIT_MS = 1000;

    private final MemberRepository memberRepository;
    private final ProblemPublicationRepository problemPublicationRepository;
    private final ProblemBankProblemRepository problemBankProblemRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionReviewRepository submissionReviewRepository;
    private final Judge0Client judge0Client;
    private final CursorCodec cursorCodec;
    private final DailyStreakService dailyStreakService;
    private final SubmissionRewardService submissionRewardService;

    @Transactional
    public SubmissionResponse createSubmission(String problemId, SubmissionCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        problemPublicationRepository.findPublishedByProblemId(problemId)
                .orElseThrow(ProblemNotFoundException::new);
        ProblemJudgingData judgingData = problemBankProblemRepository.findJudgingDataById(problemId)
                .orElseThrow(ProblemNotFoundException::new);
        if (judgingData.testCases().isEmpty()) {
            throw new IllegalArgumentException("채점 테스트 케이스가 없습니다.");
        }

        SubmissionLanguage language = SubmissionLanguage.from(request.language());
        Submission submission = new Submission(
                member,
                problemId,
                language,
                request.sourceCode(),
                judgingData.testCases().size()
        );
        Submission savedSubmission = submissionRepository.save(submission);
        submitToJudge(savedSubmission, language, request.sourceCode(), judgingData);
        dailyStreakService.recordTodaySubmission(memberId);
        return SubmissionResponse.from(savedSubmission);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse findSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(SubmissionNotFoundException::new);
        return SubmissionResponse.from(submission);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<SubmissionResponse> findProblemSubmissions(
            String problemId,
            Long memberId,
            String cursor,
            Integer size
    ) {
        problemPublicationRepository.findPublishedByProblemId(problemId)
                .orElseThrow(ProblemNotFoundException::new);
        int pageSize = normalizeSize(size);
        Cursor decodedCursor = cursorCodec.decode(cursor);
        List<Submission> submissions = submissionRepository.findProblemSubmissionsPage(
                problemId,
                memberId,
                cursorCreatedAt(decodedCursor),
                cursorId(decodedCursor),
                pageSize + 1
        );
        boolean hasNext = submissions.size() > pageSize;
        List<Submission> pageSubmissions = submissions.stream()
                .limit(pageSize)
                .toList();
        String nextCursor = hasNext && !pageSubmissions.isEmpty()
                ? cursorCodec.encode(pageSubmissions.getLast().getCreatedAt(), pageSubmissions.getLast().getId())
                : null;
        return new CursorPageResponse<>(
                pageSubmissions.stream().map(SubmissionResponse::from).toList(),
                nextCursor,
                hasNext
        );
    }

    @Transactional
    public SubmissionResponse updateSubmission(Long submissionId, SubmissionUpdateRequest request, Long memberId) {
        Submission submission = submissionRepository.findByIdAndMemberId(submissionId, memberId)
                .orElseThrow(SubmissionNotFoundException::new);
        problemPublicationRepository.findPublishedByProblemId(submission.getProblemId())
                .orElseThrow(ProblemNotFoundException::new);
        ProblemJudgingData judgingData = problemBankProblemRepository.findJudgingDataById(submission.getProblemId())
                .orElseThrow(ProblemNotFoundException::new);
        if (judgingData.testCases().isEmpty()) {
            throw new IllegalArgumentException("채점 테스트 케이스가 없습니다.");
        }

        SubmissionLanguage language = SubmissionLanguage.from(request.language());
        submission.updateForRejudge(language, request.sourceCode(), judgingData.testCases().size());
        submitToJudge(submission, language, request.sourceCode(), judgingData);
        dailyStreakService.recordTodaySubmission(memberId);
        return SubmissionResponse.from(submission);
    }

    @Transactional
    public void deleteSubmission(Long submissionId, Long memberId) {
        Submission submission = submissionRepository.findByIdAndMemberId(submissionId, memberId)
                .orElseThrow(SubmissionNotFoundException::new);
        submissionRepository.delete(submission);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<SubmissionReviewResponse> findSubmissionReviews(
            Long submissionId,
            String cursor,
            Integer size
    ) {
        submissionRepository.findById(submissionId)
                .orElseThrow(SubmissionNotFoundException::new);
        int pageSize = normalizeSize(size);
        Cursor decodedCursor = cursorCodec.decode(cursor);
        List<SubmissionReview> reviews = submissionReviewRepository.findPageBySubmissionId(
                submissionId,
                cursorCreatedAt(decodedCursor),
                cursorId(decodedCursor),
                pageSize + 1
        );
        boolean hasNext = reviews.size() > pageSize;
        List<SubmissionReview> pageReviews = reviews.stream()
                .limit(pageSize)
                .toList();
        String nextCursor = hasNext && !pageReviews.isEmpty()
                ? cursorCodec.encode(pageReviews.getLast().getCreatedAt(), pageReviews.getLast().getId())
                : null;
        return new CursorPageResponse<>(
                pageReviews.stream().map(SubmissionReviewResponse::from).toList(),
                nextCursor,
                hasNext
        );
    }

    @Transactional
    public SubmissionReviewResponse createSubmissionReview(
            Long submissionId,
            SubmissionReviewCreateRequest request,
            Long memberId
    ) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(SubmissionNotFoundException::new);
        Member author = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        SubmissionReview review = new SubmissionReview(submission, author, request.content());
        return SubmissionReviewResponse.from(submissionReviewRepository.save(review));
    }

    @Transactional
    public SubmissionReviewResponse updateSubmissionReview(
            Long reviewId,
            SubmissionReviewUpdateRequest request,
            Long memberId
    ) {
        SubmissionReview review = submissionReviewRepository.findById(reviewId)
                .orElseThrow(SubmissionReviewNotFoundException::new);
        validateReviewAccess(review, memberId);
        review.update(request.content());
        return SubmissionReviewResponse.from(review);
    }

    @Transactional
    public void deleteSubmissionReview(Long reviewId, Long memberId) {
        SubmissionReview review = submissionReviewRepository.findById(reviewId)
                .orElseThrow(SubmissionReviewNotFoundException::new);
        validateReviewAccess(review, memberId);
        submissionReviewRepository.delete(review);
    }

    @Transactional
    public void refreshInProgressSubmissions(int size) {
        List<Submission> submissions = submissionRepository.findInProgressSubmissions(size);
        for (Submission submission : submissions) {
            refreshSubmission(submission);
        }
    }

    private void refreshSubmission(Submission submission) {
        List<String> tokens = submission.getTestCaseResults().stream()
                .map(SubmissionTestCaseResult::getJudge0Token)
                .filter(token -> token != null && !token.isBlank())
                .toList();
        if (tokens.isEmpty()) {
            return;
        }
        Map<String, Judge0SubmissionResult> results = judge0Client.fetchBatchResults(tokens).stream()
                .collect(Collectors.toMap(Judge0SubmissionResult::token, Function.identity()));
        for (SubmissionTestCaseResult testCaseResult : submission.getTestCaseResults()) {
            Judge0SubmissionResult result = results.get(testCaseResult.getJudge0Token());
            if (result == null || !result.completed()) {
                continue;
            }
            testCaseResult.updateResult(
                    result.status(),
                    result.timeMs(),
                    result.memoryKb(),
                    result.stderrText(),
                    result.compileOutput(),
                    result.message()
            );
        }
        boolean allCompleted = submission.getTestCaseResults().stream()
                .allMatch(SubmissionTestCaseResult::isCompleted);
        if (allCompleted) {
            submission.aggregateCompletedResults();
            if (submission.getStatus() == SubmissionStatus.ACCEPTED) {
                submissionRewardService.rewardAcceptedProblem(
                        submission.getMember().getId(),
                        submission.getProblemId()
                );
            }
        }
    }

    private void submitToJudge(
            Submission submission,
            SubmissionLanguage language,
            String sourceCode,
            ProblemJudgingData judgingData
    ) {
        for (ProblemTestCase testCase : judgingData.testCases()) {
            submission.addTestCaseResult(new SubmissionTestCaseResult(testCase.seq()));
        }
        List<Judge0SubmissionToken> tokens = judge0Client.submitBatch(judgingData.testCases().stream()
                .map(testCase -> new Judge0SubmissionRequest(
                        testCase.seq(),
                        language,
                        sourceCode,
                        testCase,
                        normalizeTimeLimit(judgingData.timeLimitMs())
                ))
                .toList());
        assignJudgeTokens(submission, tokens);
        submission.markJudging();
    }

    private void validateReviewAccess(SubmissionReview review, Long memberId) {
        if (!review.isWrittenBy(memberId) || review.getSubmission().isDeleted()) {
            throw new SubmissionAccessDeniedException();
        }
    }

    private void assignJudgeTokens(Submission submission, List<Judge0SubmissionToken> tokens) {
        Map<Integer, Judge0SubmissionToken> tokensBySeq = tokens.stream()
                .collect(Collectors.toMap(Judge0SubmissionToken::caseSeq, Function.identity()));
        submission.getTestCaseResults().stream()
                .sorted(Comparator.comparingInt(SubmissionTestCaseResult::getCaseSeq))
                .forEach(result -> {
                    Judge0SubmissionToken token = tokensBySeq.get(result.getCaseSeq());
                    if (token == null) {
                        result.updateResult(
                                SubmissionStatus.INTERNAL_ERROR,
                                null,
                                null,
                                null,
                                null,
                                "채점 서버 응답이 올바르지 않습니다."
                        );
                        return;
                    }
                    result.assignJudge0Token(token.token());
                });
    }

    private int normalizeTimeLimit(Integer timeLimitMs) {
        return timeLimitMs == null || timeLimitMs <= 0 ? DEFAULT_TIME_LIMIT_MS : timeLimitMs;
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
