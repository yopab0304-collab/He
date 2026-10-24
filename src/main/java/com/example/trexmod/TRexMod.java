package com.example.trexmod;

import com.example.trexmod.entity.MosasaurusEntity;
import com.example.trexmod.entity.TRexEntity;
import com.example.trexmod.registry.ModEntities;
import com.example.trexmod.registry.ModItems;
import com.example.trexmod.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;

public class TRexMod implements ModInitializer {
    public static final String MOD_ID = "trexmod";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModSounds.init();
        ModEntities.init();
        ModItems.init();

        FabricDefaultAttributeRegistry.register(ModEntities.TREX, TRexEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.MOSASAURUS, MosasaurusEntity.createAttributes());

        // ---- T-Rex: เกิดบนพื้นหญ้า ในป่า/ป่าดิบชื้น/ซาวันนา/ไทกา ----
        SpawnPlacements.register(ModEntities.TREX, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        BiomeModifications.addSpawn(
                ctx -> ctx.hasTag(BiomeTags.IS_JUNGLE) || ctx.hasTag(BiomeTags.IS_FOREST)
                        || ctx.hasTag(BiomeTags.IS_SAVANNA) || ctx.hasTag(BiomeTags.IS_TAIGA),
                MobCategory.CREATURE, ModEntities.TREX, 4, 1, 1);

        // ---- Mosasaurus: เกิดในมหาสมุทรลึก ----
        SpawnPlacements.register(ModEntities.MOSASAURUS, SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR, MosasaurusEntity::checkSpawnRules);
        BiomeModifications.addSpawn(
                ctx -> ctx.hasTag(BiomeTags.IS_DEEP_OCEAN),
                MobCategory.WATER_CREATURE, ModEntities.MOSASAURUS, 2, 1, 1);
    }
}
