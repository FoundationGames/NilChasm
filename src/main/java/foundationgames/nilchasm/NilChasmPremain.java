package foundationgames.nilchasm;

import foundationgames.nilchasm.api.NilChasm;
import foundationgames.nilchasm.chasmix.NilChasmix;
import foundationgames.nilchasm.chasmix.test.TestTargetClass;
import foundationgames.nilchasm.internal.NilChasmTransformer;
import io.github.foundationgames.chasmix.Chasmix;
import nilloader.api.ClassTransformer;
import nilloader.api.ModRemapper;
import nilloader.api.NilLogger;
import org.quiltmc.chasm.lang.internal.render.Renderer;

import java.io.IOException;

public class NilChasmPremain implements Runnable {
	public static final NilLogger LOG = NilLogger.get("NilChasm");

	// In a proper environment, chasmix should be trying to generate transformers for every class on the classpath.
	private static final String[] KNOWN_MIXIN_TARGETS = {"foundationgames.nilchasm.chasmix.test.TestTargetClass"};
	
	@Override
	public void run() {
		LOG.info("Initializing NilChasm");
		ModRemapper.setTargetMapping("obf");

		ClassTransformer.register(NilChasmTransformer.INSTANCE);

		NilChasm.instance().loadFromModResources(this);

		NilChasmix.addMixinConfig();
		setupChasmix();

		TestTargetClass.init();
	}

	private void setupChasmix() {
		Chasmix.DEBUG_PRINT_RENDERER = Renderer.builder().indentation(4).trailingCommas(false).prettyPrinting(true).build();
		var chasmix = NilChasmix.CHASMIX;

		for (var target : KNOWN_MIXIN_TARGETS) {
			try {
				var transformers = chasmix.generateChasmTransformers(target);
				LOG.info("Generated mixin transformers for target '"+target+"': "+transformers);
				transformers.ifPresentOrElse(
						l -> l.forEach(NilChasm.instance()::addTransformer),
						() -> LOG.warn("Error creating transformer for "+target));
			} catch (ClassNotFoundException | IOException e) {
				LOG.error(e.getMessage());
			}
		}
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
