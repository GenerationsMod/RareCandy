package gg.generations.rarecandy.codec

import com.google.gson.*
import gg.generations.rarecandy.pokeutils.IModelConfig
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import java.io.InputStreamReader

// ---------------------------------------------------------------- errors

class JsonDecodeException internal constructor(
    private val path: ArrayDeque<String>,
    private val detail: String
) : RuntimeException(null, null, false, false) {

    constructor(detail: String) : this(ArrayDeque(), detail)

    internal fun prepend(segment: String) = apply { path.addFirst(segment) }

    override val message: String
        get() = if (path.isEmpty()) detail else "$${path.joinToString("")}: $detail"
}

fun fail(detail: String): Nothing {
    throw JsonDecodeException(detail)
}

internal inline fun <R> at(segment: String, block: () -> R): R =
    try {
        block()
    } catch (e: JsonDecodeException) {
        throw e.prepend(segment)
    }

val JsonElement.typeName: String
    get() = when {
        isJsonNull -> "null"
        isJsonArray -> "array"
        isJsonObject -> "object"
        asJsonPrimitive.isBoolean -> "boolean"
        asJsonPrimitive.isNumber -> "number"
        else -> "string"
    }

fun JsonElement.bool(): Boolean =
    if (this is JsonPrimitive && isBoolean) asBoolean else fail("expected boolean, got $typeName")

fun JsonElement.num(): Double =
    if (this is JsonPrimitive && isNumber) asDouble else fail("expected number, got $typeName")

fun JsonElement.str(): String =
    if (this is JsonPrimitive && isString) asString else fail("expected string, got $typeName")

fun JsonElement.arr(): JsonArray = this as? JsonArray ?: fail("expected array, got $typeName")

fun JsonElement.arr(size: Int): JsonArray {
    val arr = arr()
    return arr.takeIf { it.size() == size } ?: fail("expected $size elements, got ${arr.size()}")
}

fun JsonElement.obj(): JsonObject = this as? JsonObject ?: fail("expected object, got $typeName")

// ---------------------------------------------------------------- codec

interface Decoder<T> {
    fun decode(json: JsonElement): T
}

interface Encoder<T> {
    fun encode(value: T): JsonElement
}

interface Codec<T>: Encoder<T>, Decoder<T>

fun <T> codec(encoder: (T) -> JsonElement, decoder: (JsonElement) -> T): Codec<T> = object : Codec<T> {
    override fun encode(value: T) = encoder(value)
    override fun decode(json: JsonElement) = decoder(json)
}

fun <T> decoder(block: (JsonElement) -> T): Decoder<T> = object: Decoder<T> {
    override fun decode(json: JsonElement): T = block.invoke(json)
}

fun <T> Codec<T>.decodeOrNull(json: JsonElement): T? =
    try { decode(json) } catch (e: JsonDecodeException) { null }

fun <T> Codec<T>.parse(text: String): T =
    decode(try { JsonParser.parseString(text) } catch (e: JsonSyntaxException) { fail("malformed json: ${e.message}") })

fun obj(): JsonObject = JsonObject()
fun arr(): JsonArray = JsonArray()
fun arr(size: Int): JsonArray = JsonArray(size)

// ---------------------------------------------------------------- primitives

val BOOL = codec({ JsonPrimitive(it) }, { it.bool() })
val STRING: Codec<String> = codec({ JsonPrimitive(it) }, { it.str() })
val STRING_LIST = STRING.list()
val DOUBLE: Codec<Double> = codec({ JsonPrimitive(it) }, { it.num() })
val FLOAT: Codec<Float> = codec({ JsonPrimitive(it) }, { it.num().toFloat() })
val INT: Codec<Int> = codec({ JsonPrimitive(it) }, { it.num().toInt() })
val LONG: Codec<Long> = codec({ JsonPrimitive(it) }, { it.num().toLong() })
val ELEMENT: Codec<JsonElement> = codec({ it }, { it })

inline fun <reified E : Enum<E>> enumCodec(): Codec<E> {
    val constants = enumValues<E>()
    return codec(
        { JsonPrimitive(it.name) },
        { json ->
            val name = json.str()
            constants.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: fail("unknown ${E::class.simpleName} '$name', expected one of ${constants.joinToString { it.name }}")
        }
    )
}

// ---------------------------------------------------------------- combinators

fun <A, B> Codec<A>.xmap(to: (A) -> B, from: (B) -> A): Codec<B> =
    codec({ encode(from(it)) }, { to(decode(it)) })

fun <T> Codec<T>.list(): Codec<MutableList<T>> = codec(
    { values -> JsonArray(values.size).apply { values.forEachIndexed { i, v -> add(at("[$i]") { encode(v) }) } } },
    { json -> json.arr().mapIndexed { i, e -> at("[$i]") { decode(e) } }.toMutableList() }
)

fun <T> Codec<T>.map(): Codec<Map<String, T>> = codec(
    { values -> JsonObject().apply { values.forEach { (k, v) -> add(k, at(".$k") { encode(v) }) } } },
    { json -> json.obj().entrySet().associateTo(LinkedHashMap()) { (k, v) -> k to at(".$k") { decode(v) } } }
)

fun <T : Any> Codec<T>.orNull(): Codec<T?> = codec(
    { if (it == null) JsonNull.INSTANCE else encode(it) },
    { if (it.isJsonNull) null else decode(it) }
)

fun <T> Codec<T>.readAlso(fallback: Decoder<T>): Codec<T> = codec(
    { encode(it) },
    { json ->
        try {
            decode(json)
        } catch (first: JsonDecodeException) {
            try { fallback.decode(json) } catch (second: JsonDecodeException) { throw first }
        }
    }
)

fun <T> Codec<T>.readAlso(fallback: JsonElement.() -> T): Codec<T> = this.readAlso(object : Decoder<T> { override fun decode(json: JsonElement): T = fallback.invoke(json) })

fun <T> JsonObject.put(name: String, codec: Encoder<T>, value: T) = add(name, at(".$name") { codec.encode(value) })
fun <T> JsonObject.putIfNotNull(name: String, encoder: Encoder<T>, value: T?) { value?.also { add(name, encoder.encode(it)) } }
fun <T> JsonObject.readOrNull(name: String, codec: Decoder<T>): T? =  get(name)?.takeIf { !it.isJsonNull }?.let { at(".$name") { codec.decode(it) } }
fun <T> JsonObject.read(name: String, decoder: Decoder<T>): T = readOrNull(name, decoder) ?: fail("missing field '$name'")
fun <T> JsonObject.read(name: String, codec: Decoder<T>, default: T): T = get(name)?.takeIf { !it.isJsonNull }?.let { at(".$name") { codec.decode(it) } } ?: default
fun <V> JsonObject.readRest(codec: Codec<V>, vararg except: String): Map<String, V> =
    entrySet()
        .filter { it.key !in except }
        .associateTo(mutableMapOf()) { (k, e) -> k to at(".$k") { codec.decode(e) } }

fun <V> JsonObject.readRestAsMutable(codec: Codec<V>, vararg except: String): MutableMap<String, V> =
    entrySet()
        .filter { it.key !in except }
        .associateTo(mutableMapOf()) { (k, e) -> k to at(".$k") { codec.decode(e) } }


fun <V> JsonObject.putRest(codec: Codec<V>, values: Map<String, V>) {
    if(values.isEmpty()) return

    values.forEach { (k, v) ->
        if (has(k)) fail("key '$k' collides with a declared field")
        put(k, codec, v)
    }
}

fun JsonObject.getOrNull(name: String) = if(this.has(name)) this.get(name) else null

fun <T> JsonObject.readMap(name: String, decoder: Decoder<T>): MutableMap<String, T> = this.getOrNull(name)?.obj()?.asMap()?.mapValues { decoder.decode(it.value) }?.toMutableMap() ?: mutableMapOf()
fun <T> JsonObject.putMap(name: String, encoder: Encoder<T>, value: Map<String, T>) = this.add(name, obj().also { value.forEach { (key, value) -> it.put(key, encoder, value) } })

fun <T> JsonObject.putIfNotEquals(name: String, encoder: Encoder<T>, predicate: (T) -> Boolean, value: T?) {
    if(value != null && !predicate.invoke(value)) this.put(name, encoder, value)
}

fun <T> JsonArray.read(index: Int, decoder: Decoder<T>): T = get(index).takeIf { !it.isJsonNull }?.let { at("[$index]") { decoder.decode(it) } } ?: fail("position $index not found")
fun <T> JsonArray.read(index: Int, codec: Decoder<T>, default: T): T = get(index)?.takeIf { !it.isJsonNull }?.let { at("[$index]") { codec.decode(it) } } ?: default
fun JsonArray.readFloat(index: Int) = read(index, FLOAT)

fun <T> Gson.toJson(encoder: Encoder<T>, value: T) = this.toJson(encoder.encode(value))
fun <T> Gson.fromJson(reader: InputStreamReader, decoder: Decoder<T>) = this.fromJson(reader, JsonElement::class.java).let { decoder.decode(it) }
fun <T> String.fromJson(decoder: Decoder<T>): T = IModelConfig.GSON.fromJson(this, JsonElement::class.java).let { decoder.decode(it) }

// ---------------------------------------------------------------- joml

 val VEC2 = codec(
     { arr(2).apply { add(it.x); add(it.y) } },
     { json ->
         json.arr(3).let {
             Vector2f(
                 it.read(0, FLOAT, 0f),
                 it.read(0, FLOAT, 0f)
             )
         }
     }
 ).readAlso {
     var obj = obj()

     Vector2f(
         obj.read("x", FLOAT, 0f),
         obj.read("y", FLOAT, 0f)
     )
 }

 val VEC3 = codec(
     { arr(3).apply { add(it.x); add(it.y); add(it.z) } },
     { json ->
         json.arr(3).let {
             Vector3f(
                 it.read(0, FLOAT, 0f),
                 it.read(0, FLOAT, 0f),
                 it.read(0, FLOAT, 0f)
             )
         }
     }
 ).readAlso {
     var obj = obj()

     Vector3f(
         obj.read("x", FLOAT, 0f),
         obj.read("y", FLOAT, 0f),
         obj.read("z", FLOAT, 0f)
     )
 }

 val VEC4 = codec(
     { JsonArray(4).apply { add(it.x); add(it.y); add(it.z); add(it.w) } },
     { json ->
         json.arr(4).let {
             Vector4f(
                 it.read(0, FLOAT, 0f),
                 it.read(1, FLOAT, 0f),
                 it.read(2, FLOAT, 0f),
                 it.read(3, FLOAT, 0f)
             )
         }
     }
 ).readAlso {
     var obj = obj()

     Vector4f(
         obj.read("x", FLOAT, 0f),
         obj.read("y", FLOAT, 0f),
         obj.read("z", FLOAT, 0f),
         obj.read("w", FLOAT, 0f)
     )
 }

val QUAT =  VEC3.xmap({ it.mul(0.017453292f)}, { it.mul(57.29578f)}).xmap({
    Quaternionf().rotateXYZ(it.x, it.y, it.z)
}, { it.getEulerAnglesXYZ(Vector3f()) })