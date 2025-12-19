package com.ttl.userportal.repository;

import com.ttl.userportal.entity.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken, Long> {
    
    Optional<AccountActivationToken> findByToken(String token);
    
    Optional<AccountActivationToken> findByTokenAndIsUsedFalseAndIsExpiredFalse(String token);
    
    List<AccountActivationToken> findByUserIdAndIsUsedFalseAndIsExpiredFalse(Long userId);
    
    Optional<AccountActivationToken> findByEmailAndIsUsedFalseAndIsExpiredFalse(String email);
    
    /**
     * Mark all expired tokens
     */
    @Modifying
    @Query("UPDATE AccountActivationToken t SET t.isExpired = true WHERE t.expiresAt < :now AND t.isExpired = false")
    int markExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Invalidate all existing tokens for a user
     */
    @Modifying
    @Query("UPDATE AccountActivationToken t SET t.isExpired = true WHERE t.userId = :userId AND t.isUsed = false AND t.isExpired = false")
    int invalidateTokensForUser(@Param("userId") Long userId);
}

