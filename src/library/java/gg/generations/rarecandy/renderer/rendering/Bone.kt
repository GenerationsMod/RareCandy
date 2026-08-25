package gg.generations.rarecandy.renderer.rendering

import org.joml.Matrix4f
import java.util.*

/**
 * We re-use this structure for multiple meshes so if you are accessing this value from outside a meshes bone array do NOT trust it.
 */
data class Bone @JvmOverloads constructor(@JvmField var name: String, @JvmField val inverseBindMatrix: Matrix4f? = Matrix4f(), @JvmField var restPose: Matrix4f? = Matrix4f()) {

    override fun toString(): String = "Bone{name='$name'}"

    override fun hashCode(): Int = name.hashCode()

    override fun equals(o: Any?): Boolean = if (this === o) true else if (o !is Bone) false else name == o.name
}
