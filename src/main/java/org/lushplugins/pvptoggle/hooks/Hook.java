package org.lushplugins.pvptoggle.hooks;

public abstract class Hook {
    private boolean enabled = false;

    protected abstract void onEnable();

    public final void enable() {
        this.onEnable();
        this.enabled = true;
    }

    protected abstract void onDisable();

    public final void disable() {
        this.onDisable();
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
