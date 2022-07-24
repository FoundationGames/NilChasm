package foundationgames.nilchasm;

import foundationgames.nilchasm.api.NilChasm;
import foundationgames.nilchasm.internal.NilChasmTransformer;
import nilloader.api.ClassTransformer;
import nilloader.api.ModRemapper;
import nilloader.api.NilLogger;

public class NilChasmPremain implements Runnable {
	public static final NilLogger LOG = NilLogger.get("NilChasm");
	
	@Override
	public void run() {
		LOG.info("Initializing NilChasm");
		ModRemapper.setTargetMapping("obf");

		ClassTransformer.register(NilChasmTransformer.INSTANCE);

		NilChasm.instance().loadFromModResources(this);
	}

	/*
		A call to this method will be added to
		net.minecraft.client.main.Main.main()
		by a transformer included in this mod
	*/
	public static void printSuccessMessage() {
		LOG.info("~~~~~~~~ Chasm Transformer Test Success ~~~~~~~~");
	}
}
