package arrowaccuracy.client;

import arrowaccuracy.config.ArrowAccuracyConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public class ClientEventHandler {

	public static void init(ModContainer container) {
		container.registerExtensionPoint(
				IConfigScreenFactory.class,
				(modContainer, parent) -> AutoConfig.getConfigScreen(ArrowAccuracyConfig.class, parent).get());

		NeoForge.EVENT_BUS.addListener(ClientEventHandler::onClientTick);
	}

	private static void onClientTick(ClientTickEvent.Post event) {
		StuckArrowHandler.onClientTickEnd();
	}
}
