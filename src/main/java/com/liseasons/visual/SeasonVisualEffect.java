package com.liseasons.visual;

import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.SeasonState;
import org.bukkit.entity.Player;

public interface SeasonVisualEffect {
    void render(Player player, SeasonState state, TemperatureSnapshot snapshot);
}