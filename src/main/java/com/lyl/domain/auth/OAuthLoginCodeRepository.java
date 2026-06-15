package com.lyl.domain.auth;

import java.util.Optional;

public interface OAuthLoginCodeRepository {

    OAuthLoginCode save(OAuthLoginCode oauthLoginCode);

    Optional<OAuthLoginCode> findByCodeHash(String codeHash);
}
