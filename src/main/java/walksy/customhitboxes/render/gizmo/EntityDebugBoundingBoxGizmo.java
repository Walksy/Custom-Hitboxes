package walksy.customhitboxes.render.gizmo;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.GizmoDrawer;
import walksy.customhitboxes.config.Config;

public record EntityDebugBoundingBoxGizmo(Entity entity, Vec3d offset, Config.EntityEntry entry) implements Gizmo {

    @Override
    public void draw(GizmoDrawer consumer, float opacity) {
        if (this.entry == null) {
            return;
        }
        Box aabb = this.entity.getBoundingBox().offset(offset);
        double d = aabb.minX;
        double e = aabb.minY;
        double f = aabb.minZ;
        double g = aabb.maxX;
        double h = aabb.maxY;
        double i = aabb.maxZ;
        if (this.entry.fill) {
            int j = ColorHelper.scaleAlpha(this.entry.boundingBoxFillColor.getRGB(), opacity);
            consumer.addQuad(new Vec3d(g, e, f), new Vec3d(g, h, f), new Vec3d(g, h, i), new Vec3d(g, e, i), j);
            consumer.addQuad(new Vec3d(d, e, f), new Vec3d(d, e, i), new Vec3d(d, h, i), new Vec3d(d, h, f), j);
            consumer.addQuad(new Vec3d(d, e, f), new Vec3d(d, h, f), new Vec3d(g, h, f), new Vec3d(g, e, f), j);
            consumer.addQuad(new Vec3d(d, e, i), new Vec3d(g, e, i), new Vec3d(g, h, i), new Vec3d(d, h, i), j);
            consumer.addQuad(new Vec3d(d, h, f), new Vec3d(d, h, i), new Vec3d(g, h, i), new Vec3d(g, h, f), j);
            consumer.addQuad(new Vec3d(d, e, f), new Vec3d(g, e, f), new Vec3d(g, e, i), new Vec3d(d, e, i), j);
        }

        if (this.entry.lines) {
            int j = ColorHelper.scaleAlpha(this.entry.boundingBoxLineColor.getRGB(), opacity);
            double width = this.entry.boundingBoxLineWidth;
            consumer.addLine(new Vec3d(d, e, f), new Vec3d(g, e, f), j, (float) width);
            consumer.addLine(new Vec3d(d, e, f), new Vec3d(d, h, f), j, (float) width);
            consumer.addLine(new Vec3d(d, e, f), new Vec3d(d, e, i), j, (float) width);
            consumer.addLine(new Vec3d(g, e, f), new Vec3d(g, h, f), j, (float) width);
            consumer.addLine(new Vec3d(g, h, f), new Vec3d(d, h, f), j, (float) width);
            consumer.addLine(new Vec3d(d, h, f), new Vec3d(d, h, i), j, (float) width);
            consumer.addLine(new Vec3d(d, h, i), new Vec3d(d, e, i), j, (float) width);
            consumer.addLine(new Vec3d(d, e, i), new Vec3d(g, e, i), j, (float) width);
            consumer.addLine(new Vec3d(g, e, i), new Vec3d(g, e, f), j, (float) width);
            consumer.addLine(new Vec3d(d, h, i), new Vec3d(g, h, i), j, (float) width);
            consumer.addLine(new Vec3d(g, e, i), new Vec3d(g, h, i), j, (float) width);
            consumer.addLine(new Vec3d(g, h, f), new Vec3d(g, h, i), j, (float) width);
        }
    }
}
