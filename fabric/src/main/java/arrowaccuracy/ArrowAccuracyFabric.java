package arrowaccuracy;

import arrowaccuracy.client.StuckArrowHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class ArrowAccuracyFabric implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ArrowAccuracyCommon.init();

		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> StuckArrowHandler.onClientTickEnd());
	}
}