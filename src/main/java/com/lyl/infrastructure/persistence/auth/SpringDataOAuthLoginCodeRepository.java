package com.lyl.infrastructure.persistence.auth;

import com.lyl.domain.auth.OAuthLoginCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOAuthLoginCodeRepository extends JpaRepository<OAuthLoginCode, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<OAuthLoginCode> findByCodeHashAndDeletedAtIsNull(String codeHash);
}
