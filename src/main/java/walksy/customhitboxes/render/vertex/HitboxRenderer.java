package walksy.customhitboxes.render.vertex;

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

import java.util.Map;
import java.util.WeakHashMap;

public class HitboxRenderer {

    final WorldRenderContext context;
    private static final Map<Entity, Config.EntityEntry> ENTRY_CACHE = new WeakHashMap<>();

    public HitboxRenderer(WorldRenderContext context) {
        this.context = context;
    }

    private Config.EntityEntry getCachedEntry(Entity entity) {
        Profilers.get().push("cache");
        Config.EntityEntry entry = ENTRY_CACHE.computeIfAbsent(entity, Config::getEntry);
        Profilers.get().pop();
        return entry;
    }

    public void render() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Profilers.get().push("customHitboxes");

        for (Entity entity : client.world.getEntities()) {
            Profilers.get().push("entity");

            Profilers.get().push("frustum");
            if (!FrustumManager.isVisible(entity.getBoundingBox())) {
                Profilers.get().pop();
                Profilers.get().pop();
                continue;
            }
            Profilers.get().pop();

            Profilers.get().push("shouldRender");
            boolean render = this.shouldRender(client, entity);
            Profilers.get().pop();

            if (render) {
                Config.EntityEntry entry = getCachedEntry(entity);
                float tickProgress = client.getRenderTickCounter().getTickProgress(false);

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
            double vanishDist = entry.vanishDistance;
            double vanishDistSq = vanishDist * vanishDist;

            double dx = lerpedPos.x - cameraPos.x;
            double dy = lerpedPos.y - cameraPos.y;
            double dz = lerpedPos.z - cameraPos.z;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= vanishDistSq) {
                Profilers.get().pop();
                return;
            }

            if (entry.vanishFade) {
                double fadeStartDist = vanishDist + 3.0;
                double fadeStartDistSq = fadeStartDist * fadeStartDist;

                if (distSq < fadeStartDistSq) {
                    double t = (Math.sqrt(distSq) - vanishDist) / (fadeStartDist - vanishDist);
                    opacityMultiplier = (float) t;
                }
            }
        }


        Vec3d entityPos = entity.getEntityPos();
        Vec3d offset = lerpedPos.subtract(entityPos);

        this.context.matrices().push();
        this.context.matrices().translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());

        Profilers.get().pop();

        if (entry.fill) {
            Profilers.get().push("fill");

            WalksyLibColor color = (entry.fillDamageColor && entity instanceof LivingEntity living && living.hurtTime > 0)
                ? entry.boundingBoxFillDamageColor
                : entry.boundingBoxFillColor;

            WalksyLibColor color2 = entry.fillGradient
                ? entry.boundingBoxFillGradientColor
                : color;

            VertexRenderer.drawFilledBox(
                this.context.commandQueue(),
                this.context.matrices(),
                this.context.consumers(),
                entity.getBoundingBox().offset(offset),
                applyFade(color, opacityMultiplier),
                applyFade(color2, opacityMultiplier)
            );

            Profilers.get().pop();
        }

        if (entry.lines) {
            Profilers.get().push("lines");

            WalksyLibColor color = (entry.lineDamageColor && entity instanceof LivingEntity living && living.hurtTime > 0)
                ? entry.boundingBoxLineDamageColor
                : entry.boundingBoxLineColor;

            WalksyLibColor color2 = entry.linesGradient
                ? entry.boundingBoxLineGradientColor
                : color;

            VertexRenderer.drawOutlineBox(
                this.context.commandQueue(),
                this.context.matrices(),
                this.context.consumers(),
                entity.getBoundingBox().offset(offset),
                this.applyFade(color, opacityMultiplier),
                this.applyFade(color2, opacityMultiplier),
                entry.boundingBoxLineWidth
            );

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

    private void drawDragonBoundingBoxes(EnderDragonEntity dragon,
                                         Config.EntityEntry entry,
                                         float tickProgress,
                                         float opacityMultiplier) {

        for (EnderDragonPart part : dragon.getBodyParts()) {
            Profilers.get().push("part");

            Vec3d base = part.getEntityPos();
            Vec3d lerped = part.getLerpedPos(tickProgress);
            Vec3d offset = lerped.subtract(base);

            if (entry.fill) {
                VertexRenderer.drawFilledBox(
                    this.context.commandQueue(),
                    this.context.matrices(),
                    this.context.consumers(),
                    part.getBoundingBox().offset(offset),
                    applyFade(entry.boundingBoxFillColor, opacityMultiplier),
                    applyFade(entry.boundingBoxFillGradientColor, opacityMultiplier)
                );
            }

            if (entry.lines) {
                VertexRenderer.drawOutlineBox(
                    this.context.commandQueue(),
                    this.context.matrices(),
                    this.context.consumers(),
                    part.getBoundingBox().offset(offset),
                    applyFade(entry.boundingBoxLineColor, opacityMultiplier),
                    applyFade(entry.boundingBoxLineGradientColor, opacityMultiplier),
                    entry.boundingBoxLineWidth
                );
            }

            Profilers.get().pop();
        }
    }

    private void drawEyeHeightBox(Entity entity, Vec3d offset, Config.EntityEntry entry, float opacityMultiplier) {
        if (!(entity instanceof LivingEntity)) return;

        Box box = entity.getBoundingBox().offset(offset);
        double eyeY = box.minY + entity.getStandingEyeHeight();

        double expand = 0.0001;
        Box eyeBox = new Box(
            box.minX - expand,
            eyeY - 0.01,
            box.minZ - expand,
            box.maxX + expand,
            eyeY + 0.01,
            box.maxZ + expand
        );

        WalksyLibColor color = this.applyFade(entry.eyeHeightBoxColor, opacityMultiplier);

        if (entry.eyeHeightBoxFill) {
            VertexRenderer.drawFilledBox(
                this.context.commandQueue(),
                this.context.matrices(),
                this.context.consumers(),
                eyeBox,
                color,
                color
            );
        } else {
            VertexRenderer.drawOutlineBox(
                this.context.commandQueue(),
                this.context.matrices(),
                this.context.consumers(),
                eyeBox,
                color,
                color,
                entry.eyeHeightBoxLineWidth
            );
        }
    }

    private void drawLookVector(Entity entity,
                                Vec3d lerpedPos,
                                Config.EntityEntry entry,
                                float tickProgress,
                                float opacityMultiplier) {

        Vec3d start = lerpedPos.add(0.0, entity.getStandingEyeHeight(), 0.0);
        Vec3d end = start.add(entity.getRotationVec(tickProgress).multiply(2));

        VertexRenderer.drawArrow(
            this.context.matrices(),
            this.context.consumers(),
            start,
            end,
            applyFade(entry.lookVectorShaftColor, opacityMultiplier),
            applyFade(entry.lookVectorArrowColor, opacityMultiplier),
            entry.lookVectorShaftWidth,
            entry.lookVectorArrow
        );
    }

    private WalksyLibColor applyFade(WalksyLibColor original, float multiplier) {
        if (multiplier >= 1.0f) return original;
        return new WalksyLibColor(
            original.getRed(),
            original.getGreen(),
            original.getBlue(),
            (int) (original.getAlpha() * multiplier)
        );
    }

    public boolean shouldRender(MinecraftClient client, Entity entity) {
        return !entity.isInvisible()
            && client.debugHudEntryList.isEntryVisible(DebugHudEntries.ENTITY_HITBOXES)
            && Config.shouldRender(entity)
            && (entity != client.getCameraEntity()
            || client.options.getPerspective() != Perspective.FIRST_PERSON);
    }

    public boolean isInRange(Entity entity) {

    }
}

