package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.OAuthAccount;
import com.lyl.domain.member.OAuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<OAuthAccount> findByProviderAndProviderUserIdAndDeletedAtIsNull(
            OAuthProvider provider,
            String providerUserId
    );
}
