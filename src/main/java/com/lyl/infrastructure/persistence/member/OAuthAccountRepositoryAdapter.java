package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.OAuthAccount;
import com.lyl.domain.member.OAuthAccountRepository;
import com.lyl.domain.member.OAuthProvider;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OAuthAccountRepositoryAdapter implements OAuthAccountRepository {

    private final SpringDataOAuthAccountRepository repository;

    @Override
    public OAuthAccount save(OAuthAccount oauthAccount) {
        return repository.save(oauthAccount);
    }

    @Override
    public Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        return repository.findByProviderAndProviderUserIdAndDeletedAtIsNull(provider, providerUserId);
    }

    @Override
    public boolean existsByMemberId(Long memberId) {
        return repository.existsByMemberIdAndDeletedAtIsNull(memberId);
    }
}
