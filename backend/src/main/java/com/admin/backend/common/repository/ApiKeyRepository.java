package com.admin.backend.common.repository;

import com.admin.backend.common.domain.ApiKey;
import com.admin.backend.common.enumeration.AiProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {

    @Query("""
        SELECT ak FROM ApiKey ak
        JOIN FETCH ak.user u
        JOIN FETCH u.team
        WHERE ak.keyValue = :keyValue AND ak.provider = :provider
        """)
    Optional<ApiKey> findByKeyValueAndProvider(@Param("keyValue") String keyValue,
                                                @Param("provider") AiProvider provider);

    boolean existsByUserIdAndProvider(String userId, AiProvider provider);
}
