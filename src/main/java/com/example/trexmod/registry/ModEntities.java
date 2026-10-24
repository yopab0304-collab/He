package com.example.trexmod.registry;

import com.example.trexmod.TRexMod;
import com.example.trexmod.entity.MosasaurusEntity;
import com.example.trexmod.entity.TRexEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final ResourceKey<EntityType<?>> TREX_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, TRexMod.id("trex"));

    public static final EntityType<TRexEntity> TREX = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, TREX_KEY,
            EntityType.Builder.of(TRexEntity::new, MobCategory.CREATURE)
                    .sized(2.6f, 3.6f)
                    .eyeHeight(3.2f)
                    .clientTrackingRange(10)
                    .build(TREX_KEY));

    public static final ResourceKey<EntityType<?>> MOSASAURUS_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, TRexMod.id("mosasaurus"));

    public static final EntityType<MosasaurusEntity> MOSASAURUS = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, MOSASAURUS_KEY,
            EntityType.Builder.of(MosasaurusEntity::new, MobCategory.WATER_CREATURE)
                    .sized(3.0f, 2.0f)
                    .eyeHeight(1.1f)
                    .clientTrackingRange(12)
                    .build(MOSASAURUS_KEY));

    public static void init() {}
}
