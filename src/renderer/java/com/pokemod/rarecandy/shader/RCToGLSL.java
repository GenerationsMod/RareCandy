package com.pokemod.rarecandy.shader;

import java.util.*;

/**
 * Converts Rare Candy Shader's (rc) to GL Shading Language (glsl)
 */
public class RCToGLSL {

    public static void main(String[] args) {
        convert("""
                vec4 vertMain(
                    in vec4 InPosition,
                    in vec2 InUV,
                    out vec2 OutUV
                ) {
                    return InPosition;
                    OutUV = InUV;
                }
                                
                vec4 fragMain(
                    in vec2 UV,
                    uniform sampler2D diffuse
                ) {
                    return texture(diffuse, UV);
                }
                                
                """);
    }

    private static void convert(String rcShader) {
        var processedShader = rcShader
                .replaceAll("//.+\n", "")
                .replace("\n", "")
                .replace("\t", "");

        var types = List.of(
                "vec4",
                "vec3",
                "vec2",
                "float",
                "sampler2D",
                "samplerCube"
        );
        var keywords = new ArrayList<>(List.of(
                "in",
                "out",
                "uniform"
        ));
        keywords.addAll(types);

        int currentIndex = 0;
        RcMethod currentMethod = null;

        while (true) {
            var currentString = processedShader.substring(currentIndex);

            var methodType = findNextKeywords(currentString, keywords).get(0);
            currentIndex += methodType.length();

            // Error Check
            if (!currentString.substring(currentIndex).startsWith(" "))
                throw new RuntimeException("Method missing name. Only found type \"" + methodType + "\"");

            // Get method name and update length.
            currentString = currentString.substring(currentIndex + 1);
            var methodName = currentString.substring(0, currentString.indexOf('('));
            currentString = currentString.substring(methodName.length());
            var method = new RcMethod(RcMethod.getType(methodName));

            // Error Checks
            if (!currentString.startsWith("("))
                throw new RuntimeException("Missing '(' for parameters in method " + methodName);
            else if (!currentString.contains(")"))
                throw new RuntimeException("Missing ')' for parameters in method " + methodName);

            // Separate parameters string and handle away from the main string.
            var parameters = currentString.substring(1, currentString.indexOf(')'));
            var split = parameters.split(",");
            for (var s : split) {
                var safeString = s.replaceAll("[ \\t]+", " ");
                if (safeString.startsWith(" ")) safeString = safeString.substring(1);
                var originTypeName = Arrays.asList(safeString.split("[ \\t]+"));
                method.params.add(new RcParameter(originTypeName.get(0), originTypeName.get(1), originTypeName.get(2)));
            }

            // By this point the index is most likely malformed. Make sure it is fixed.
            currentIndex = processedShader.indexOf(currentString) + parameters.length() + 2;
            currentString = processedShader.substring(currentIndex);


        }

        System.out.println("ok");
    }

    /**
     * I'm awful at string reading. This proves it.
     */
    @Deprecated
    private static String labelBodies(HashMap<String, String> bodies, String possibleBodyContainer) {
        var labeledString = possibleBodyContainer;
        var start = possibleBodyContainer.indexOf('{');
        var end = possibleBodyContainer.lastIndexOf('}');

        if (start != -1 && end != -1) {
            var unfixedBodyText = possibleBodyContainer.substring(start + 1, end);
            var label = "B_" + bodies.size();
            // placeholder so when recurring can use size of map.
            bodies.put(label, unfixedBodyText);

            // check for bodies in the body & replace placeholder after
            var innerFixedBodyText = labelBodies(bodies, unfixedBodyText);
            bodies.put(label, innerFixedBodyText);

            labeledString = possibleBodyContainer.replace(unfixedBodyText, label);
        }

        return labeledString;
    }

    private static List<String> findNextKeywords(String string, List<String> keywords) {
        var nextKeyWordIndex = new HashMap<String, Integer>();

        for (var keyword : keywords) {
            int index = string.indexOf(keyword);
            if (index != -1) nextKeyWordIndex.put(keyword, index);
        }

        return nextKeyWordIndex.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(Map.Entry::getKey)
                .toList();
    }
}
