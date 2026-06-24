package gg.generations.rarecandy.pokeutils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;

import java.util.Collections;
import java.util.List;

public interface IHideDuringAnimation {
    IHideDuringAnimation NONE = new IHideDuringAnimation() {
        @Override
        public boolean blackList() {
            return false;
        }

        @Override
        public List<String> animations() {
            return Collections.emptyList();
        }
    };

    boolean blackList();
    List<String> animations();

    default boolean check(String animation) {
        return animations().contains(animation) != blackList();
    }

    static IHideDuringAnimation deserialize(JsonElement element) {
        var obj = element.getAsJsonObject();
        var blackList = JsonUtils.extractIfPresent(obj, "blackList", false, JsonElement::getAsBoolean);
        var animations = JsonUtils.extractListIfPresent(obj, "animations", JsonElement::getAsString);

        return IModelConfig.Factory.ACTIVE_FACTORY.createHideDuringAnimation(blackList, animations);
    }

    static JsonElement serialize(IHideDuringAnimation hideDuringAnimation) {
        var obj = new JsonObject();
        JsonUtils.putIf(value -> value, obj, "blackList", hideDuringAnimation.blackList(), JsonPrimitive::new);
        JsonUtils.putListIf(obj, "animations", hideDuringAnimation.animations(), JsonPrimitive::new);
        return obj;
    }

    //        public boolean check(Animation animation) {
//            return check(animation != null ? animation.name : null);
//        }
}
