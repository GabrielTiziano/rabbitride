package com.gghiaroni.rabbitride.analysisservice.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, String> {
    Optional<Blacklist> findByCpf(String cpf);
}
