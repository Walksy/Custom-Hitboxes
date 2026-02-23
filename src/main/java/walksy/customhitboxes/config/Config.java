package walksy.customhitboxes.config;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.LocalConfig;
import main.walksy.lib.core.config.local.*;
import main.walksy.lib.core.config.local.builders.*;
import main.walksy.lib.core.config.local.options.*;
import main.walksy.lib.core.config.local.options.groups.OptionGroup;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import main.walksy.lib.core.manager.Team;
import main.walksy.lib.core.manager.WalksyLibTeamManager;
import main.walksy.lib.core.utils.PathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
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

public class Config implements WalksyLibConfig {

    public static boolean modEnabled = true;
    public static boolean enableHitboxesOnGameLoad = true;
    public static boolean playerOnlyInElytra = false;
    public static boolean playerShowSelf = true;

    public static final Map<Team, EntityEntry> TEAM_ENTRIES = new EnumMap<>(Team.class);
    private static final Map<Class<? extends Entity>, EntityEntry> ENTITY_ENTRIES = new LinkedHashMap<>();
    public static final EntityEntry OTHER_ENTRY = EntityEntry.of("Other", false);

    @Override
    public LocalConfig define() {
        LocalConfigBuilder builder = LocalConfig.createBuilder("Custom Hitboxes")
                .path(PathUtils.ofConfigDir("customhitboxes"));

        CategoryBuilder generalCat = Category.createBuilder("General");

        generalCat.group(OptionGroup.createBuilder("Global Options")
                .addOption(newBool("Mod Enabled", () -> modEnabled, v -> modEnabled = v,
                        "Main toggle for the mod. Turn this off to hide everything").build())
                .addOption(newBool("Enable Hitboxes On Game Load", () -> enableHitboxesOnGameLoad, v -> enableHitboxesOnGameLoad = v,
                        "Hitboxes will be active as soon as you launch the game", () -> modEnabled).build())
                .build());

        OptionGroup.Builder teamGroup = OptionGroup.createBuilder("Registered Players");
        WalksyLibTeamManager.getTeams().forEach((name, team) -> {
            teamGroup.addOption(EnumOption.createBuilder(name,
                    () -> WalksyLibTeamManager.getTeams().getOrDefault(name, Team.None),
                    Team.None, newTeam -> WalksyLibTeamManager.getTeams().put(name, newTeam), Team.class).build());
        });
        generalCat.group(teamGroup.build());
        builder.category(generalCat.build());

        TEAM_ENTRIES.values().forEach(e -> builder.category(createEntityCategory(e).build()));
        ENTITY_ENTRIES.values().forEach(e -> builder.category(createEntityCategory(e).build()));
        builder.category(createEntityCategory(OTHER_ENTRY).build());

        return builder.build();
    }

    private CategoryBuilder createEntityCategory(EntityEntry entry) {
        CategoryBuilder cat = Category.createBuilder(entry.category);

        cat.group(OptionGroup.createBuilder("General Settings")
                .addOption(newBool("Render Hitbox", () -> entry.shouldRender, v -> entry.shouldRender = v, "Draw hitbox for this entity", () -> modEnabled).build())
                .build());

        Supplier<Boolean> linesActive = () -> modEnabled && entry.shouldRender && entry.lines;
        OptionGroup.Builder boxLines = OptionGroup.createBuilder("Bounding Box Lines")
                .addOption(newBool("Lines", () -> entry.lines, v -> entry.lines = v, "Show wireframe", () -> modEnabled && entry.shouldRender).build())
                .addOption(newNum("Line Width", () -> entry.boundingBoxLineWidth, v -> entry.boundingBoxLineWidth = v.doubleValue(), 0, 10, 0.1, linesActive).build())
                .addOption(newColor("Lines Color", () -> entry.boundingBoxLineColor, v -> entry.boundingBoxLineColor = v, linesActive).build())
                .addOption(newBool("Lines Gradient", () -> entry.linesGradient, v -> entry.linesGradient = v, "Enable wireframe gradient", linesActive).build())
                .addOption(newColor("Lines Gradient Color", () -> entry.boundingBoxLineGradientColor, v -> entry.boundingBoxLineGradientColor = v, () -> linesActive.get() && entry.linesGradient).build())
                .addOption(newBool("Lines Damage", () -> entry.lineDamageColor, v -> entry.lineDamageColor = v, "Flash on damage", linesActive).build())
                .addOption(newColor("Lines Damage Color", () -> entry.boundingBoxLineDamageColor, v -> entry.boundingBoxLineDamageColor = v, () -> linesActive.get() && entry.lineDamageColor).build())
                .addOption(newBool("Lines In Range", () -> entry.lineInRangeColor, v -> entry.lineInRangeColor = v, "", linesActive).build())
                .addOption(newColor("Lines In Range Color", () -> entry.boundingBoxLineInRangeColor, v -> entry.boundingBoxLineInRangeColor = v, () -> linesActive.get() && entry.lineInRangeColor).build())
                .addOption(newBool("Lines Targeting", () -> entry.lineTargetColor, v -> entry.lineTargetColor = v, "", linesActive).build())
                .addOption(newColor("Lines Targeting Color", () -> entry.boundingBoxLineTargetColor, v -> entry.boundingBoxLineTargetColor = v, () -> linesActive.get() && entry.lineTargetColor).build());
        cat.group(boxLines.build());

        Supplier<Boolean> fillActive = () -> modEnabled && entry.shouldRender && entry.fill;
        OptionGroup.Builder boxFill = OptionGroup.createBuilder("Bounding Box Fill")
                .addOption(newBool("Fill", () -> entry.fill, v -> entry.fill = v, "Fills the inside", () -> modEnabled && entry.shouldRender).build())
                .addOption(newColor("Fill Color", () -> entry.boundingBoxFillColor, v -> entry.boundingBoxFillColor = v, fillActive).build())
                .addOption(newBool("Fill Gradient", () -> entry.fillGradient, v -> entry.fillGradient = v, "Enable fill gradient", fillActive).build())
                .addOption(newColor("Fill Gradient Color", () -> entry.boundingBoxFillGradientColor, v -> entry.boundingBoxFillGradientColor = v, () -> fillActive.get() && entry.fillGradient).build())
                .addOption(newBool("Fill Damage", () -> entry.fillDamageColor, v -> entry.fillDamageColor = v, "Fill flash on damage", fillActive).build())
                .addOption(newColor("Fill Damage Color", () -> entry.boundingBoxFillDamageColor, v -> entry.boundingBoxFillDamageColor = v, () -> fillActive.get() && entry.fillDamageColor).build())
                .addOption(newBool("Fill In Range", () -> entry.fillInRangeColor, v -> entry.fillInRangeColor = v, "", fillActive).build())
                .addOption(newColor("Fill In Range Color", () -> entry.boundingBoxFillInRangeColor, v -> entry.boundingBoxFillInRangeColor = v, () -> fillActive.get() && entry.fillInRangeColor).build())
                .addOption(newBool("Fill Targeting", () -> entry.fillTargetColor, v -> entry.fillTargetColor = v, "", fillActive).build())
                .addOption(newColor("Fill Targeting Color", () -> entry.boundingBoxFillTargetColor, v -> entry.boundingBoxFillTargetColor = v, () -> fillActive.get() && entry.fillTargetColor).build());
        cat.group(boxFill.build());

        Supplier<Boolean> vecActive = () -> modEnabled && entry.shouldRender && entry.lookVector;
        cat.group(OptionGroup.createBuilder("Vector Settings")
                .addOption(newBool("Look Vector", () -> entry.lookVector, v -> entry.lookVector = v, "Show rotation vector", () -> modEnabled && entry.shouldRender).build())
                .addOption(newNum("Length", () -> entry.lookVectorShaftLength, v -> entry.lookVectorShaftLength = v.doubleValue(), 0, 10, 0.1, vecActive).build())
                .addOption(newNum("Width", () -> entry.lookVectorShaftWidth, v -> entry.lookVectorShaftWidth = v.doubleValue(), 0, 10, 0.1, vecActive).build())
                .addOption(newColor("Shaft Color", () -> entry.lookVectorShaftColor, v -> entry.lookVectorShaftColor = v, vecActive).build())
                .addOption(newBool("Arrow", () -> entry.lookVectorArrow, v -> entry.lookVectorArrow = v, "Show arrow head", vecActive).build())
                .addOption(newColor("Arrow Color", () -> entry.lookVectorArrowColor, v -> entry.lookVectorArrowColor = v, () -> vecActive.get() && entry.lookVectorArrow).build())
                .build());

        if (!entry.hasLimitedOptions()) {
            Supplier<Boolean> eyeActive = () -> modEnabled && entry.shouldRender && entry.eyeHeightBox;
            cat.group(OptionGroup.createBuilder("Eye Height Settings")
                    .addOption(newBool("Eye Box", () -> entry.eyeHeightBox, v -> entry.eyeHeightBox = v, "Draw eye level box", () -> modEnabled && entry.shouldRender).build())
                    .addOption(newBool("Fill", () -> entry.eyeHeightBoxFill, v -> entry.eyeHeightBoxFill = v, "Fill eye box", eyeActive).build())
                    .addOption(newNum("Line Width", () -> entry.eyeHeightBoxLineWidth, v -> entry.eyeHeightBoxLineWidth = v.doubleValue(), 0, 10, 0.1, () -> eyeActive.get() && !entry.eyeHeightBoxFill).build())
                    .addOption(newColor("Color", () -> entry.eyeHeightBoxColor, v -> entry.eyeHeightBoxColor = v, eyeActive).build())
                    .build());
        }

        cat.group(OptionGroup.createBuilder("Vanish Settings")
                .addOption(newBool("Vanish When Close", () -> entry.vanishWhenClose, v -> entry.vanishWhenClose = v, "Disappear near camera", () -> modEnabled && entry.shouldRender).build())
                .addOption(newNum("Vanish Distance", () -> entry.vanishDistance, v -> entry.vanishDistance = v.doubleValue(), 0, 30, 0.5, () -> modEnabled && entry.shouldRender && entry.vanishWhenClose).build())
                .addOption(newBool("Fade", () -> entry.vanishFade, v -> entry.vanishFade = v, "Smooth fade effect", () -> modEnabled && entry.shouldRender && entry.vanishWhenClose).build())
                .build());

        entry.applyAdditional(cat);
        return cat;
    }

    private BooleanOption newBool(String name, Supplier<Boolean> get, Consumer<Boolean> set, String desc) {
        return newBool(name, get, set, desc, () -> true);
    }

    private BooleanOption newBool(String name, Supplier<Boolean> get, Consumer<Boolean> set, String desc, Supplier<Boolean> avail) {
        return BooleanOption.createBuilder(name, get, get.get(), set)
                .availability(avail, "Conditions not met")
                .description(OptionDescription.ofOrderedString(() -> desc));
    }

    private NumericalOption newNum(String name, Supplier<Number> get, Consumer<Number> set, double min, double max, double step, Supplier<Boolean> avail) {
        return NumericalOption.createBuilder(name, get, get.get(), set)
                .values(min, max, step).availability(avail, "Conditions not met");
    }

    private ColorOption newColor(String name, Supplier<WalksyLibColor> get, Consumer<WalksyLibColor> set, Supplier<Boolean> avail) {
        return (ColorOption) ColorOption.createBuilder(name, get, get.get(), set).availability(avail, "Conditions not met");
    }


    static {
        for (Team team : Team.values()) {
            if (team != Team.None) TEAM_ENTRIES.put(team, EntityEntry.of(team.name().replace("_", ""), false));
        }

        registerEntry(PlayerEntity.class, "Player", cat -> cat.group(OptionGroup.createBuilder("Additional Options")
                .addOption(BooleanOption.createBuilder("Only In Elytra", () -> playerOnlyInElytra, playerOnlyInElytra, v -> playerOnlyInElytra = v).build())
                .addOption(BooleanOption.createBuilder("Show Self", () -> playerShowSelf, playerShowSelf, v -> playerShowSelf = v).build())
                .build()));

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

    private static void registerEntry(Class<? extends Entity> clazz, String name) { registerEntry(clazz, name, null); }
    private static void registerEntry(Class<? extends Entity> clazz, String name, Consumer<CategoryBuilder> extra) {
        EntityEntry entry = EntityEntry.of(name, !LivingEntity.class.isAssignableFrom(clazz));
        if (extra != null) entry.additionalOptions(extra);
        ENTITY_ENTRIES.put(clazz, entry);
    }

    public static EntityEntry getEntry(Entity entity) {
        if (entity == null) return OTHER_ENTRY;
        if (entity instanceof PlayerEntity) {
            Team t = WalksyLibTeamManager.getPlayerTeam(entity.getName().getString());
            if (t != null && t != Team.None) return TEAM_ENTRIES.get(t);
        }
        return ENTITY_ENTRIES.entrySet().stream()
                .filter(e -> e.getKey().isInstance(entity))
                .map(Map.Entry::getValue).findFirst().orElse(OTHER_ENTRY);
    }

    public static void tick() {
        ENTITY_ENTRIES.values().forEach(EntityEntry::tick);
        TEAM_ENTRIES.values().forEach(EntityEntry::tick);
        OTHER_ENTRY.tick();
    }

    public static boolean shouldRender(Entity entity) {
        EntityEntry entry = getEntry(entity);
        if (entry == null || !entry.shouldRender || !modEnabled) return false;

        if (entry.vanishWhenClose) {
            Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
            if (entity.getEntityPos().squaredDistanceTo(cameraPos) < entry.vanishDistance * entry.vanishDistance) {
                return false;
            }
        }

        if (entity instanceof PlayerEntity player) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (player == client.player && !playerShowSelf) return false;
            if (playerOnlyInElytra) {
                return player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && player.isGliding();
            }
        }

        return !entity.isInvisible();
    }

    public static class EntityEntry {
        public final String category;
        private final boolean limitedOptions;
        public boolean shouldRender = true, lines = true, fill = false, linesGradient, fillGradient, lineDamageColor, fillDamageColor,
                lineInRangeColor, fillInRangeColor, lineTargetColor, fillTargetColor, lookVector, lookVectorArrow = true,
                eyeHeightBox, eyeHeightBoxFill, vanishWhenClose, vanishFade;

        public double boundingBoxLineWidth = 2.5, lookVectorShaftLength = 2.0, lookVectorShaftWidth = 2.5, vanishDistance = 5.0, eyeHeightBoxLineWidth = 2.5;

        public WalksyLibColor boundingBoxLineColor = new WalksyLibColor(255, 255, 255), boundingBoxLineGradientColor = new WalksyLibColor(255, 255, 255),
                boundingBoxFillColor = new WalksyLibColor(255, 255, 255), boundingBoxFillGradientColor = new WalksyLibColor(255, 255, 255),
                boundingBoxLineDamageColor = new WalksyLibColor(255, 255, 255), boundingBoxFillDamageColor = new WalksyLibColor(255, 255, 255),
                lookVectorShaftColor = new WalksyLibColor(255, 255, 255), lookVectorArrowColor = new WalksyLibColor(255, 255, 255),
                eyeHeightBoxColor = new WalksyLibColor(255, 255, 255), boundingBoxLineInRangeColor = new WalksyLibColor(255, 255, 255),
                boundingBoxFillInRangeColor = new WalksyLibColor(255, 255, 255), boundingBoxLineTargetColor = new WalksyLibColor(255, 255, 255),
                boundingBoxFillTargetColor = new WalksyLibColor(255, 255, 255);

        private Consumer<CategoryBuilder> extra = b -> {};
        private EntityEntry(String c, boolean l) { this.category = c; this.limitedOptions = l; }
        public static EntityEntry of(String c, boolean l) { return new EntityEntry(c, l); }
        public boolean hasLimitedOptions() { return limitedOptions; }
        public EntityEntry additionalOptions(Consumer<CategoryBuilder> a) { this.extra = a; return this; }
        public void applyAdditional(CategoryBuilder b) { extra.accept(b); }

        public void tick() {
            List.of(boundingBoxLineColor, boundingBoxLineGradientColor, boundingBoxFillColor, boundingBoxFillGradientColor,
                    boundingBoxLineDamageColor, boundingBoxFillDamageColor, lookVectorShaftColor, lookVectorArrowColor,
                    eyeHeightBoxColor, boundingBoxLineInRangeColor, boundingBoxFillInRangeColor,
                    boundingBoxLineTargetColor, boundingBoxFillTargetColor).forEach(WalksyLibColor::tick);
        }
    }
}