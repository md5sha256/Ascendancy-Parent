package com.gmail.andrewandy.ascendancy.client.keybinds;

import com.gmail.andrewandy.ascendancy.client.io.ClientPacketHandler;
import com.gmail.andrewandy.ascendancy.lib.AscendancyPacket;
import com.gmail.andrewandy.ascendancy.lib.keybind.AscendancyKey;
import com.gmail.andrewandy.ascendancy.lib.keybind.CustomKeyPressedPacket;
import com.gmail.andrewandy.ascendancy.lib.keybind.KeyPressAction;
import com.gmail.andrewandy.ascendancy.lib.results.AscendancyResultPacket;
import com.gmail.andrewandy.ascendancy.lib.results.Result;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public enum ActiveKeyBind {

    INSTANCE;

    private final KeyBinding keyBinding = new KeyBinding("Active Key", Keyboard.KEY_F, "key.Ascendency");
    private boolean pressed = false;

    ActiveKeyBind() {
    }

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        KeyPressAction action;
        if (keyBinding.isKeyDown()) {
            action = KeyPressAction.HOLDING;
            pressed = true;
        } else if (keyBinding.isPressed()) {
            action = KeyPressAction.PRESS;
            pressed = true;
        } else {
            if (!pressed) {
                action = KeyPressAction.RELEASE;
                pressed = false;
            } else {
                throw new IllegalStateException("Invalid state! Unknown key press status!");
            }
        }
        CustomKeyPressedPacket packet = new CustomKeyPressedPacket(ForgeHooks.getCraftingPlayer().getPersistentID(), action, AscendancyKey.ACTIVE_KEY);
        AscendancyPacket result = ClientPacketHandler.getInstance().onMessage(packet);
        assert result instanceof AscendancyResultPacket;
        AscendancyResultPacket resultPacket = (AscendancyResultPacket) result;
        if (resultPacket.getResult() != Result.SUCCESS) {
            System.out.println("[Ascendency] WARNING - Unexpected response received. Got " + resultPacket.getResult().name() + ", expected " + Result.SUCCESS.name());
        }
    }

}
