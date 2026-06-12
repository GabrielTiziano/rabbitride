package com.gghiaroni.rabbitride.analysisservice.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "blacklist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blacklist {

    @Id
    @Column(length = 11)
    private String cpf;

    @Column(nullable = false, length = 500)
    private String motivo;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}
