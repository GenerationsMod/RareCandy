package gg.generations.rarecandy.renderer.rendering;

public class StateToggle {
    private final Runnable enable;
    private final Runnable disable;
    private boolean state;
    public StateToggle(Runnable enable, Runnable disable) {
        this.enable = enable;
        this.disable = disable;
    }

    public void toggle(boolean newState) {
        if(newState && !state) {
            state = true;
            enable.run();
        } else if(!newState && state) {
            state = false;
            disable.run();
        }

    }
}
