package gg.generationsmod.rarecandy.model.config.rks.animation;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
public class AnimationInfo {
    private static final Random RANDOM = new Random();
    private String start;
    private String main;
    private String end;
    private List<String> meshesToHide;
    private List<String> meshesToShow;
    private double chance = 100;
    private List<String> variants = new ArrayList<>();

    public boolean hasStart() {
        return start != null;
    }

    public boolean hasEnd() {
        return end != null;
    }

    @Nullable
    public String getStartAnimation() {
        return start;
    }

    public String getMainAnimation() {
        if (!variants.isEmpty() && RANDOM.nextDouble(0, 101) <= chance)
            return variants.get(RANDOM.nextInt(0, variants.size()));
        else return main;
    }

    @Nullable
    public String getEndAnimation() {
        return end;
    }

    public List<String> getAllPossibleAnimations() {
        var allAnimations = new ArrayList<>(variants);
        if(start != null) allAnimations.add(start);
        if(main != null) allAnimations.add(main);
        if(end != null) allAnimations.add(end);
        return allAnimations;
    }

    public List<String> getAllMainAnimations() {
        var allAnimations = new ArrayList<>(variants);
        if(main != null) allAnimations.add(main);
        return allAnimations;
    }
}
