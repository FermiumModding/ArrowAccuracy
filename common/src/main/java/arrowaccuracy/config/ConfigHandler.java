package arrowaccuracy.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.world.InteractionResult;

public abstract class ConfigHandler {

	public static ArrowAccuracyConfig CONFIG;

	public static void init() {
		ConfigHolder<ArrowAccuracyConfig> holder = AutoConfig.register(ArrowAccuracyConfig.class, Toml4jConfigSerializer::new);
		CONFIG = holder.getConfig();
		holder.registerSaveListener((configHolder, config) -> {
			config.validatePostLoad();
			CONFIG = config;
			return InteractionResult.SUCCESS;
		});
		holder.registerLoadListener((configHolder, config) -> {
			CONFIG = config;
			return InteractionResult.SUCCESS;
		});
	}
}