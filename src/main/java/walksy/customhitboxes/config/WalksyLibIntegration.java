package walksy.customhitboxes.config;

import main.walksy.lib.api.WalksyLibApi;
import main.walksy.lib.core.config.impl.LocalConfig;

public class WalksyLibIntegration implements WalksyLibApi {

    public LocalConfig getConfig() {
        Config config = new Config();
        return config.getOrCreateConfig();
    }
}
