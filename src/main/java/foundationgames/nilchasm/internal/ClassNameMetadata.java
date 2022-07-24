package foundationgames.nilchasm.internal;

import org.quiltmc.chasm.api.metadata.Metadata;

public record ClassNameMetadata(String name) implements Metadata {
    @Override
    public Metadata copy() {
        return new ClassNameMetadata(this.name);
    }
}
