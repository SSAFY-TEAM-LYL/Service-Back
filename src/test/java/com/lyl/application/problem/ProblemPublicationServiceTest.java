package com.lyl.application.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.Role;
import com.lyl.domain.problem.ProblemPublication;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.exception.ProblemAccessDeniedException;
import com.lyl.domain.problem.exception.ProblemPublicationNotFoundException;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.problem.dto.ProblemPublicationCreateRequest;
import com.lyl.presentation.problem.dto.ProblemPublicationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProblemPublicationServiceTest {

    @Autowired
    private ProblemPublicationService problemPublicationService;

    @Autowired
    private ProblemPublicationRepository problemPublicationRepository;

    @Autowired
    private MemberRepository memberRepository;

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
}
