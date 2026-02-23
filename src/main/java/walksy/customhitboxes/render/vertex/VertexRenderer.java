package walksy.customhitboxes.render.vertex;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class VertexRenderer {
    private static final RenderPipeline QUAD_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelines.POSITION_COLOR_SNIPPET}).withLocation("pipeline/walksy_quad_pipeline").withDepthWrite(true).withCull(true).build();
    private static final RenderLayer QUAD_RENDER_LAYER = RenderLayer.of((String)"walksy_quad_render_layer", (RenderSetup)RenderSetup.builder((RenderPipeline)QUAD_PIPELINE).translucent().layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).build());

    public static void drawOutlineBox(OrderedRenderCommandQueue queue, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Box box, WalksyLibColor color2, WalksyLibColor color, double lineWidth) {
        float r = (float)color.getRed() / 255.0f;
        float g = (float)color.getGreen() / 255.0f;
        float b = (float)color.getBlue() / 255.0f;
        float a = (float)color.getAlpha() / 255.0f;
        float r2 = (float)color2.getRed() / 255.0f;
        float g2 = (float)color2.getGreen() / 255.0f;
        float b2 = (float)color2.getBlue() / 255.0f;
        float a2 = (float)color2.getAlpha() / 255.0f;
        queue.submitCustom(matrices, RenderLayers.lines(), (entry, consumer) -> {
            VertexRenderer.drawLine(entry, consumer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, r, g, b, a, r, g, b, a, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, r, g, b, a, r, g, b, a, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, r, g, b, a, r, g, b, a, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, r, g, b, a, r, g, b, a, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, r2, g2, b2, a2, r2, g2, b2, a2, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, r2, g2, b2, a2, r2, g2, b2, a2, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, r2, g2, b2, a2, r2, g2, b2, a2, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, r2, g2, b2, a2, r2, g2, b2, a2, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, r, g, b, a, r2, g2, b2, a2, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, a, r2, g2, b2, a2, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, r, g, b, a, r2, g2, b2, a2, lineWidth);
            VertexRenderer.drawLine(entry, consumer, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, a, r2, g2, b2, a2, lineWidth);
        });
    }

    private static void drawLine(MatrixStack.Entry entry, VertexConsumer consumer, double x1, double y1, double z1, double x2, double y2, double z2, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2, double lineWidth) {
        Vector3f normal = new Vector3f((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();
        consumer.vertex(entry, (float)x1, (float)y1, (float)z1).color(r1, g1, b1, a1).lineWidth((float)lineWidth).normal(entry, normal);
        consumer.vertex(entry, (float)x2, (float)y2, (float)z2).color(r2, g2, b2, a2).lineWidth((float)lineWidth).normal(entry, normal);
    }

    public static void drawFilledBox(OrderedRenderCommandQueue queue, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Box box, WalksyLibColor color2, WalksyLibColor color) {
        float r = (float)color.getRed() / 255.0f;
        float g = (float)color.getGreen() / 255.0f;
        float b = (float)color.getBlue() / 255.0f;
        float a = (float)color.getAlpha() / 255.0f;
        float r2 = (float)color2.getRed() / 255.0f;
        float g2 = (float)color2.getGreen() / 255.0f;
        float b2 = (float)color2.getBlue() / 255.0f;
        float a2 = (float)color2.getAlpha() / 255.0f;
        queue.submitCustom(matrices, QUAD_RENDER_LAYER, (matrixEntry, vertexConsumer) -> VertexRenderer.drawFilledBox(matrixEntry.getPositionMatrix(), vertexConsumer, (float)box.minX, (float)box.minY, (float)box.minZ, (float)box.maxX, (float)box.maxY, (float)box.maxZ, r, g, b, a, r2, g2, b2, a2));
    }

    public static void drawFilledBox(MatrixStack matrices, VertexConsumer vertexConsumers, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        for (Direction direction : Direction.values()) {
            VertexRenderer.drawSide(matrices, vertexConsumers, direction, minX, minY, minZ, maxX, maxY, maxZ, r1, g1, b1, a1, r2, g2, b2, a2);
        }
    }

    public static void drawFilledBox(Matrix4f matrices, VertexConsumer vertexConsumers, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        for (Direction direction : Direction.values()) {
            VertexRenderer.drawSide(matrices, vertexConsumers, direction, minX, minY, minZ, maxX, maxY, maxZ, r1, g1, b1, a1, r2, g2, b2, a2);
        }
    }

    public static void drawSide(MatrixStack matrices, VertexConsumer vertexConsumers, Direction side, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        VertexRenderer.drawSide(matrices.peek().getPositionMatrix(), vertexConsumers, side, minX, minY, minZ, maxX, maxY, maxZ, r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawSide(Matrix4f matrix4f, VertexConsumer vertexConsumers, Direction side, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        switch (side) {
            case DOWN: {
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, minY, maxZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, minY, maxZ).color(r1, g1, b1, a1);
                break;
            }
            case UP: {
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, maxY, minZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, maxY, minZ).color(r2, g2, b2, a2);
                break;
            }
            case NORTH: {
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, maxY, minZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, maxY, minZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, minY, minZ).color(r1, g1, b1, a1);
                break;
            }
            case SOUTH: {
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, minY, maxZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, minY, maxZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, maxY, maxZ).color(r2, g2, b2, a2);
                break;
            }
            case WEST: {
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, minY, maxZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, minX, maxY, minZ).color(r2, g2, b2, a2);
                break;
            }
            case EAST: {
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, maxY, minZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex((Matrix4fc)matrix4f, maxX, minY, maxZ).color(r1, g1, b1, a1);
            }
        }
    }

    public static void drawArrow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d start, Vec3d end, WalksyLibColor shaftColor, WalksyLibColor arrowColor, double lineWidth, boolean arrow) {
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayers.lines());
        MatrixStack.Entry entry = matrices.peek();
        float sR = (float)shaftColor.getRed() / 255.0f;
        float sG = (float)shaftColor.getGreen() / 255.0f;
        float sB = (float)shaftColor.getBlue() / 255.0f;
        float sA = (float)shaftColor.getAlpha() / 255.0f;
        VertexRenderer.drawLine(entry, consumer, start.x, start.y, start.z, end.x, end.y, end.z, sR, sG, sB, sA, sR, sG, sB, sA, lineWidth);
        if (!arrow) {
            return;
        }
        Vec3d direction = end.subtract(start).normalize();
        Quaternionf rotation = new Quaternionf().rotationTo((Vector3fc)new Vector3f(1.0f, 0.0f, 0.0f), (Vector3fc)direction.toVector3f());
        float size = (float)MathHelper.clamp((double)(end.distanceTo(start) * 0.1), (double)0.1, (double)1.0);
        Vector3f[] headOffsets = new Vector3f[]{rotation.transform(new Vector3f(-size, size, 0.0f)), rotation.transform(new Vector3f(-size, 0.0f, size)), rotation.transform(new Vector3f(-size, -size, 0.0f)), rotation.transform(new Vector3f(-size, 0.0f, -size))};
        float aR = (float)arrowColor.getRed() / 255.0f;
        float aG = (float)arrowColor.getGreen() / 255.0f;
        float aB = (float)arrowColor.getBlue() / 255.0f;
        float aA = (float)arrowColor.getAlpha() / 255.0f;
        for (Vector3f offset : headOffsets) {
            VertexRenderer.drawLine(entry, consumer, end.x + (double)offset.x, end.y + (double)offset.y, end.z + (double)offset.z, end.x, end.y, end.z, aR, aG, aB, aA, aR, aG, aB, aA, lineWidth);
        }
    }
}
