package arrowaccuracy;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ArrowAccuracy.MODID,
	 version = ArrowAccuracy.VERSION,
	 name = ArrowAccuracy.NAME,
	 clientSideOnly = true,
	 acceptableRemoteVersions = "*",
	 dependencies = "required-after:fermiumbooter;"
)
public class ArrowAccuracy {
	public static final String MODID = "arrowaccuracy";
	public static final String VERSION = "1.0.0";
	public static final String NAME = "ArrowAccuracy";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
}