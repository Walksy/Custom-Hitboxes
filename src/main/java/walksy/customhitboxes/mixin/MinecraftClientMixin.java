package walksy.customhitboxes.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.customhitboxes.config.Config;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    @Final
    public DebugHudProfile debugHudEntryList;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onGameLoad(RunArgs args, CallbackInfo ci) {
        if (Config.modEnabled && Config.enableHitboxesOnGameLoad && !this.debugHudEntryList.isEntryVisible(DebugHudEntries.ENTITY_HITBOXES)) {
            this.debugHudEntryList.toggleVisibility(DebugHudEntries.ENTITY_HITBOXES);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (Config.modEnabled) {
            Config.tick();
        }
    }
}
