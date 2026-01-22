package walksy.customhitboxes.render.gizmo.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import walksy.customhitboxes.config.Config;
import walksy.customhitboxes.render.gizmo.EntityDebugBoundingBoxGizmo;

public class DebugHitboxRenderer implements DebugRenderer.Renderer {

    final MinecraftClient client;

    public DebugHitboxRenderer(MinecraftClient client) {
        this.client = client;

    }

    @Override
    public void render(double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum, float tickProgress) {
        if (this.client.world == null) return;
        Profilers.get().push("custom-hitboxes-gizmo");
        for (Entity entity : this.client.world.getEntities()) {
            if (!entity.isInvisible()
                && Config.shouldRender(entity)
                && (entity != this.client.getCameraEntity()
                || this.client.options.getPerspective() != Perspective.FIRST_PERSON)) {
                this.drawHitbox(entity, tickProgress);
            }

        }
        Profilers.get().pop();
    }

    private void drawHitbox(Entity entity, float tickProgress) {
        Vec3d vec3d = entity.getEntityPos();
        Vec3d vec3d2 = entity.getLerpedPos(tickProgress);
        Vec3d vec3d3 = vec3d2.subtract(vec3d);
        GizmoDrawing.collect(new EntityDebugBoundingBoxGizmo(entity, vec3d3, Config.getEntry(entity)));
        if (this.client.options.jumpKey.isPressed()) {
            GizmoDrawing.point(vec3d2, -1, 2.0F); //wtf is this
        }
        //wtf does this code do below too
        Entity entity2 = entity.getVehicle();
        if (entity2 != null) {
            float f = Math.min(entity2.getWidth(), entity.getWidth()) / 2.0F;
            Vec3d vec3d4 = entity2.getPassengerRidingPos(entity).add(vec3d3);
            GizmoDrawing.box(new Box(vec3d4.x - (double)f, vec3d4.y, vec3d4.z - (double)f, vec3d4.x + (double)f, vec3d4.y + (double)0.0625F, vec3d4.z + (double)f), DrawStyle.stroked(-256));
        }


        this.drawEyeHeightBox(entity, vec3d3);
        if (entity instanceof EnderDragonEntity enderDragonEntity) {
            this.drawDragonBoundingBoxes(enderDragonEntity, tickProgress);
        }
        this.drawLookVector(entity, vec3d2, tickProgress);
    }

    private void drawEyeHeightBox(Entity entity, Vec3d vec3d3) {
        if (entity instanceof LivingEntity) {
            Box box = entity.getBoundingBox().offset(vec3d3);
            GizmoDrawing.box(new Box(box.minX, box.minY + (double)entity.getStandingEyeHeight() - (double)0.01F, box.minZ, box.maxX, box.minY + (double)entity.getStandingEyeHeight() + (double)0.01F, box.maxZ), DrawStyle.stroked(-65536));
        }
    }

    private void drawDragonBoundingBoxes(EnderDragonEntity enderDragonEntity, float tickProgress) {
        for(EnderDragonPart enderDragonPart : enderDragonEntity.getBodyParts()) {
            Vec3d vec3d5 = enderDragonPart.getEntityPos();
            Vec3d vec3d6 = enderDragonPart.getLerpedPos(tickProgress);
            Vec3d vec3d7 = vec3d6.subtract(vec3d5);
            GizmoDrawing.box(enderDragonPart.getBoundingBox().offset(vec3d7), DrawStyle.stroked(ColorHelper.fromFloats(1.0F, 0.25F, 1.0F, 0.0F)));
        }
    }

    private void drawLookVector(Entity entity, Vec3d vec3d2, float tickProgress) {
        Vec3d vec3d8 = vec3d2.add((double)0.0F, (double)entity.getStandingEyeHeight(), (double)0.0F);
        Vec3d vec3d9 = entity.getRotationVec(tickProgress);
        GizmoDrawing.arrow(vec3d8, vec3d8.add(vec3d9.multiply((double)2.0F)), -16776961);
        /*
        if (inLocalServer) {
            Vec3d vec3d4 = entity.getVelocity();
            GizmoDrawing.arrow(vec3d2, vec3d2.add(vec3d4), -256);
        }
         */
    }
}

