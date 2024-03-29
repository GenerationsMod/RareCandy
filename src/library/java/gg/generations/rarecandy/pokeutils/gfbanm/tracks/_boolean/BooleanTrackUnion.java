// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm.tracks._boolean;

import com.google.flatbuffers.FlatBufferBuilder;

public class BooleanTrackUnion {
  private byte type;
  private BooleanTrackProcessor value;

  public byte getType() { return type; }

  public void setType(byte type) { this.type = type; }

  public BooleanTrackProcessor getValue() { return value; }

  public void setValue(BooleanTrackProcessor value) { this.value = value; }

  public BooleanTrackUnion() {
    this.type = BooleanTrack.NONE;
    this.value = null;
  }

  public FixedBooleanTrackT asFixedBooleanTrack() { return (FixedBooleanTrackT) value; }
  public DynamicBooleanTrackT asDynamicBooleanTrack() { return (DynamicBooleanTrackT) value; }
  public Framed16BooleanTrackT asFramed16BooleanTrack() { return (Framed16BooleanTrackT) value; }
  public Framed8BooleanTrackT asFramed8BooleanTrack() { return (Framed8BooleanTrackT) value; }

  public static int pack(FlatBufferBuilder builder, BooleanTrackUnion _o) {
      return switch (_o.type) {
          case BooleanTrack.FixedBooleanTrack -> FixedBooleanTrack.pack(builder, _o.asFixedBooleanTrack());
          case BooleanTrack.DynamicBooleanTrack -> DynamicBooleanTrack.pack(builder, _o.asDynamicBooleanTrack());
          case BooleanTrack.Framed16BooleanTrack -> Framed16BooleanTrack.pack(builder, _o.asFramed16BooleanTrack());
          case BooleanTrack.Framed8BooleanTrack -> Framed8BooleanTrack.pack(builder, _o.asFramed8BooleanTrack());
          default -> 0;
      };
  }
}

