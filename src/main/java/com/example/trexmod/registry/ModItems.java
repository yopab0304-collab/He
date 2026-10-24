package com.example.trexmod.registry;

import com.example.trexmod.TRexMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {
    public static final ResourceKey<Item> TREX_SPAWN_EGG_KEY =
            ResourceKey.create(Registries.ITEM, TRexMod.id("trex_spawn_egg"));
    public static final Item TREX_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, TREX_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(TREX_SPAWN_EGG_KEY).spawnEgg(ModEntities.TREX)));

    public static final ResourceKey<Item> MOSASAURUS_SPAWN_EGG_KEY =
            ResourceKey.create(Registries.ITEM, TRexMod.id("mosasaurus_spawn_egg"));
    public static final Item MOSASAURUS_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, MOSASAURUS_SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(MOSASAURUS_SPAWN_EGG_KEY).spawnEgg(ModEntities.MOSASAURUS)));

    public static void init() {}
}
