package com.lyl.application.submission;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.MemberSolvedProblem;
import com.lyl.domain.member.MemberSolvedProblemRepository;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDifficulty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionRewardService {

    private final MemberRepository memberRepository;
    private final MemberSolvedProblemRepository solvedProblemRepository;
    private final ProblemBankProblemRepository problemBankProblemRepository;

    @Transactional
    public void rewardAcceptedProblem(Long memberId, String problemId) {
        if (solvedProblemRepository.existsByMemberIdAndProblemId(memberId, problemId)) {
            return;
        }
        int earnedXp = problemBankProblemRepository.findDifficultyById(problemId)
                .flatMap(ProblemDifficulty::parse)
                .map(ProblemDifficulty::xp)
                .orElse(0);
        if (earnedXp <= 0) {
            return;
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        solvedProblemRepository.save(new MemberSolvedProblem(member, problemId, earnedXp));
        member.addXp(earnedXp);
    }
}
