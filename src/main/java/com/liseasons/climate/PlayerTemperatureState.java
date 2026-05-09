package com.liseasons.climate;

public final class PlayerTemperatureState {
    private double bodyTemperature = Double.NaN;
    private double wetness;
    private long lastWetnessRecoveryTick;

    public double bodyTemperature() {
        return this.bodyTemperature;
    }

    public void setBodyTemperature(double bodyTemperature) {
        this.bodyTemperature = bodyTemperature;
    }

    public double wetness() {
        return this.wetness;
    }

    public void soak() {
        this.wetness = 1.0D;
    }

    public void recoverWetness(double amount, long currentTick, long intervalTicks) {
        if (currentTick - this.lastWetnessRecoveryTick < intervalTicks) {
            return;
        }
        this.lastWetnessRecoveryTick = currentTick;
        this.wetness = Math.max(0.0D, this.wetness - Math.max(0.0D, amount));
    }
}
