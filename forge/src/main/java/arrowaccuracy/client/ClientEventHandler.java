package arrowaccuracy.client;

import arrowaccuracy.config.ArrowAccuracyConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;

public class ClientEventHandler {

	public static void init() {
		ModLoadingContext.get().registerExtensionPoint(
				ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> AutoConfig.getConfigScreen(ArrowAccuracyConfig.class, parent).get()));

		MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onClientTick);
	}

	private static void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END) StuckArrowHandler.onClientTickEnd();
	}
}