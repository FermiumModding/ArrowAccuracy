package arrowaccuracy.config;

import arrowaccuracy.Constants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.util.Mth;

@Config(name = Constants.MOD_ID)
public class ArrowAccuracyConfig implements ConfigData {

	@ConfigEntry.Gui.Tooltip
	public int stuckArrowDuration = 600;

	@ConfigEntry.Gui.Tooltip
	public int fadeOutDuration = 20;

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 1, max = 256)
	public int maxArrowsPerEntity = 16;

	@ConfigEntry.Gui.Tooltip(count = 2)
	@ConfigEntry.BoundedDiscrete(min = 1, max = 200)
	public int hitConfirmationTimeout = 40;

	@ConfigEntry.Gui.Tooltip
	public boolean hideVanillaArrows = true;

	@Override
	public void validatePostLoad() {
		this.stuckArrowDuration = Mth.clamp(this.stuckArrowDuration, 1, 72000);
		this.fadeOutDuration = Mth.clamp(this.fadeOutDuration, 0, 72000);
		this.maxArrowsPerEntity = Mth.clamp(this.maxArrowsPerEntity, 1, 256);
		this.hitConfirmationTimeout = Mth.clamp(this.hitConfirmationTimeout, 1, 200);
	}
}