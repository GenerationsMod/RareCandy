package gg.generations.rarecandy.launch;

public enum MultithreadingEffectiveness {
    LOW(-1, 1),
    MEDIUM(6, 2),
    HIGH(8, 4);

    public final int sysCoreCount;
    public final int shouldUseXCores;

    MultithreadingEffectiveness(int sysCoreCount, int shouldUseXCores) {
        this.sysCoreCount = sysCoreCount;
        this.shouldUseXCores = shouldUseXCores;
    }
}
