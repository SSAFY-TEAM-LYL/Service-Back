package com.lyl.application.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.Role;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemPublication;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.domain.problem.exception.ProblemAccessDeniedException;
import com.lyl.domain.problem.exception.ProblemPublicationNotFoundException;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.common.PageResponse;
import com.lyl.presentation.problem.dto.AdminProblemBankProblemResponse;
import com.lyl.presentation.problem.dto.ProblemPublicationCreateRequest;
import com.lyl.presentation.problem.dto.ProblemPublicationResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class ProblemPublicationServiceTest {

    @Autowired
    private ProblemPublicationService problemPublicationService;

    @Autowired
    private ProblemPublicationRepository problemPublicationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FakeProblemBankProblemRepository problemBankProblemRepository;

    @BeforeEach
    void setUp() {
        problemBankProblemRepository.clear();
    }

    @Test
    void publishCreatesPublicationWhenAdminRequests() {
        Member admin = saveMember("problem-publish-admin@example.com", "problemAdmin", Role.ADMIN);
        String problemId = "11111111-1111-4111-8111-111111111111";

        ProblemPublicationResponse response = problemPublicationService.publish(
                new ProblemPublicationCreateRequest(problemId),
                admin.getId()
        );

        assertThat(response.problemId()).isEqualTo(problemId);
        assertThat(response.published()).isTrue();
    }

    @Test
    void publishThrowsAccessDeniedWhenUserIsNotAdmin() {
        Member user = saveMember("problem-publish-user@example.com", "problemUser", Role.USER);

        assertThatThrownBy(() -> problemPublicationService.publish(
                new ProblemPublicationCreateRequest("22222222-2222-4222-8222-222222222222"),
                user.getId()
        )).isInstanceOf(ProblemAccessDeniedException.class);
    }

    @Test
    void findPublishedReturnsOnlyPublishedRows() {
        Member admin = saveMember("problem-list-admin@example.com", "problemListAdmin", Role.ADMIN);
        String publishedProblemId = "33333333-3333-4333-8333-333333333333";
        String unpublishedProblemId = "44444444-4444-4444-8444-444444444444";
        problemPublicationService.publish(new ProblemPublicationCreateRequest(publishedProblemId), admin.getId());
        problemPublicationService.publish(new ProblemPublicationCreateRequest(unpublishedProblemId), admin.getId());
        problemPublicationService.unpublish(unpublishedProblemId, admin.getId());

        CursorPageResponse<ProblemPublicationResponse> response =
                problemPublicationService.findPublished(null, 20, admin.getId());

        assertThat(response.items())
                .extracting(ProblemPublicationResponse::problemId)
                .contains(publishedProblemId)
                .doesNotContain(unpublishedProblemId);
    }

    @Test
    void findProblemBankProblemsReturnsPublishedStatusForAdmin() {
        Member admin = saveMember("problem-bank-list-admin@example.com", "problemBankAdmin", Role.ADMIN);
        String publishedProblemId = "77777777-7777-4777-8777-777777777777";
        String unpublishedProblemId = "88888888-8888-4888-8888-888888888888";
        problemBankProblemRepository.addSummary(new ProblemSummary(
                publishedProblemId,
                "공개된 문제",
                1000,
                OffsetDateTime.parse("2026-06-15T01:00:00Z")
        ));
        problemBankProblemRepository.addSummary(new ProblemSummary(
                unpublishedProblemId,
                "등록 가능한 문제",
                2000,
                OffsetDateTime.parse("2026-06-15T02:00:00Z")
        ));
        problemPublicationService.publish(new ProblemPublicationCreateRequest(publishedProblemId), admin.getId());

        PageResponse<AdminProblemBankProblemResponse> response =
                problemPublicationService.findProblemBankProblems(0, 20, admin.getId());

        assertThat(response.items())
                .extracting(AdminProblemBankProblemResponse::id)
                .containsExactly(publishedProblemId, unpublishedProblemId);
        assertThat(response.items())
                .filteredOn(AdminProblemBankProblemResponse::published)
                .extracting(AdminProblemBankProblemResponse::id)
                .containsExactly(publishedProblemId);
    }

    @Test
    void unpublishSoftDeletesPublication() {
        Member admin = saveMember("problem-unpublish-admin@example.com", "problemUnpublishAdmin", Role.ADMIN);
        String problemId = "55555555-5555-4555-8555-555555555555";
        ProblemPublicationResponse response = problemPublicationService.publish(
                new ProblemPublicationCreateRequest(problemId),
                admin.getId()
        );

        problemPublicationService.unpublish(problemId, admin.getId());

        ProblemPublication publication = problemPublicationRepository.findByProblemId(problemId).orElseThrow();
        assertThat(publication.getDeletedAt()).isNotNull();
        assertThat(publication.getId()).isEqualTo(response.id());
        assertThatThrownBy(() -> problemPublicationService.unpublish(problemId, admin.getId()))
                .isInstanceOf(ProblemPublicationNotFoundException.class);
    }

    @Test
    void publishRestoresUnpublishedPublication() {
        Member admin = saveMember("problem-republish-admin@example.com", "problemRepublishAdmin", Role.ADMIN);
        String problemId = "66666666-6666-4666-8666-666666666666";
        ProblemPublicationResponse firstResponse = problemPublicationService.publish(
                new ProblemPublicationCreateRequest(problemId),
                admin.getId()
        );
        problemPublicationService.unpublish(problemId, admin.getId());

        ProblemPublicationResponse secondResponse = problemPublicationService.publish(
                new ProblemPublicationCreateRequest(problemId),
                admin.getId()
        );

        assertThat(secondResponse.id()).isEqualTo(firstResponse.id());
        assertThat(secondResponse.published()).isTrue();
    }

    private Member saveMember(String email, String nickname, Role role) {
        return memberRepository.save(new Member(email, nickname, "encoded-password", role));
    }

    @TestConfiguration
    static class ProblemPublicationServiceTestConfig {

        @Bean
        @Primary
        FakeProblemBankProblemRepository fakeProblemBankProblemRepository() {
            return new FakeProblemBankProblemRepository();
        }
    }

    static class FakeProblemBankProblemRepository implements ProblemBankProblemRepository {

        private final Map<String, ProblemSummary> summaries = new LinkedHashMap<>();

        @Override
        public List<ProblemSummary> findPublishedSummaries(int offset, int size) {
            return summaries.values().stream()
                    .skip(offset)
                    .limit(size)
                    .toList();
        }

        @Override
        public List<ProblemSummary> findSummariesByIds(List<String> problemIds) {
            List<ProblemSummary> result = new ArrayList<>();
            for (String problemId : problemIds) {
                ProblemSummary summary = summaries.get(problemId);
                if (summary != null) {
                    result.add(summary);
                }
            }
            return result;
        }

        @Override
        public Optional<ProblemDetail> findDetailById(String problemId) {
            return Optional.empty();
        }

        void addSummary(ProblemSummary summary) {
            summaries.put(summary.id(), summary);
        }

        void clear() {
            summaries.clear();
        }
    }
}
