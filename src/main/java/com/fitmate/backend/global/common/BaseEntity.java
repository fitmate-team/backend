package com.fitmate.backend.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false, precision = 6) // 생성 시간은 update 방지
    private LocalDateTime createdAt;

    @LastModifiedDate // 데이터 수정될 떄 마다 자동으로 시간 갱신
    @Column(precision = 6)
    private LocalDateTime updatedAt;
}