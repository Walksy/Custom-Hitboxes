package walksy.customhitboxes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import walksy.customhitboxes.config.Config;
import walksy.customhitboxes.render.vertex.HitboxRenderer;

public class CustomHitboxes
        implements ModInitializer {
    public void onInitialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (Config.modEnabled) {
                HitboxRenderer vertexCollective = new HitboxRenderer(context);
                vertexCollective.render();
            }
        });
    }
}

