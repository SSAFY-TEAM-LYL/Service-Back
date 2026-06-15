package com.lyl.domain.member;

import java.util.Optional;

public interface OAuthAccountRepository {

    OAuthAccount save(OAuthAccount oauthAccount);

    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
}
