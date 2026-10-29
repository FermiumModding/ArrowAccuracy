package arrowaccuracy;

import arrowaccuracy.client.ClientEventHandler;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;

@Mod(Constants.MOD_ID)
public class ArrowAccuracyForge {

	public ArrowAccuracyForge() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));

		if(FMLEnvironment.dist.isClient()) {
			ArrowAccuracyCommon.init();
			ClientEventHandler.init();
		}
	}
}