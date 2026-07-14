package com.example.renderdistancemacro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class RenderDistanceMacroClient implements ClientModInitializer {

    private static net.minecraft.client.option.KeyBinding triggerKey;

    private static boolean macroActive = false;
    private static int previousRenderDistance = -1;
    private static int ticksWaited = 0;

    private static final int HOLD_TICKS = 1;

    @Override
    public void onInitializeClient() {
        triggerKey = KeyBindingHelper.registerKeyBinding(new net.minecraft.client.option.KeyBinding(
                "key.renderdistancemacro.trigger",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.renderdistancemacro"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(MinecraftClient client) {
        while (triggerKey.wasPressed()) {
            if (!macroActive) {
                startMacro(client);
            }
        }

        if (macroActive) {
            ticksWaited++;
            if (ticksWaited >= HOLD_TICKS) {
                finishMacro(client);
            }
        }
    }

    private void startMacro(MinecraftClient client) {
        if (client.options == null) return;

        SimpleOption<Integer> viewDistanceOption = client.options.getViewDistance();
        previousRenderDistance = viewDistanceOption.getValue();

        viewDistanceOption.setValue(2);

        macroActive = true;
        ticksWaited = 0;
    }

    private void finishMacro(MinecraftClient client) {
        if (client.options != null && previousRenderDistance > 0) {
            client.options.getViewDistance().setValue(previousRenderDistance);
        }

        macroActive = false;
        ticksWaited = 0;

        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("Render macro executed successfully").formatted(Formatting.GREEN),
                    true
            );
        }

        previousRenderDistance = -1;
    }
}
