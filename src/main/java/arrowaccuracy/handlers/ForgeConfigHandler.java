package arrowaccuracy.handlers;

import arrowaccuracy.ArrowAccuracy;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ArrowAccuracy.MODID)
public class ForgeConfigHandler {

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig CLIENT = new ClientConfig();

	public static class ClientConfig {

		@Config.Comment("Ticks an arrow stays stuck in an entity.")
		@Config.Name("Stuck Arrow Duration")
		@Config.RangeInt(min = 1, max = 72000)
		public int stuckArrowDuration = 600;

		@Config.Comment("Ticks at the end of the stuck duration the arrow spends fading out. 0 disables fading.")
		@Config.Name("Fade Out Duration")
		@Config.RangeInt(min = 0, max = 72000)
		public int fadeOutDuration = 20;

		@Config.Comment("Maximum amount of arrows rendered in a single entity. Oldest arrows are removed first.")
		@Config.Name("Max Arrows Per Entity")
		@Config.RangeInt(min = 1, max = 256)
		public int maxArrowsPerEntity = 16;

		@Config.Comment("Ticks a stuck arrow waits for the server to confirm the hit by removing the arrow entity. Raise if high ping causes stuck arrows to vanish.")
		@Config.Name("Hit Confirmation Timeout")
		@Config.RangeInt(min = 1, max = 200)
		public int hitConfirmationTimeout = 40;

		@Config.Comment("Hides the vanilla randomly placed arrows rendered in players.")
		@Config.Name("Hide Vanilla Player Arrows")
		public boolean hideVanillaArrows = true;
	}

	@Mod.EventBusSubscriber(modid = ArrowAccuracy.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(ArrowAccuracy.MODID)) {
				ConfigManager.sync(ArrowAccuracy.MODID, Config.Type.INSTANCE);
			}
		}
	}
}