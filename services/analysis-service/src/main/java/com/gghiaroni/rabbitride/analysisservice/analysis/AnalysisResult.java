package com.gghiaroni.rabbitride.analysisservice.analysis;

public record AnalysisResult(boolean aprovado, String motivo) {
    public static AnalysisResult aprovar(){
        return new AnalysisResult(true, null);
    }

    public static AnalysisResult rejeitar(String motivo) {
        return new AnalysisResult(false, motivo);
    }
}
