package com.example.cognisync.model;

public class MLPredictRequest {

    private int maas;
    private int panas_pos;
    private int panas_neg;
    private int dass;
    private int erq;
    private int phlms;

    public MLPredictRequest(
            int maas,
            int panas_pos,
            int panas_neg,
            int dass,
            int erq,
            int phlms
    ) {
        this.maas = maas;
        this.panas_pos = panas_pos;
        this.panas_neg = panas_neg;
        this.dass = dass;
        this.erq = erq;
        this.phlms = phlms;
    }

    // Getters (Retrofit/Gson needs them)
    public int getMaas() { return maas; }
    public int getPanas_pos() { return panas_pos; }
    public int getPanas_neg() { return panas_neg; }
    public int getDass() { return dass; }
    public int getErq() { return erq; }
    public int getPhlms() { return phlms; }
}
