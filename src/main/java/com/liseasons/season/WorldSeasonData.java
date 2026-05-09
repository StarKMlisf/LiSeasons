package com.liseasons.season;

public final class WorldSeasonData {
    private SeasonState state;
    private boolean automatic;

    public WorldSeasonData(SeasonState state, boolean automatic) {
        this.state = state;
        this.automatic = automatic;
    }

    public SeasonState state() {
        return this.state;
    }

    public void setState(SeasonState state) {
        this.state = state;
    }

    public boolean automatic() {
        return this.automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }
}
