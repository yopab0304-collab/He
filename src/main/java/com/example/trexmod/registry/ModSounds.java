package com.example.trexmod.registry;

import com.example.trexmod.TRexMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    // T-Rex
    public static final SoundEvent ROAR    = reg("entity.trex.roar");
    public static final SoundEvent AMBIENT = reg("entity.trex.ambient");
    public static final SoundEvent HURT    = reg("entity.trex.hurt");
    public static final SoundEvent DEATH   = reg("entity.trex.death");
    public static final SoundEvent STEP    = reg("entity.trex.step");
    public static final SoundEvent TAME    = reg("entity.trex.tame");
    public static final SoundEvent BITE    = reg("entity.trex.bite");

    // Mosasaurus
    public static final SoundEvent MOSA_ROAR    = reg("entity.mosasaurus.roar");
    public static final SoundEvent MOSA_AMBIENT = reg("entity.mosasaurus.ambient");
    public static final SoundEvent MOSA_HURT    = reg("entity.mosasaurus.hurt");
    public static final SoundEvent MOSA_DEATH   = reg("entity.mosasaurus.death");
    public static final SoundEvent MOSA_BITE    = reg("entity.mosasaurus.bite");
    public static final SoundEvent MOSA_TAME    = reg("entity.mosasaurus.tame");

    private static SoundEvent reg(String name) {
        Identifier id = TRexMod.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {}
}
