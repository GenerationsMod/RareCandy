// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.GFLib.Anim;

import gg.generations.rarecandy.renderer.animation.TransformStorage;

public class DynamicFloatTrackT implements TrackProcesser<Float> {
    private float[] float_;

    public float[] getFloat() {
        return float_;
    }

    public void setFloat(float[] float_) {
        this.float_ = float_;
    }


    public DynamicFloatTrackT() {
        this.float_ = null;
    }

    @Override
    public void process(TransformStorage<Float> rotationKeys) {
        for (int i = 0; i < float_.length; i++) {
            var vec = float_[i];
            rotationKeys.add(i, vec);
        }
    }
}

