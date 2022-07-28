package foundationgames.nilchasm.internal;

import foundationgames.nilchasm.NilChasmPremain;
import foundationgames.nilchasm.api.NilChasm;
import nilloader.NilLoader;
import nilloader.api.ClassTransformer;
import nilloader.api.lib.asm.ClassReader;
import nilloader.api.lib.nanojson.JsonParser;
import nilloader.api.lib.nanojson.JsonParserException;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.ClassData;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.util.ClassLoaderClassInfoProvider;
import org.quiltmc.chasm.internal.transformer.ChasmLangTransformer;
import org.quiltmc.chasm.lang.api.ast.Node;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NilChasmTransformer implements ClassTransformer, NilChasm {
    public static final NilChasmTransformer INSTANCE = new NilChasmTransformer();

    private final ClassLoader classLoader = this.getClass().getClassLoader();

    private final ChasmProcessor processor = new ChasmProcessor(new ClassLoaderClassInfoProvider(null, this.classLoader));

    private boolean processed = false;

    private final Set<String> registered = new HashSet<>();
    private final Map<String, byte[]> transformed = new HashMap<>();

    @Override
    public byte[] transform(String className, byte[] originalData) {
        if (!this.processed && this.registered.contains(className)) {
            this.process();
        }

        if (this.transformed.containsKey(className)) {
            return this.transformed.get(className);
        }
        return originalData;
    }

    private void process() {
        var classes = this.processor.process();
        for (var data : classes) {
            var bytes = data.getClassBytes();
            var classReader = new ClassReader(bytes);

            this.transformed.put(classReader.getClassName(), bytes);
        }

        this.processed = true;
    }

    @Override
    public void loadFromModResources(Runnable modEntrypoint) {
        var mod = NilLoader.getActiveMod();
        var modClsLoader = modEntrypoint.getClass().getClassLoader();
        if (mod != null) {
            try (var chasmJson = modClsLoader.getResourceAsStream("chasm/"+mod+"/chasm.json")) {
                if (chasmJson != null) {
                    var json = JsonParser.object().from(chasmJson);

                    var classes = json.getArray("classes");
                    var transformers = json.getArray("transformers");
                    if (classes == null) {
                        NilChasmPremain.LOG.error("Mod '{}' is missing array 'classes' in chasm.json", mod);
                        return;
                    }
                    if (transformers == null) {
                        NilChasmPremain.LOG.error("Mod '{}' is missing array 'transformers' in chasm.json", mod);
                        return;
                    }

                    for (int i = 0; i < classes.size(); i++) {
                        var className = classes.getString(i);
                        if (className != null) {
                            this.addClass(className);
                        }
                    }
                    for (int i = 0; i < transformers.size(); i++) {
                        var transfId = transformers.getString(i);
                        if (transfId != null) {
                            try (var transformer = modClsLoader.getResourceAsStream("chasm/"+mod+"/"+transfId)) {
                                if (transformer != null) {
                                    this.addTransformer(transfId, new String(transformer.readAllBytes(), StandardCharsets.UTF_8));
                                } else {
                                    NilChasmPremain.LOG.error("Could not find transformer '{}' requested by mod '{}'", transfId, mod);
                                }
                            } catch (IOException e) {
                                NilChasmPremain.LOG.error("Error loading transformer '{}' from mod '{}': {}", transfId, mod, e.getMessage());
                            }
                        }
                    }
                } else {
                    NilChasmPremain.LOG.error("Missing chasm.json for mod '{}'", mod);
                }
            } catch (IOException | JsonParserException e) {
                NilChasmPremain.LOG.error("Error loading chasm.json for mod '{}': {}", mod, e.getMessage());
            }
        } else {
            NilChasmPremain.LOG.error("Tried to load CHASM too early/late");
        }
    }

    @Override
    public void addClass(String className) {
        try (var resource = classLoader.getResourceAsStream(className + ".class")) {
            if (resource != null) {
                this.processor.addClass(new ClassData(resource.readAllBytes()));
                this.registered.add(className);
            } else {
                NilChasmPremain.LOG.error("Could not find class '" + className + "'");
            }
        } catch (IOException e) {
            NilChasmPremain.LOG.error("Error reading class '" + className + "': " + e.getMessage());
        }
    }

    @Override
    public void addTransformer(String id, String expr) {
        this.addTransformer(new ChasmLangTransformer(id, Node.parse(expr)));
    }

    @Override
    public void addTransformer(Transformer transformer) {
        this.processor.addTransformer(transformer);
    }
}
