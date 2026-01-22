package walksy.customhitboxes.config;

import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.LocalConfig;
import main.walksy.lib.core.config.local.Category;
import main.walksy.lib.core.config.local.Option;
import main.walksy.lib.core.config.local.OptionDescription;
import main.walksy.lib.core.config.local.builders.CategoryBuilder;
import main.walksy.lib.core.config.local.builders.LocalConfigBuilder;
import main.walksy.lib.core.config.local.options.BooleanOption;
import main.walksy.lib.core.config.local.options.ColorOption;
import main.walksy.lib.core.config.local.options.NumericalOption;
import main.walksy.lib.core.config.local.options.groups.OptionGroup;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import main.walksy.lib.core.utils.PathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Config implements WalksyLibConfig {

    public static boolean modEnabled = true;
    public static boolean enableHitboxesOnGameLoad = true;

    public static boolean playerOnlyInElytra = false;
    public static boolean playerShowSelf = true;

    private static final Map<Class<? extends Entity>, EntityEntry> ENTITY_ENTRIES = new LinkedHashMap<>();
    public static final EntityEntry OTHER_ENTRY = EntityEntry.of("Other", false);

    static {
        registerEntry(PlayerEntity.class, "Player", categoryBuilder -> {
            Option<Boolean> onlyInElytraOption = BooleanOption.createBuilder("Only In Elytra", () -> playerOnlyInElytra, playerOnlyInElytra, newValue -> playerOnlyInElytra = newValue)
                .build();
            Option<Boolean> playerShowSelfOption = BooleanOption.createBuilder("Show Self", () -> playerShowSelf, playerShowSelf, newValue -> playerShowSelf = newValue)
                .build();
            categoryBuilder.group(OptionGroup.createBuilder("Additional Options")
                .addOption(onlyInElytraOption)
                .addOption(playerShowSelfOption)
                .build());
        });
        registerEntry(ArrowEntity.class, "Arrow");
        registerEntry(ItemEntity.class, "Dropped Item");
        registerEntry(EndCrystalEntity.class, "End Crystal");
        registerEntry(ItemFrameEntity.class, "Item Frame");
        registerEntry(PotionEntity.class, "Potion");
        registerEntry(ExperienceBottleEntity.class, "Experience Bottle");
        registerEntry(ExperienceOrbEntity.class, "Experience Orb");
        registerEntry(HostileEntity.class, "Hostile Mob");
        registerEntry(PassiveEntity.class, "Passive Mob");
        registerEntry(EnderDragonEntity.class, "Ender Dragon");
    }

    private final Option<Boolean> modEnabledOption = BooleanOption.createBuilder("Mod Enabled",
            () -> modEnabled,
            modEnabled,
            newValue -> modEnabled = newValue)
        .description(OptionDescription.ofOrderedString(() -> "The main toggle for the whole mod. Turn this off to hide everything"))
        .build();

    private final Option<Boolean> enableHitboxesOnGameLoadOption = BooleanOption.createBuilder("Enable Hitboxes On Game Load",
            () -> enableHitboxesOnGameLoad,
            enableHitboxesOnGameLoad,
            newValue -> enableHitboxesOnGameLoad = newValue)
        .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
        .description(OptionDescription.ofOrderedString(() -> "If this is on, hitboxes will be active as soon as you launch the game"))
        .build();


    @Override
    public LocalConfig define() {
        LocalConfigBuilder builder = LocalConfig.createBuilder("Custom Hitboxes")
            .path(PathUtils.ofConfigDir("customhitboxes"))
            .category(Category.createBuilder("General")
                .group(OptionGroup.createBuilder("Global Options")
                    .addOption(modEnabledOption)
                    .addOption(enableHitboxesOnGameLoadOption)
                    .build())
                .build());

        for (EntityEntry entry : ENTITY_ENTRIES.values()) {
            builder.category(createEntityCategory(entry).build());
        }

        builder.category(createEntityCategory(OTHER_ENTRY).build());

        return builder.build();
    }

    private CategoryBuilder createEntityCategory(EntityEntry entry) {
        CategoryBuilder categoryBuilder = Category.createBuilder(entry.category);

        categoryBuilder.group(OptionGroup.createBuilder("General Settings")
            .addOption(BooleanOption.createBuilder("Render Hitbox",
                    () -> entry.shouldRender,
                    entry.shouldRender,
                    newValue -> entry.shouldRender = newValue)
                .availability(() -> modEnabled, "Requires 'Mod Enabled' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Whether or not to draw the hitbox for this specific entity"))
                .build())
            .build());

        categoryBuilder.group(OptionGroup.createBuilder("Bounding Box Settings")
            .addOption(BooleanOption.createBuilder("Lines",
                    () -> entry.lines,
                    entry.lines,
                    newValue -> entry.lines = newValue)
                .availability(() -> modEnabled && entry.shouldRender, "Requires 'Mod Enabled' & 'Should Render' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Shows the wireframe lines around the entity"))
                .build())
            .addOption(NumericalOption.createBuilder("Bounding Box Line Width",
                    () -> entry.boundingBoxLineWidth,
                    entry.boundingBoxLineWidth,
                    newValue -> entry.boundingBoxLineWidth = newValue)
                .values(0D, 10D, 0.1D)
                .availability(() -> modEnabled && entry.shouldRender && entry.lines, "Requires 'Mod Enabled' & 'Should Render' & 'Lines' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Change how thick the wireframe lines look"))
                .build())
            .addOption(ColorOption.createBuilder("Lines Color",
                    () -> entry.boundingBoxLineColor,
                    entry.boundingBoxLineColor,
                    newValue -> entry.boundingBoxLineColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.lines, "Requires 'Mod Enabled' & 'Should Render' & 'Lines' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Pick a custom color for the wireframe"))
                .build())
            .addOption(BooleanOption.createBuilder("Lines Gradient",
                    () -> entry.linesGradient,
                    entry.linesGradient,
                    newValue -> entry.linesGradient = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.lines, "Requires 'Lines' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Enable a color gradient for the wireframe lines"))
                .build())
            .addOption(ColorOption.createBuilder("Lines Gradient Color",
                    () -> entry.boundingBoxLineGradientColor,
                    entry.boundingBoxLineGradientColor,
                    newValue -> entry.boundingBoxLineGradientColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.lines && entry.linesGradient, "Requires 'Lines Gradient' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "The second color for the wireframe gradient"))
                .build())
            .addOption(BooleanOption.createBuilder("Lines Damage",
                    () -> entry.lineDamageColor,
                    entry.lineDamageColor,
                    newValue -> entry.lineDamageColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.lines, "Requires 'Mod Enabled' & 'Should Render' & 'Lines' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "The wireframe will flash a different color when the mob gets hit"))
                .build())
            .addOption(ColorOption.createBuilder("Lines Damage Color",
                    () -> entry.boundingBoxLineDamageColor,
                    entry.boundingBoxLineDamageColor,
                    newValue -> entry.boundingBoxLineDamageColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.lines && entry.lineDamageColor, "Requires 'Mod Enabled' & 'Should Render' & 'Lines Damage' & 'Lines' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "The specific color to show when the mob takes damage"))
                .build())
            .addOption(BooleanOption.createBuilder("Fill",
                    () -> entry.fill,
                    entry.fill,
                    newValue -> entry.fill = newValue)
                .availability(() -> modEnabled && entry.shouldRender, "Requires 'Mod Enabled' & 'Should Render' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Fills the inside of the hitbox with a see-through color"))
                .build())
            .addOption(ColorOption.createBuilder("Fill Color",
                    () -> entry.boundingBoxFillColor,
                    entry.boundingBoxFillColor,
                    newValue -> entry.boundingBoxFillColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.fill, "Requires 'Mod Enabled' & 'Should Render' & 'Fill' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Set the color and transparency for the box fill"))
                .build())
            .addOption(BooleanOption.createBuilder("Fill Gradient",
                    () -> entry.fillGradient,
                    entry.fillGradient,
                    newValue -> entry.fillGradient = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.fill, "Requires 'Fill' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Enable a color gradient for the box fill"))
                .build())
            .addOption(ColorOption.createBuilder("Fill Gradient Color",
                    () -> entry.boundingBoxFillGradientColor,
                    entry.boundingBoxFillGradientColor,
                    newValue -> entry.boundingBoxFillGradientColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.fill && entry.fillGradient, "Requires 'Fill Gradient' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "The second color for the box fill gradient"))
                .build())
            .addOption(BooleanOption.createBuilder("Fill Damage",
                    () -> entry.fillDamageColor,
                    entry.fillDamageColor,
                    newValue -> entry.fillDamageColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.fill, "Requires 'Mod Enabled' & 'Should Render' & 'Fill' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "The filled box will flash a different color when the mob gets hit"))
                .build())
            .addOption(ColorOption.createBuilder("Fill Damage Color",
                    () -> entry.boundingBoxFillDamageColor,
                    entry.boundingBoxFillDamageColor,
                    newValue -> entry.boundingBoxFillDamageColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.fill && entry.fillDamageColor, "Requires 'Mod Enabled' & 'Should Render' & 'Fill Damage' & 'Fill' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "The color to use for the fill during the damage flash"))
                .build())
            .build());

        categoryBuilder.group(OptionGroup.createBuilder("Vector Settings")
            .addOption(BooleanOption.createBuilder("Look Vector",
                    () -> entry.lookVector,
                    entry.lookVector,
                    newValue -> entry.lookVector = newValue)
                .availability(() -> modEnabled && entry.shouldRender, "Requires 'Mod Enabled' & 'Should Render' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Shows a line pointing exactly where the mob is looking"))
                .build())
            .addOption(NumericalOption.createBuilder("Look Vector Length",
                    () -> entry.lookVectorShaftLength,
                    entry.lookVectorShaftLength,
                    newValue -> entry.lookVectorShaftLength = newValue)
                .values(0D, 10D, 0.1D)
                .availability(() -> modEnabled && entry.shouldRender && entry.lookVector, "Requires 'Mod Enabled' & 'Should Render' & 'Look Vector' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "How long the look vector line should be"))
                .build())
            .addOption(NumericalOption.createBuilder("Look Vector Width",
                    () -> entry.lookVectorShaftWidth,
                    entry.lookVectorShaftWidth,
                    newValue -> entry.lookVectorShaftWidth = newValue)
                .values(0D, 10D, 0.1D)
                .availability(() -> modEnabled && entry.shouldRender && entry.lookVector, "Requires 'Mod Enabled' & 'Should Render' & 'Look Vector' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "How thick the look vector line should be"))
                .build())
            .addOption(ColorOption.createBuilder("Look Vector Shaft Color",
                    () -> entry.lookVectorShaftColor,
                    entry.lookVectorShaftColor,
                    newValue -> entry.lookVectorShaftColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.lookVector, "Requires 'Mod Enabled' & 'Should Render' & 'Look Vector' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Pick a color for the look vector line"))
                .build())
            .addOption(BooleanOption.createBuilder("Look Vector Arrow",
                    () -> entry.lookVectorArrow,
                    entry.lookVectorArrow,
                    newValue -> entry.lookVectorArrow = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.lookVector, "Requires 'Mod Enabled' & 'Should Render' & 'Look Vector' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Puts a little arrow head on the end of the look line"))
                .build())
            .addOption(ColorOption.createBuilder("Look Vector Arrow Color",
                    () -> entry.lookVectorArrowColor,
                    entry.lookVectorArrowColor,
                    newValue -> entry.lookVectorArrowColor = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.lookVectorArrow && entry.lookVector, "Requires 'Mod Enabled' & 'Should Render' & 'Look Vector Arrow' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Pick a color for the arrow head"))
                .build())
            .build());

        if (!entry.hasLimitedOptions()) {
            categoryBuilder.group(OptionGroup.createBuilder("Eye Height Settings")
                .addOption(BooleanOption.createBuilder("Eye Height Bounding Box",
                        () -> entry.eyeHeightBox,
                        entry.eyeHeightBox,
                        newValue -> entry.eyeHeightBox = newValue)
                    .availability(() -> modEnabled && entry.shouldRender, "Requires 'Mod Enabled' & 'Should Render' to be enabled")
                    .description(OptionDescription.ofOrderedString(() -> "Draws a flat box at the mob's eye level"))
                    .build())
                .addOption(BooleanOption.createBuilder("Eye Height Bounding Box Fill",
                        () -> entry.eyeHeightBoxFill,
                        entry.eyeHeightBoxFill,
                        newValue -> entry.eyeHeightBoxFill = newValue)
                    .availability(() -> modEnabled && entry.shouldRender && entry.eyeHeightBox, "Requires 'Mod Enabled' & 'Should Render' & 'Eye Height Bounding Box' to be enabled")
                    .description(OptionDescription.ofOrderedString(() -> "Fills the eye level box with color"))
                    .build())
                .addOption(NumericalOption.createBuilder("Eye Height Bounding Box Line Width",
                        () -> entry.eyeHeightBoxLineWidth,
                        entry.eyeHeightBoxLineWidth,
                        newValue -> entry.eyeHeightBoxLineWidth = newValue)
                    .values(0D, 10D, 0.1D)
                    .availability(() -> modEnabled && entry.shouldRender && entry.eyeHeightBox && !entry.eyeHeightBoxFill, "Requires 'Mod Enabled' & 'Should Render' & 'Eye Height Bounding Box' to be enabled & 'Eye Height Bounding Box Fill' to be disabled")
                    .description(OptionDescription.ofOrderedString(() -> "How thick the lines for the eye height box should be"))
                    .build())
                .addOption(ColorOption.createBuilder("Eye Height Bounding Box Color",
                        () -> entry.eyeHeightBoxColor,
                        entry.eyeHeightBoxColor,
                        newValue -> entry.eyeHeightBoxColor = newValue)
                    .availability(() -> modEnabled && entry.shouldRender && entry.eyeHeightBox, "Requires 'Mod Enabled' & 'Should Render' & 'Eye Height Bounding Box' to be enabled")
                    .description(OptionDescription.ofOrderedString(() -> "Pick a color for the eye level box"))
                    .build())
                .build());
        }

        categoryBuilder.group(OptionGroup.createBuilder("Vanish Settings")
            .addOption(BooleanOption.createBuilder("Vanish When Close",
                    () -> entry.vanishWhenClose,
                    entry.vanishWhenClose,
                    newValue -> entry.vanishWhenClose = newValue)
                .availability(() -> modEnabled && entry.shouldRender, "Requires 'Mod Enabled' & 'Should Render' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Makes the hitbox disappear when you get right next to it"))
                .build())
            .addOption(NumericalOption.<Double>createBuilder("Vanish Distance",
                    () -> entry.vanishDistance,
                    entry.vanishDistance,
                    newValue -> entry.vanishDistance = newValue)
                .values(0D, 30D, 0.5D)
                .availability(() -> modEnabled && entry.shouldRender && entry.vanishWhenClose, "Requires 'Vanish When Close' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "How close you need to be before the hitbox vanishes"))
                .build())
            .addOption(BooleanOption.createBuilder("Vanish Fade",
                    () -> entry.vanishFade,
                    entry.vanishFade,
                    newValue -> entry.vanishFade = newValue)
                .availability(() -> modEnabled && entry.shouldRender && entry.vanishWhenClose, "Requires 'Vanish When Close' to be enabled")
                .description(OptionDescription.ofOrderedString(() -> "Makes the hitbox smoothly fade away instead of just cutting out instantly"))
                .build())
            .build());

        entry.applyAdditional(categoryBuilder);
        return categoryBuilder;
    }

    public static void tick() {
        for (EntityEntry entry : ENTITY_ENTRIES.values()) {
            entry.tick();
        }
        OTHER_ENTRY.tick();
    }

    public static boolean shouldRender(Entity entity) {
        EntityEntry entry = getEntry(entity);
        if (entry == null) return false;
        if (!entry.shouldRender) return false;
        if (entry.vanishWhenClose) {
            Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
            double distanceSq = entity.getEntityPos().squaredDistanceTo(cameraPos);
            if (distanceSq < (entry.vanishDistance * entry.vanishDistance)) {
                return false;
            }
        }
        if (entity instanceof PlayerEntity player) {
            if (player == MinecraftClient.getInstance().player && !playerShowSelf) {
                return false;
            }
            if (playerOnlyInElytra) {
                boolean wearingElytra = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isOf(Items.ELYTRA);
                return wearingElytra && player.isGliding();
            }
        }
        return true;
    }

    private static void registerEntry(Class<? extends Entity> entity, String category) {
        boolean isLiving = LivingEntity.class.isAssignableFrom(entity);
        ENTITY_ENTRIES.put(entity, EntityEntry.of(category, !isLiving));
    }

    private static void registerEntry(Class<? extends Entity> entity, String category, Consumer<CategoryBuilder> additional) {
        boolean isLiving = LivingEntity.class.isAssignableFrom(entity);
        ENTITY_ENTRIES.put(entity, EntityEntry.of(category, !isLiving).additionalOptions(additional));
    }

    public static EntityEntry getEntry(Entity entity) {
        if (entity == null) return OTHER_ENTRY;
        for (Map.Entry<Class<? extends Entity>, EntityEntry> entry : ENTITY_ENTRIES.entrySet()) {
            if (entry.getKey().isInstance(entity)) {
                return entry.getValue();
            }
        }
        return OTHER_ENTRY;
    }

    public static class EntityEntry {
        final boolean limitedOptions;
        public final String category;
        public boolean shouldRender = true;
        public boolean fill = false;
        public boolean fillGradient = false;
        public boolean fillDamageColor = false;
        public boolean lines = true;
        public boolean linesGradient = false;
        public boolean lineDamageColor = false;
        public double boundingBoxLineWidth = 2.5D;
        public boolean lookVector = false;
        public double lookVectorShaftLength = 2.0D;
        public double lookVectorShaftWidth = 2.5D;
        public boolean lookVectorArrow = true;
        public boolean eyeHeightBox = false;
        public boolean eyeHeightBoxFill = false;
        public double eyeHeightBoxLineWidth = 2.5;
        public WalksyLibColor eyeHeightBoxColor = new WalksyLibColor(255, 255, 255);
        public WalksyLibColor boundingBoxLineColor = new WalksyLibColor(255, 255, 255);
        public WalksyLibColor boundingBoxLineGradientColor = new WalksyLibColor(255, 255, 255);
        public WalksyLibColor boundingBoxFillColor = new WalksyLibColor(255, 255, 255);
        public WalksyLibColor boundingBoxFillGradientColor = new WalksyLibColor(255, 255, 255);
        public WalksyLibColor boundingBoxLineDamageColor = new WalksyLibColor(255, 255, 255);
        public WalksyLibColor boundingBoxFillDamageColor = new WalksyLibColor(255, 255, 255);
        public WalksyLibColor lookVectorShaftColor = new WalksyLibColor(255, 255, 255);
        public WalksyLibColor lookVectorArrowColor = new WalksyLibColor(255, 255, 255);
        public boolean vanishWhenClose = false;
        public boolean vanishFade = false;
        public double vanishDistance = 5D;

        private Consumer<CategoryBuilder> additionalOptions = (builder) -> {};

        public void tick() {
            this.boundingBoxFillColor.tick();
            this.boundingBoxFillGradientColor.tick();
            this.boundingBoxLineColor.tick();
            this.boundingBoxLineGradientColor.tick();
            this.boundingBoxLineDamageColor.tick();
            this.boundingBoxFillDamageColor.tick();
            this.lookVectorArrowColor.tick();
            this.lookVectorShaftColor.tick();
            this.eyeHeightBoxColor.tick();
        }

        private EntityEntry(String category, boolean limitedOptions) {
            this.category = category;
            this.limitedOptions = limitedOptions;
        }

        public static EntityEntry of(String category, boolean limitedOptions) {
            return new EntityEntry(category, limitedOptions);
        }

        public boolean hasLimitedOptions() {
            return this.limitedOptions;
        }

        public EntityEntry additionalOptions(Consumer<CategoryBuilder> additionalOptions) {
            this.additionalOptions = additionalOptions;
            return this;
        }

        public void applyAdditional(CategoryBuilder builder) {
            this.additionalOptions.accept(builder);
        }
    }
}
