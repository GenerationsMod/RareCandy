// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm.tracks._byte;

import gg.generations.rarecandy.renderer.animation.TransformStorage;
import org.joml.Vector3f;

public class FixedByteTrackT implements ByteProcessor {
  private int byte_;

  public int getByte() { return byte_; }

  public void setByte(int byte_) { this.byte_ = byte_; }


  public FixedByteTrackT() {
    this.byte_ = 0;
  }

  public void process(TransformStorage<Byte> keys) {
    keys.add(0, (byte) byte_);
  }

}

