package foundationgames.nilchasm.internal;

import foundationgames.nilchasm.NilChasmPremain;
import foundationgames.nilchasm.api.NilChasm;
import nilloader.NilLoader;
import nilloader.api.ClassTransformer;
import nilloader.api.lib.nanojson.JsonObject;
import nilloader.api.lib.nanojson.JsonParser;
import nilloader.api.lib.nanojson.JsonParserException;
import nilloader.api.lib.nanojson.JsonReader;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.ClassData;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.util.ClassLoaderClassInfoProvider;
import org.quiltmc.chasm.internal.transformer.ChasmLangTransformer;
import org.quiltmc.chasm.lang.Evaluator;
import org.quiltmc.chasm.lang.op.Expression;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class NilChasmTransformer implements ClassTransformer, NilChasm {
    public static final NilChasmTransformer INSTANCE = new NilChasmTransformer();

    private final ClassLoader classLoader = this.getClass().getClassLoader();

    private final Evaluator evaluator = new Evaluator();
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
            var className = data.getMetadataProvider().get(ClassNameMetadata.class).name();

            this.transformed.put(className, data.getClassBytes());
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
                        var transfName = transformers.getString(i);
                        if (transfName != null) {
                            try (var transformer = modClsLoader.getResourceAsStream("chasm/"+mod+"/"+transfName)) {
                                if (transformer != null) {
                                    this.addTransformer(CharStreams.fromStream(transformer));
                                } else {
                                    NilChasmPremain.LOG.error("Could not find transformer '{}' requested by mod '{}'", transfName, mod);
                                }
                            } catch (IOException e) {
                                NilChasmPremain.LOG.error("Error loading transformer '{}' from mod '{}': {}", transfName, mod, e.getMessage());
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
                var metadata = new MetadataProvider();
                metadata.put(ClassNameMetadata.class, new ClassNameMetadata(className));

                this.processor.addClass(new ClassData(resource.readAllBytes(), metadata));
                this.registered.add(className);
            } else {
                NilChasmPremain.LOG.error("Could not find class '" + className + "'");
            }
        } catch (IOException e) {
            NilChasmPremain.LOG.error("Error reading class '" + className + "': " + e.getMessage());
        }
    }

    @Override
    public void addTransformer(CharStream transformerExpression) {
        this.addTransformer(eval -> new ChasmLangTransformer(eval, Expression.parse(transformerExpression)));
    }

    @Override
    public void addTransformer(Function<Evaluator, Transformer> transformerProvider) {
        this.processor.addTransformer(transformerProvider.apply(this.evaluator));
    }
}
