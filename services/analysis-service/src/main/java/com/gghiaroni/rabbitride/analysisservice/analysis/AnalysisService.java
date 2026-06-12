package com.gghiaroni.rabbitride.analysisservice.analysis;

import com.gghiaroni.rabbitride.commons.events.RentalRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class AnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);
    private static final int LATENCIA_SIMULADA_SEGUNDOS = 2;

    private final BlacklistRepository blacklistRepository;

    public AnalysisService(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    @Transactional(readOnly = true)
    public AnalysisResult analisar(RentalRequestedEvent event) {
        log.info("Iniciando análise: rentalId={}, cpf={}",
            event.rentalId(), event.userCpf());

        simularLatencia();

        return blacklistRepository.findByCpf(event.userCpf())
            .map(entrada -> {
                log.info("Análise REJEITADA: rentalId={}, motivo={}",
                    event.rentalId(), entrada.getMotivo());
                return AnalysisResult.rejeitar(entrada.getMotivo());
            })
            .orElseGet(() -> {
                log.info("Análise APROVADA: rentalId={}", event.rentalId());
                return AnalysisResult.aprovar();
            });
    }

    private void simularLatencia() {
        try {
            TimeUnit.SECONDS.sleep(LATENCIA_SIMULADA_SEGUNDOS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
