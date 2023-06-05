package gg.generations.pokeutils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.javagl.jgltf.impl.v1.Material;
import gg.generations.pokeutils.reader.TextureReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelConfig {
    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantReference> defaultVariant;
    public Map<String, Map<String, VariantReference>> variants;

    public static abstract class MaterialReference {
        public final String texture;

        public MaterialReference(String texture) {
            this.texture = texture;
        }

        public static class SolidMaterialReference extends MaterialReference {
            public SolidMaterialReference(String texture) {
                super(texture);
            }
        }

        public static class TransparentMaterialReference extends MaterialReference {
            public final float alpha;

            public TransparentMaterialReference(String texture, float alpha) {
                super(texture);
                this.alpha = alpha;
            }
        }

        public static class MaterialReferenceTypeAdapter extends TypeAdapter<MaterialReference> {
            private static final String TYPE_SOLID = "solid";
            private static final String TYPE_TRANSPARENT = "transparent";

            @Override
            public void write(JsonWriter out, MaterialReference materialReference) throws IOException {
                out.beginObject();
                out.name("type").value(getTypeName(materialReference));
                out.name("texture").value(materialReference.texture);
                if (materialReference instanceof TransparentMaterialReference) {
                    TransparentMaterialReference transparentMaterialReference = (TransparentMaterialReference) materialReference;
                    out.name("alpha").value(transparentMaterialReference.alpha);
                }
                out.endObject();
            }

            @Override
            public MaterialReference read(JsonReader in) throws IOException {
                String type = null;
                String texture = null;
                float alpha = 1.0f; // default alpha value for SolidMaterialReference

                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    if (name.equals("type")) {
                        type = in.nextString();
                    } else if (name.equals("texture")) {
                        texture = in.nextString();
                    } else if (name.equals("alpha")) {
                        alpha = (float) in.nextDouble();
                    }
                }
                in.endObject();

                if (type != null) {
                    if (type.equals(TYPE_SOLID)) {
                        return new SolidMaterialReference(texture);
                    } else if (type.equals(TYPE_TRANSPARENT)) {
                        return new TransparentMaterialReference(texture, alpha);
                    }
                }
                throw new IllegalArgumentException("Invalid or missing type value");
            }

            private String getTypeName(MaterialReference materialReference) {
                if (materialReference instanceof SolidMaterialReference) {
                    return TYPE_SOLID;
                } else if (materialReference instanceof TransparentMaterialReference) {
                    return TYPE_TRANSPARENT;
                }
                throw new IllegalArgumentException("Invalid MaterialReference type");
            }
        }
    }
}
