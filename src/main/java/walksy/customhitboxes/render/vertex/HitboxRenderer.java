package walksy.customhitboxes.render.vertex;

import java.util.Map;
import java.util.WeakHashMap;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;
import walksy.customhitboxes.config.Config;
import walksy.customhitboxes.render.FrustumManager;

public class HitboxRenderer {
    private final WorldRenderContext context;
    private final MinecraftClient client;
    private static final Map<Entity, Config.EntityEntry> ENTRY_CACHE = new WeakHashMap<>();

    public HitboxRenderer(WorldRenderContext context) {
        this.context = context;
        this.client = MinecraftClient.getInstance();
    }

    public void render() {
        if (this.client.world == null) return;

        Profilers.get().push("customHitboxes");
        for (Entity entity : this.client.world.getEntities()) {
            Profilers.get().push("entity");

            if (!FrustumManager.isVisible(entity.getBoundingBox())) {
                Profilers.get().pop();
                continue;
            }

            if (this.shouldRender(entity)) {
                Config.EntityEntry entry = Config.getEntry(entity);
                float tickProgress = this.client.getRenderTickCounter().getTickProgress(false);

                Profilers.get().push("draw");
                this.drawHitbox(entity, entry, tickProgress);
                Profilers.get().pop();
            }
            Profilers.get().pop();
        }
        Profilers.get().pop();
    }

    private void drawHitbox(Entity entity, Config.EntityEntry entry, float tickProgress) {
        Profilers.get().push("setup");

        Vec3d cameraPos = this.context.gameRenderer().getCamera().getCameraPos();
        Vec3d lerpedPos = entity.getLerpedPos(tickProgress);
        float opacityMultiplier = 1.0f;

        if (entry.vanishWhenClose) {
            double distSq = lerpedPos.squaredDistanceTo(cameraPos);
            double vanishDistSq = entry.vanishDistance * entry.vanishDistance;

            if (distSq <= vanishDistSq) {
                Profilers.get().pop();
                return;
            }

            if (entry.vanishFade) {
                double fadeStartDist = entry.vanishDistance + 3.0;
                double fadeStartDistSq = fadeStartDist * fadeStartDist;
                if (distSq < fadeStartDistSq) {
                    opacityMultiplier = (float) ((Math.sqrt(distSq) - entry.vanishDistance) / (fadeStartDist - entry.vanishDistance));
                }
            }
        }

        Vec3d entityPos = entity.getEntityPos();
        Vec3d offset = lerpedPos.subtract(entityPos);
        Box box = entity.getBoundingBox().offset(offset);

        this.context.matrices().push();
        this.context.matrices().translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Profilers.get().pop();

        boolean inRange = this.isInRange(entity);
        boolean lookingAt = this.isLookingAt(entity);

        if (entry.fill) {
            Profilers.get().push("fill");
            WalksyLibColor[] colors = getColors(entity, entry, true, inRange, lookingAt);
            VertexRenderer.drawFilledBox(this.context.commandQueue(), this.context.matrices(), this.context.consumers(),
                    box, applyFade(colors[0], opacityMultiplier), applyFade(colors[1], opacityMultiplier));
            Profilers.get().pop();
        }

        if (entry.lines) {
            Profilers.get().push("lines");
            WalksyLibColor[] colors = getColors(entity, entry, false, inRange, lookingAt);
            VertexRenderer.drawOutlineBox(this.context.commandQueue(), this.context.matrices(), this.context.consumers(),
                    box, applyFade(colors[0], opacityMultiplier), applyFade(colors[1], opacityMultiplier), entry.boundingBoxLineWidth);
            Profilers.get().pop();
        }

        if (entity instanceof EnderDragonEntity dragon) {
            Profilers.get().push("dragon");
            this.drawDragonBoundingBoxes(dragon, entry, tickProgress, opacityMultiplier);
            Profilers.get().pop();
        }

        if (entry.eyeHeightBox) {
            Profilers.get().push("eyeHeight");
            this.drawEyeHeightBox(entity, offset, entry, opacityMultiplier);
            Profilers.get().pop();
        }

        if (entry.lookVector) {
            Profilers.get().push("lookVector");
            this.drawLookVector(entity, lerpedPos, entry, tickProgress, opacityMultiplier);
            Profilers.get().pop();
        }

        this.context.matrices().pop();
    }

    private WalksyLibColor[] getColors(Entity entity, Config.EntityEntry entry, boolean isFill, boolean inRange, boolean lookingAt) {
        WalksyLibColor color1, color2;
        boolean isLiving = entity instanceof LivingEntity;
        LivingEntity living = isLiving ? (LivingEntity) entity : null;

        if (isFill) {
            if (entry.fillDamageColor && isLiving && living.hurtTime > 0) {
                color1 = color2 = entry.boundingBoxFillDamageColor;
            } else if (lookingAt && entry.fillTargetColor) {
                color1 = color2 = entry.boundingBoxFillTargetColor;
            } else if (inRange && entry.fillInRangeColor) {
                color1 = color2 = entry.boundingBoxFillInRangeColor;
            } else {
                color1 = entry.boundingBoxFillColor;
                color2 = entry.fillGradient ? entry.boundingBoxFillGradientColor : color1;
            }
        } else {
            if (entry.lineDamageColor && isLiving && living.hurtTime > 0) {
                color1 = color2 = entry.boundingBoxLineDamageColor;
            } else if (lookingAt && entry.lineTargetColor) {
                color1 = color2 = entry.boundingBoxLineTargetColor;
            } else if (inRange && entry.lineInRangeColor) {
                color1 = color2 = entry.boundingBoxLineInRangeColor;
            } else {
                color1 = entry.boundingBoxLineColor;
                color2 = entry.linesGradient ? entry.boundingBoxLineGradientColor : color1;
            }
        }
        return new WalksyLibColor[]{color1, color2};
    }

    private void drawDragonBoundingBoxes(EnderDragonEntity dragon, Config.EntityEntry entry, float tickProgress, float opacityMultiplier) {
        for (EnderDragonPart part : dragon.getBodyParts()) {
            Profilers.get().push("part");
            Vec3d offset = part.getLerpedPos(tickProgress).subtract(part.getEntityPos());
            Box partBox = part.getBoundingBox().offset(offset);

            if (entry.fill) {
                VertexRenderer.drawFilledBox(this.context.commandQueue(), this.context.matrices(), this.context.consumers(),
                        partBox, applyFade(entry.boundingBoxFillColor, opacityMultiplier), applyFade(entry.boundingBoxFillGradientColor, opacityMultiplier));
            }
            if (entry.lines) {
                VertexRenderer.drawOutlineBox(this.context.commandQueue(), this.context.matrices(), this.context.consumers(),
                        partBox, applyFade(entry.boundingBoxLineColor, opacityMultiplier), applyFade(entry.boundingBoxLineGradientColor, opacityMultiplier), entry.boundingBoxLineWidth);
            }
            Profilers.get().pop();
        }
    }

    private void drawEyeHeightBox(Entity entity, Vec3d offset, Config.EntityEntry entry, float opacityMultiplier) {
        if (!(entity instanceof LivingEntity)) return;

        Box box = entity.getBoundingBox().offset(offset);
        double eyeY = box.minY + (double) entity.getStandingEyeHeight();
        double expand = 1.0E-4;
        Box eyeBox = new Box(box.minX - expand, eyeY - 0.01, box.minZ - expand, box.maxX + expand, eyeY + 0.01, box.maxZ + expand);

        WalksyLibColor color = applyFade(entry.eyeHeightBoxColor, opacityMultiplier);
        if (entry.eyeHeightBoxFill) {
            VertexRenderer.drawFilledBox(this.context.commandQueue(), this.context.matrices(), this.context.consumers(), eyeBox, color, color);
        } else {
            VertexRenderer.drawOutlineBox(this.context.commandQueue(), this.context.matrices(), this.context.consumers(), eyeBox, color, color, entry.eyeHeightBoxLineWidth);
        }
    }

    private void drawLookVector(Entity entity, Vec3d lerpedPos, Config.EntityEntry entry, float tickProgress, float opacityMultiplier) {
        Vec3d start = lerpedPos.add(0.0, entity.getStandingEyeHeight(), 0.0);
        Vec3d end = start.add(entity.getRotationVec(tickProgress).multiply(2.0));
        VertexRenderer.drawArrow(this.context.matrices(), this.context.consumers(), start, end,
                applyFade(entry.lookVectorShaftColor, opacityMultiplier), applyFade(entry.lookVectorArrowColor, opacityMultiplier),
                entry.lookVectorShaftWidth, entry.lookVectorArrow);
    }

    private WalksyLibColor applyFade(WalksyLibColor original, float multiplier) {
        if (multiplier >= 1.0f) return original;
        return new WalksyLibColor(original.getRed(), original.getGreen(), original.getBlue(), (int) (original.getAlpha() * multiplier));
    }

    private boolean shouldRender(Entity entity) {
        boolean hitboxEnabled = this.client.debugHudEntryList.isEntryVisible(DebugHudEntries.ENTITY_HITBOXES);
        return !entity.isInvisible()
                && hitboxEnabled
                && Config.shouldRender(entity)
                && (entity != this.client.getCameraEntity() || this.client.options.getPerspective() != Perspective.FIRST_PERSON);
    }

    private boolean isLookingAt(Entity entity) {
        return this.client.targetedEntity == entity;
    }

    private boolean isInRange(Entity target) {
        if (this.client.player == null) return false;
        double reach = this.client.player.getEntityInteractionRange();
        Vec3d eyePos = this.client.player.getEyePos();
        return target.getBoundingBox().squaredMagnitude(eyePos) <= reach * reach;
    }
}