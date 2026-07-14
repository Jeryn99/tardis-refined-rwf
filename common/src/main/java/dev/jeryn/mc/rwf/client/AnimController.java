package dev.jeryn.mc.rwf.client;

public class AnimController {

    private Anim current = Anim.PILOTING;
    private Anim previous = Anim.PILOTING;
    private float blendWeight = 1f;
    private float blendDuration = 10f;  // ticks to complete a blend
    private float blendProgress = 0f;

    public void requestAnim(Anim next) {
        if (next == current) return;
        previous = current;
        current = next;
        blendProgress = 0f;
        blendWeight = 0f;
    }

    /**
     * Call every tick from setupAnim
     */
    public void tick() {
        if (blendWeight < 1f) {
            blendProgress = Math.min(blendProgress + 1f, blendDuration);
            blendWeight = blendProgress / blendDuration;
        }
    }

    public Anim getCurrent() {
        return current;
    }

    public Anim getPrevious() {
        return previous;
    }

    public float getBlend() {
        return blendWeight;
    }

    public enum Anim {PILOTING, PILOTING2, PILOTING3}
}