package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.util.JomlConstants;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;
import gg.generations.rarecandy.renderer.model.material.*;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.*;

public record MaterialReference(
    String parent,
    String shader,
    CullType cull,
    BlendType blend,
    IMaterialImages images,
    IMaterialValues values
) implements IMaterialReference {
}
