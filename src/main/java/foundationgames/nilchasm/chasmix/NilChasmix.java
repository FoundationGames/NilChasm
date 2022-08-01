package foundationgames.nilchasm.chasmix;

import io.github.foundationgames.chasmix.Chasmix;
import nilloader.NilLoader;
import org.spongepowered.asm.launch.MixinBootstrap;

public class NilChasmix {
    public static final Chasmix CHASMIX;

    public static void addMixinConfig() {
        Chasmix.addMixinConfig(NilLoader.getActiveMod() + ".mixins.json");
    }

    static {
        Chasmix.provideServices();
        MixinBootstrap.init();

        CHASMIX = new Chasmix();
    }
}
