package arrowaccuracy;

import arrowaccuracy.client.ClientEventHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class ArrowAccuracyNeoForge {

	public ArrowAccuracyNeoForge(ModContainer container) {
		ArrowAccuracyCommon.init();
		ClientEventHandler.init(container);
	}
}
