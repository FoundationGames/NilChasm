package foundationgames.nilchasm.api;

import foundationgames.nilchasm.internal.NilChasmTransformer;
import org.quiltmc.chasm.api.Transformer;

public interface NilChasm {
    /**
     * @return An instance of the NilChasm API.
     */
    static NilChasm instance() {
        return NilChasmTransformer.INSTANCE;
    }

    /**
     * Load CHASM metadata and transformers from your mod resources. <br/>
     * Only call during mod entrypoint.
     *
     * @param mod an instance of the mod's entrypoint (if running in the entrypoint class, {@code this})
     */
    void loadFromModResources(Runnable mod);

    /**
     * Manually add a class by name
     */
    void addClass(String className);

    /**
     * Manually add a transformer from a char stream
     */
    void addTransformer(String id, String expr);

    /**
     * Manually add a custom transformer
     */
    void addTransformer(Transformer transformer);
}
