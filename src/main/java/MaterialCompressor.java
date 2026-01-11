import com.google.gson.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialCompressor {

    private static final Set<String> EFFECTS = Set.of("galaxy", "sketch", "shadow", "pastel", "vintage");

    // Safeguard: match "BodyA/BodyB/BodyC" only when "body" is not part of a larger alnum token,
    // and the letter is followed by a digit, delimiter, boundary, or end. Prevents false hits like "body_back".
    private static final Pattern BODY_ABC_PATTERN =
            Pattern.compile("(?i)body.*([abc])");

    public static void main(String[] args) throws Exception {
        String input = readClipboard();
        JsonElement parsed = JsonParser.parseString(input);
        if (!parsed.isJsonObject()) {
            throw new IllegalArgumentException("Clipboard JSON root must be an object.");
        }

        JsonObject root = parsed.getAsJsonObject();
        JsonObject materials = root.getAsJsonObject("materials");

        Map<String, String> remap = new LinkedHashMap<>();

        if (materials != null) {
            normalizeMaterialNames(materials, remap);
            processMaterialObjects(materials);

            collapseByDiffuseWhereSafe(materials, remap);
            collapseEyesByEquivalence(materials, remap);
        }

        remap = resolveRemap(remap);

        applyRemapToDefaultVariant(root, remap);
        applyRemapToAllVariants(root, remap);

        if (materials != null) {
            removeMaterialsWithEffectWordsInName(materials);
        }

        removeOffsetsIfEmpty(root);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        writeClipboard(gson.toJson(root));
    }

    /* ===================== CLIPBOARD ===================== */

    private static String readClipboard() throws UnsupportedFlavorException, IOException {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        return (String) clipboard.getData(DataFlavor.stringFlavor);
    }

    private static void writeClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    /* ===================== OFFSETS ===================== */

    private static void removeOffsetsIfEmpty(JsonObject root) {
        if (root == null) return;
        JsonElement e = root.get("offsets");
        if (e == null) return;
        if (!e.isJsonObject()) return;
        JsonObject offsets = e.getAsJsonObject();
        if (offsets.entrySet().isEmpty()) root.remove("offsets");
    }

    /* ===================== REMAP RESOLUTION ===================== */

    private static Map<String, String> resolveRemap(Map<String, String> remap) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String k : remap.keySet()) {
            out.put(k, resolveOne(remap, k));
        }
        return out;
    }

    private static String resolveOne(Map<String, String> remap, String k) {
        String cur = k;
        Set<String> seen = new HashSet<>();
        while (remap.containsKey(cur)) {
            if (!seen.add(cur)) break;
            cur = remap.get(cur);
        }
        return cur;
    }

    /* ===================== MATERIAL NAME NORMALIZATION ===================== */

    private static void normalizeMaterialNames(JsonObject materials, Map<String, String> remap) {
        List<String> keys = new ArrayList<>(materials.keySet());
        for (String key : keys) {
            if (!materials.has(key)) continue;

            String newKey = normalizeBodyABCInName(key);
            if (newKey.equals(key)) continue;

            JsonObject mat = materials.getAsJsonObject(key);

            if (!materials.has(newKey)) {
                materials.add(newKey, mat);
                materials.remove(key);
                remap.put(key, newKey);
            } else {
                materials.remove(key);
                remap.put(key, newKey);
            }
        }
    }

    private static String normalizeBodyABCInName(String name) {

        Matcher m = BODY_ABC_PATTERN.matcher(name);
        if (!m.find()) return name;

        String letter = m.group(1).toLowerCase(Locale.ROOT);

        // Safeguard: only accept a/b/c explicitly; otherwise no-op.
        if (!("a".equals(letter) || "b".equals(letter) || "c".equals(letter))) return name;

        int start = m.start();
        int end = m.end();

        name = name.substring(0, start) + "body_" + letter;

        return name;
    }

    /* ===================== MATERIAL OBJECT PROCESSING ===================== */

    private static void processMaterialObjects(JsonObject materials) {
        for (String key : new ArrayList<>(materials.keySet())) {
            JsonObject mat = materials.getAsJsonObject(key);
            if (mat == null) continue;

            if ("None".equals(optString(mat, "cull"))) mat.remove("cull");
            if ("None".equals(optString(mat, "blend"))) mat.remove("blend");

            if ("solid".equals(optString(mat, "shader"))) {
                mat.remove("shader");
            } else if ("layered".equals(optString(mat, "shader"))) {
                processLayeredMaterial(mat);
            }

            if (mat.has("values")) {
                JsonObject values = mat.getAsJsonObject("values");
                if (values != null) {
                    reorderValues(values);
                    if (values.entrySet().isEmpty()) mat.remove("values");
                } else {
                    mat.remove("values");
                }
            }
        }
    }

    private static void processLayeredMaterial(JsonObject mat) {
        JsonObject values = mat.has("values") && mat.get("values").isJsonObject()
                ? mat.getAsJsonObject("values")
                : null;

        if (values != null) {
            values.remove("useLight");
            values.remove("usedLight");

            for (Map.Entry<String, JsonElement> e : new ArrayList<>(values.entrySet())) {
                JsonElement v = e.getValue();
                if (v != null && v.isJsonArray()) {
                    JsonArray a = v.getAsJsonArray();
                    if (a.size() == 3 && isDecimalTriplet(a)) {
                        values.addProperty(e.getKey(), rgbToHex(a));
                    }
                }
            }

            if (values.entrySet().isEmpty()) {
                mat.remove("values");
                mat.remove("shader");
            }
        }
    }

    private static void reorderValues(JsonObject values) {
        List<Map.Entry<String, JsonElement>> base = new ArrayList<>();
        List<Map.Entry<String, JsonElement>> emiColor = new ArrayList<>();
        List<Map.Entry<String, JsonElement>> emiIntensity = new ArrayList<>();
        List<Map.Entry<String, JsonElement>> other = new ArrayList<>();

        for (Map.Entry<String, JsonElement> e : values.entrySet()) {
            String k = e.getKey();
            if (k.startsWith("baseColor")) {
                base.add(e);
            } else if (k.startsWith("emiColor")) {
                emiColor.add(e);
            } else if (k.startsWith("emiIntensity")) {
                emiIntensity.add(e);
            } else {
                other.add(e);
            }
        }

        Comparator<Map.Entry<String, JsonElement>> descNumericSuffix = (a, b) -> {
            int na = trailingInt(a.getKey());
            int nb = trailingInt(b.getKey());
            return Integer.compare(nb, na);
        };

        base.sort(descNumericSuffix);
        emiColor.sort(descNumericSuffix);
        emiIntensity.sort(descNumericSuffix);

        JsonObject out = new JsonObject();
        for (Map.Entry<String, JsonElement> e : base) out.add(e.getKey(), e.getValue());
        for (Map.Entry<String, JsonElement> e : emiColor) out.add(e.getKey(), e.getValue());
        for (Map.Entry<String, JsonElement> e : emiIntensity) out.add(e.getKey(), e.getValue());
        for (Map.Entry<String, JsonElement> e : other) out.add(e.getKey(), e.getValue());

        values.entrySet().clear();
        for (Map.Entry<String, JsonElement> e : out.entrySet()) {
            values.add(e.getKey(), e.getValue());
        }
    }

    private static int trailingInt(String s) {
        int i = s.length() - 1;
        while (i >= 0 && Character.isDigit(s.charAt(i))) i--;
        if (i == s.length() - 1) return -1;
        try {
            return Integer.parseInt(s.substring(i + 1));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /* ===================== COLLAPSE BY DIFFUSE ===================== */

    private static void collapseByDiffuseWhereSafe(JsonObject materials, Map<String, String> remap) {
        Map<String, String> firstByDiffuse = new LinkedHashMap<>();

        for (String key : new ArrayList<>(materials.keySet())) {
            if (!materials.has(key)) continue;

            JsonObject mat = materials.getAsJsonObject(key);
            if (mat == null || !mat.has("images") || !mat.get("images").isJsonObject()) continue;

            JsonObject images = mat.getAsJsonObject("images");
            if (!images.has("diffuse")) continue;

            if (images.entrySet().size() > 1) continue;

            String diffuse = images.get("diffuse").getAsString();
            String first = firstByDiffuse.get(diffuse);
            if (first == null) {
                firstByDiffuse.put(diffuse, key);
            } else {
                remap.put(key, first);
                materials.remove(key);
            }
        }
    }

    /* ===================== EYES COLLAPSE ===================== */

    private static void collapseEyesByEquivalence(JsonObject materials, Map<String, String> remap) {
        Map<String, String> firstKeyBySignature = new LinkedHashMap<>();

        for (String key : new ArrayList<>(materials.keySet())) {
            if (!materials.has(key)) continue;
            if (!containsEyeIgnoreCase(key)) continue;

            JsonObject mat = materials.getAsJsonObject(key);
            if (mat == null) continue;

            String signature = mat.toString();
            String first = firstKeyBySignature.get(signature);
            if (first == null) {
                firstKeyBySignature.put(signature, key);
            } else {
                remap.put(key, first);
                materials.remove(key);
            }
        }

        for (Map.Entry<String, String> e : firstKeyBySignature.entrySet()) {
            String signature = e.getKey();
            String firstKey = e.getValue();

            if (!materials.has(firstKey)) continue;

            boolean shiny = firstKey.startsWith("shiny_");
            String canonical = shiny ? "shiny_eyes" : "eyes";

            if (firstKey.equals(canonical)) continue;

            if (!materials.has(canonical)) {
                JsonObject mat = materials.getAsJsonObject(firstKey);
                materials.add(canonical, mat);
                materials.remove(firstKey);
                remap.put(firstKey, canonical);
            } else {
                JsonObject existing = materials.getAsJsonObject(canonical);
                if (existing != null && existing.toString().equals(signature)) {
                    materials.remove(firstKey);
                    remap.put(firstKey, canonical);
                }
            }
        }
    }

    private static boolean containsEyeIgnoreCase(String s) {
        return s.toLowerCase(Locale.ROOT).contains("eye");
    }

    /* ===================== REMOVE EFFECT-NAMED MATERIALS ===================== */

    private static void removeMaterialsWithEffectWordsInName(JsonObject materials) {
        for (String key : new ArrayList<>(materials.keySet())) {
            String lower = key.toLowerCase(Locale.ROOT);
            boolean hasEffectWord = false;
            for (String eff : EFFECTS) {
                if (lower.contains(eff)) {
                    hasEffectWord = true;
                    break;
                }
            }
            if (hasEffectWord) {
                materials.remove(key);
            }
        }
    }

    /* ===================== APPLY REMAP ===================== */

    private static void applyRemapToDefaultVariant(JsonObject root, Map<String, String> remap) {
        JsonObject def = root.getAsJsonObject("defaultVariant");
        if (def == null) return;

        for (Map.Entry<String, JsonElement> e : def.entrySet()) {
            JsonElement v = e.getValue();
            if (v == null || !v.isJsonObject()) continue;

            JsonObject obj = v.getAsJsonObject();
            if (!obj.has("material")) continue;

            String mat = obj.get("material").getAsString();
            String resolved = remap.get(mat);
            if (resolved != null) obj.addProperty("material", resolved);
        }
    }

    private static void applyRemapToAllVariants(JsonObject root, Map<String, String> remap) {
        JsonObject variants = root.getAsJsonObject("variants");
        if (variants == null) return;
        for (Map.Entry<String, JsonElement> ve : variants.entrySet()) {
            String variantName = ve.getKey();
            JsonElement vObjEl = ve.getValue();
            if (vObjEl == null || !vObjEl.isJsonObject()) continue;
            JsonObject variantObj = vObjEl.getAsJsonObject();

            String effect = effectFromVariantName(variantName);
            boolean isShiny = variantName.toLowerCase(Locale.ROOT).contains("shiny");

            if (isShiny) {
                variantObj.addProperty("parent", "shiny");
            }

            for (Map.Entry<String, JsonElement> me : variantObj.entrySet()) {
                JsonElement slotEl = me.getValue();
                if (slotEl == null || !slotEl.isJsonObject()) continue;
                JsonObject slot = slotEl.getAsJsonObject();

                if (slot.has("material")) {
                    String mat = slot.get("material").getAsString();
                    String resolved = remap.get(mat);
                    if (resolved != null) slot.addProperty("material", resolved);
                }

                if (effect != null) {
                    slot.remove("material");
                    slot.addProperty("effect", effect);
                }
            }
        }
    }

    private static String effectFromVariantName(String variantName) {
        String n = variantName.toLowerCase(Locale.ROOT);
        for (String eff : EFFECTS) {
            if (n.contains(eff)) return eff;
        }
        return null;
    }

    /* ===================== COLOR CONVERSION ===================== */

    private static boolean isDecimalTriplet(JsonArray a) {
        for (int i = 0; i < 3; i++) {
            JsonElement e = a.get(i);
            if (e == null || !e.isJsonPrimitive()) return false;
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (!p.isNumber()) return false;
        }
        return true;
    }

    private static String rgbToHex(JsonArray a) {
        int r = to255(a.get(0).getAsDouble());
        int g = to255(a.get(1).getAsDouble());
        int b = to255(a.get(2).getAsDouble());
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private static int to255(double v) {
        if (Double.isNaN(v)) v = 0.0;
        if (v < 0.0) v = 0.0;
        if (v > 1.0) v = 1.0;
        long rounded = Math.round(v * 255.0);
        if (rounded < 0L) rounded = 0L;
        if (rounded > 255L) rounded = 255L;
        return (int) rounded;
    }

    /* ===================== UTILS ===================== */

    private static String optString(JsonObject obj, String key) {
        if (obj == null) return null;
        JsonElement e = obj.get(key);
        if (e == null || !e.isJsonPrimitive()) return null;
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (!p.isString()) return null;
        return p.getAsString();
    }
}
