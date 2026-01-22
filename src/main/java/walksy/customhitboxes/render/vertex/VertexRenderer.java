package walksy.customhitboxes.render.vertex;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value = EnvType.CLIENT)
public class VertexRenderer {

    private static final RenderPipeline QUAD_PIPELINE = RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
        .withLocation("pipeline/walksy_quad_pipeline")
        .withDepthWrite(true)
        .withCull(true)
        .build();

    private static final RenderLayer QUAD_RENDER_LAYER = RenderLayer.of("walksy_quad_render_layer", RenderSetup.builder(QUAD_PIPELINE)
        .translucent()
        .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
        .build());

    public static void drawOutlineBox(OrderedRenderCommandQueue queue, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Box box, WalksyLibColor color2, WalksyLibColor color, double lineWidth) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        float r2 = color2.getRed() / 255f;
        float g2 = color2.getGreen() / 255f;
        float b2 = color2.getBlue() / 255f;
        float a2 = color2.getAlpha() / 255f;

        queue.submitCustom(matrices, RenderLayers.lines(), (entry, consumer) -> {
            drawLine(entry, consumer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, r, g, b, a, r, g, b, a, lineWidth);
            drawLine(entry, consumer, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, r, g, b, a, r, g, b, a, lineWidth);
            drawLine(entry, consumer, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, r, g, b, a, r, g, b, a, lineWidth);
            drawLine(entry, consumer, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, r, g, b, a, r, g, b, a, lineWidth);

            drawLine(entry, consumer, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, r2, g2, b2, a2, r2, g2, b2, a2, lineWidth);
            drawLine(entry, consumer, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, r2, g2, b2, a2, r2, g2, b2, a2, lineWidth);
            drawLine(entry, consumer, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, r2, g2, b2, a2, r2, g2, b2, a2, lineWidth);
            drawLine(entry, consumer, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, r2, g2, b2, a2, r2, g2, b2, a2, lineWidth);

            drawLine(entry, consumer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, r, g, b, a, r2, g2, b2, a2, lineWidth);
            drawLine(entry, consumer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, a, r2, g2, b2, a2, lineWidth);
            drawLine(entry, consumer, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, r, g, b, a, r2, g2, b2, a2, lineWidth);
            drawLine(entry, consumer, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, a, r2, g2, b2, a2, lineWidth);
        });
    }

    private static void drawLine(MatrixStack.Entry entry, VertexConsumer consumer, double x1, double y1, double z1, double x2, double y2, double z2, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2, double lineWidth) {
        Vector3f normal = new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1)).normalize();
        consumer.vertex(entry, (float) x1, (float) y1, (float) z1).color(r1, g1, b1, a1).lineWidth((float) lineWidth).normal(entry, normal);
        consumer.vertex(entry, (float) x2, (float) y2, (float) z2).color(r2, g2, b2, a2).lineWidth((float) lineWidth).normal(entry, normal);
    }

    public static void drawFilledBox(OrderedRenderCommandQueue queue, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Box box, WalksyLibColor color2, WalksyLibColor color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        float r2 = color2.getRed() / 255f;
        float g2 = color2.getGreen() / 255f;
        float b2 = color2.getBlue() / 255f;
        float a2 = color2.getAlpha() / 255f;
        queue.submitCustom(matrices, QUAD_RENDER_LAYER, (matrixEntry, vertexConsumer) -> {
            drawFilledBox(matrixEntry.getPositionMatrix(), vertexConsumer, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, r, g, b, a, r2, g2, b2, a2);
        });
    }

    public static void drawFilledBox(MatrixStack matrices, VertexConsumer vertexConsumers, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        for (Direction direction : Direction.values()) {
            drawSide(matrices, vertexConsumers, direction, minX, minY, minZ, maxX, maxY, maxZ, r1, g1, b1, a1, r2, g2, b2, a2);
        }
    }

    public static void drawFilledBox(Matrix4f matrices, VertexConsumer vertexConsumers, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        for (Direction direction : Direction.values()) {
            drawSide(matrices, vertexConsumers, direction, minX, minY, minZ, maxX, maxY, maxZ, r1, g1, b1, a1, r2, g2, b2, a2);
        }
    }

    public static void drawSide(MatrixStack matrices, VertexConsumer vertexConsumers, Direction side, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        drawSide(matrices.peek().getPositionMatrix(), vertexConsumers, side, minX, minY, minZ, maxX, maxY, maxZ, r1, g1, b1, a1, r2, g2, b2, a2);
    }

    public static void drawSide(Matrix4f matrix4f, VertexConsumer vertexConsumers, Direction side, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
        switch (side) {
            case DOWN -> {
                vertexConsumers.vertex(matrix4f, minX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, maxX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, maxX, minY, maxZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, minX, minY, maxZ).color(r1, g1, b1, a1);
            }
            case UP -> {
                vertexConsumers.vertex(matrix4f, minX, maxY, minZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, minX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, maxX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, maxX, maxY, minZ).color(r2, g2, b2, a2);
            }
            case NORTH -> {
                vertexConsumers.vertex(matrix4f, minX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, minX, maxY, minZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, maxX, maxY, minZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, maxX, minY, minZ).color(r1, g1, b1, a1);
            }
            case SOUTH -> {
                vertexConsumers.vertex(matrix4f, minX, minY, maxZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, maxX, minY, maxZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, maxX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, minX, maxY, maxZ).color(r2, g2, b2, a2);
            }
            case WEST -> {
                vertexConsumers.vertex(matrix4f, minX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, minX, minY, maxZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, minX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, minX, maxY, minZ).color(r2, g2, b2, a2);
            }
            case EAST -> {
                vertexConsumers.vertex(matrix4f, maxX, minY, minZ).color(r1, g1, b1, a1);
                vertexConsumers.vertex(matrix4f, maxX, maxY, minZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, maxX, maxY, maxZ).color(r2, g2, b2, a2);
                vertexConsumers.vertex(matrix4f, maxX, minY, maxZ).color(r1, g1, b1, a1);
            }
        }
    }

    public static void drawArrow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d start, Vec3d end, WalksyLibColor shaftColor, WalksyLibColor arrowColor, double lineWidth, boolean arrow) {
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayers.lines());
        MatrixStack.Entry entry = matrices.peek();

        float sR = shaftColor.getRed() / 255f;
        float sG = shaftColor.getGreen() / 255f;
        float sB = shaftColor.getBlue() / 255f;
        float sA = shaftColor.getAlpha() / 255f;

        drawLine(entry, consumer, start.x, start.y, start.z, end.x, end.y, end.z, sR, sG, sB, sA, sR, sG, sB, sA, lineWidth);

        if (!arrow) return;
        Vec3d direction = end.subtract(start).normalize();
        Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(1.0f, 0.0f, 0.0f), direction.toVector3f());

        float size = (float) MathHelper.clamp(end.distanceTo(start) * 0.1, 0.1, 1.0);

        Vector3f[] headOffsets = new Vector3f[]{
            rotation.transform(new Vector3f(-size, size, 0.0f)),
            rotation.transform(new Vector3f(-size, 0.0f, size)),
            rotation.transform(new Vector3f(-size, -size, 0.0f)),
            rotation.transform(new Vector3f(-size, 0.0f, -size))
        };

        float aR = arrowColor.getRed() / 255f;
        float aG = arrowColor.getGreen() / 255f;
        float aB = arrowColor.getBlue() / 255f;
        float aA = arrowColor.getAlpha() / 255f;

        for (Vector3f offset : headOffsets) {
            drawLine(entry, consumer,
                end.x + offset.x, end.y + offset.y, end.z + offset.z,
                end.x, end.y, end.z,
                aR, aG, aB, aA, aR, aG, aB, aA, lineWidth
            );
        }
    }
}
