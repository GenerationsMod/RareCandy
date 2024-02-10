// automatically generated by the FlatBuffers compiler, do not modify

package gg.generationsmod.rarecandy.model.animation.gfbanm;

import gg.generationsmod.rarecandy.model.animation.TransformStorage;
import org.joml.Vector3f;

public class DynamicVectorTrackT implements TrackProcesser<Vector3f> {
  private Vec3T[] co;

  public Vec3T[] getCo() { return co; }

  public void setCo(Vec3T[] co) { this.co = co; }


  public DynamicVectorTrackT() {
    this.co = null;
  }

  @Override
  public void process(TransformStorage<Vector3f> keys) {
    for (int i = 0; i < co.length; i++) {
      var vec = getCo()[i];
      keys.add(i, new Vector3f(vec.getX(), vec.getY(), vec.getZ()));
    }

  }
}

