package com.lyl.infrastructure.persistence.auth;

import com.lyl.domain.auth.OAuthLoginCode;
import com.lyl.domain.auth.OAuthLoginCodeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OAuthLoginCodeRepositoryAdapter implements OAuthLoginCodeRepository {

    private final SpringDataOAuthLoginCodeRepository repository;

    @Override
    public OAuthLoginCode save(OAuthLoginCode oauthLoginCode) {
        return repository.save(oauthLoginCode);
    }

    @Override
    public Optional<OAuthLoginCode> findByCodeHash(String codeHash) {
        return repository.findByCodeHashAndDeletedAtIsNull(codeHash);
    }
}
