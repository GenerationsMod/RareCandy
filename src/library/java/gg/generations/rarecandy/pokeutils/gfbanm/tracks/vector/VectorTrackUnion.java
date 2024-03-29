// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm.tracks.vector;

import com.google.flatbuffers.FlatBufferBuilder;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.TrackProcesser;
import org.joml.Vector3f;

public class VectorTrackUnion {
  private byte type;
  private VectorProcessor value;

  public byte getType() { return type; }

  public void setType(byte type) { this.type = type; }

  public VectorProcessor getValue() {
    return value;
  }

  public void setValue(VectorProcessor value) { this.value = value; }

  public VectorTrackUnion() {
    this.type = VectorTrack.NONE;
    this.value = null;
  }

  public FixedVectorTrackT asFixedVectorTrack() { return (FixedVectorTrackT) value; }
  public DynamicVectorTrackT asDynamicVectorTrack() { return (DynamicVectorTrackT) value; }
  public Framed16VectorTrackT asFramed16VectorTrack() { return (Framed16VectorTrackT) value; }
  public Framed8VectorTrackT asFramed8VectorTrack() { return (Framed8VectorTrackT) value; }

  public static int pack(FlatBufferBuilder builder, VectorTrackUnion _o) {
      return switch (_o.type) {
          case VectorTrack.FixedVectorTrack -> FixedVectorTrack.pack(builder, _o.asFixedVectorTrack());
          case VectorTrack.DynamicVectorTrack -> DynamicVectorTrack.pack(builder, _o.asDynamicVectorTrack());
          case VectorTrack.Framed16VectorTrack -> Framed16VectorTrack.pack(builder, _o.asFramed16VectorTrack());
          case VectorTrack.Framed8VectorTrack -> Framed8VectorTrack.pack(builder, _o.asFramed8VectorTrack());
          default -> 0;
      };
  }
}

