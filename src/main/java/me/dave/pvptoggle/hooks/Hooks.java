package me.dave.pvptoggle.hooks;

import me.dave.pvptoggle.PvpTogglePlugin;
import me.dave.pvptoggle.hooks.custom.Hook;

import java.util.HashMap;

public class Hooks {
    private static final HashMap<String, Hook> hooks = new HashMap<>();

    public static void register(String hookName, Hook hook) {
        hooks.put(hookName, hook);
        hook.enable();
        PvpTogglePlugin.getInstance().getLogger().info(hookName + " hook has been registered.");
    }

    public static boolean isHookRegistered(String hookName) {
        return hooks.get(hookName) != null;
    }

    public static Hook getHook(String hookName) {
        return hooks.get(hookName);
    }
}
