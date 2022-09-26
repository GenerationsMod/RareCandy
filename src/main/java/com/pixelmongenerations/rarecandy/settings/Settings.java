package com.pixelmongenerations.rarecandy.settings;

public record Settings(int pbrDistance, int differentAnimationCount, boolean useLightMap, TransparencyMethod transparencyMethod, boolean usePlaceholderUntilModelsLoad, int modelLoadingThreads) {
}
