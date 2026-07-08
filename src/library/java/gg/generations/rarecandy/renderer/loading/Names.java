package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.IMaterialReference;
import gg.generations.rarecandy.pokeutils.IModelConfig;
import gg.generations.rarecandy.pokeutils.IVariantDetails;
import gg.generations.rarecandy.pokeutils.IVariantParent;

import java.util.*;
import java.util.stream.Stream;

public record Names(List<String> meshes, List<String> variants, List<String> images, List<String> materials) {
    public Names() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public static Names generateNames(IModelConfig config) {
        var names = new Names();
        var aliases = config.aliases() != null ? config.aliases() : Collections.<String, List<String>>emptyMap();

        collectVariantNames(names, config.defaultVariant(), config.variants(), aliases, config.meshesToRenderFirst());
        collectMaterialImageNames(names, config.materials());

        return names;
    }

    private static void collectVariantNames(
            Names names,
            Map<String, ? extends IVariantDetails> defaultVariant,
            Map<String, ? extends IVariantParent> variants,
            Map<String, List<String>> aliases,
            List<String> meshesToRenderFirst) {
        if (defaultVariant != null) {
            defaultVariant.forEach((variantName, details) -> {
                if (details == null) return;
                addMeshNames(names, variantName, aliases, meshesToRenderFirst != null && meshesToRenderFirst.contains(variantName));
                checkIfAlreadyIn(names.materials(), details.material());
            });
        }

        if (variants != null) {
            variants.forEach((variantName, parent) -> {
                checkIfAlreadyIn(names.variants(), variantName);

                if (parent == null || parent.details() == null) return;
                parent.details().forEach((meshName, details) -> {
                    if (details == null) return;
                    addMeshNames(names, meshName, aliases, false);
                    checkIfAlreadyIn(names.materials(), details.material());
                });
            });
        }
    }

    private static void collectMaterialImageNames(Names names, Map<String, ? extends IMaterialReference> materials) {
        if (materials == null) return;

        var cache = new HashMap<String, MaterialImageNames>();

        for (String materialName : names.materials()) {
            addMaterialImages(names, materialImageNames(materialName, materials, cache, new HashSet<>()));
        }
    }

    private static void addMaterialImages(Names names, MaterialImageNames images) {
        Stream.of(images.diffuse(), images.layer(), images.emission(), images.mask())
                .forEach(image -> checkIfAlreadyIn(names.images(), image));
    }

    private static void checkIfAlreadyIn(List<String> list, String entry) {
        checkIfAlreadyIn(list, entry, false);
    }

    private static void checkIfAlreadyIn(List<String> list, String entry, boolean addFirst) {
        if (entry == null || list.contains(entry)) return;
        if (addFirst) list.addFirst(entry); else list.add(entry);
    }

    private static MaterialImageNames materialImageNames(
            String materialName,
            Map<String, ? extends IMaterialReference> materials,
            Map<String, MaterialImageNames> cache,
            Set<String> resolving) {
        var cached = cache.get(materialName);
        if (cached != null) return cached;

        var reference = materials.get(materialName);
        if (reference == null) return MaterialImageNames.EMPTY;

        var images = reference.images();
        var imageNames = images != null
                ? new MaterialImageNames(images.diffuse(), images.layer(), images.mask(), images.emission())
                : MaterialImageNames.EMPTY;

        var parent = reference.parent();
        if (parent != null && resolving.add(materialName)) {
            imageNames = imageNames.fill(materialImageNames(parent, materials, cache, resolving));
            resolving.remove(materialName);
        }

        cache.put(materialName, imageNames);
        return imageNames;
    }

    private static void addMeshNames(Names names, String mesh, Map<String, List<String>> aliases, boolean addFirst) {
        var aliasList = aliases.get(mesh);
        if (aliasList != null) {
            for (String alias : aliasList) checkIfAlreadyIn(names.meshes(), alias, addFirst);
        } else {
            checkIfAlreadyIn(names.meshes(), mesh, addFirst);
        }
    }

    record MaterialImageNames(String diffuse, String layer, String mask, String emission) {
        private static final MaterialImageNames EMPTY = new MaterialImageNames(null, null, null, null);

        private static String coalesce(String a, String b) {
            return a != null ? a : b;
        }

        private MaterialImageNames fill(MaterialImageNames parent) {
            return new MaterialImageNames(
                    coalesce(parent.diffuse, diffuse),
                    coalesce(parent.layer, layer),
                    coalesce(parent.mask, mask),
                    coalesce(parent.emission, emission)
            );
        }
    }
}