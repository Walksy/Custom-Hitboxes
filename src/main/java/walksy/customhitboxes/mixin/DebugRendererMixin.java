package walksy.customhitboxes.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.customhitboxes.config.Config;
import walksy.customhitboxes.render.gizmo.impl.DebugHitboxRenderer;

import java.util.List;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Redirect(method = "initRenderers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/debug/DebugHudProfile;isEntryVisible(Lnet/minecraft/util/Identifier;)Z"))
    public boolean redirectVanillaHitboxRenderer(DebugHudProfile instance, Identifier entryId) {
        if (Config.modEnabled && entryId.equals(DebugHudEntries.ENTITY_HITBOXES)) {
            return false;
        } else {
            return MinecraftClient.getInstance().debugHudEntryList.isEntryVisible(entryId);
        }
    }
}
