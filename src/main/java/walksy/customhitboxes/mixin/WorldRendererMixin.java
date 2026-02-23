package walksy.customhitboxes.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.customhitboxes.render.FrustumManager;

@Mixin(value={WorldRenderer.class})
public abstract class WorldRendererMixin {

    @Shadow protected abstract Frustum setupFrustum(Matrix4f posMatrix, Matrix4f projMatrix, Vec3d pos);

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/WorldRenderer;setupFrustum(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/client/render/Frustum;")})
    public void onRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        Vec3d vec3d = camera.getCameraPos();
        FrustumManager.updateFrustum(this.setupFrustum(positionMatrix, projectionMatrix, vec3d));
    }
}
