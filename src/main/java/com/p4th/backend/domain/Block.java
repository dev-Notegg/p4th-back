package com.p4th.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "block")
@Getter
@Setter
public class Block {
    @Id
    private String blockId;
    private String userId;
    private String targetPostId;
    private String targetUserId;
    private LocalDateTime blockDate;
    private String blockReason;
}
