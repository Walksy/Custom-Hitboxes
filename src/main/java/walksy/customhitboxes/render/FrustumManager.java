package walksy.customhitboxes.render;

import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;

public class FrustumManager {
    private static Frustum currentFrustum;
    public static void updateFrustum(Frustum frustum) {
        currentFrustum = frustum;
    }

    public static boolean isVisible(Box boundingBox) {
        if (currentFrustum == null) {
            return true;
        }
        return currentFrustum.isVisible(boundingBox);
    }
}
