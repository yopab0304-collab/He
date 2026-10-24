package com.example.trexmod.entity;

import com.example.trexmod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class TRexEntity extends TamableAnimal {

    /** "ปลาตัวใหญ่" ที่ใช้เลี้ยงทีเร็กซ์ให้เชื่อง = ปลาแซลมอนดิบ (เปลี่ยนได้ตรงนี้) */
    private static boolean isBigFish(ItemStack stack) {
        return stack.is(Items.SALMON);
    }

    private int roarCooldown = 0;
    /** ตัวนับแอนิเมชันคำราม (ฝั่งไคลเอนต์ใช้วาดท่าคำราม) */
    private int roarAnim = 0;
    public static final int ROAR_ANIM_TICKS = 40;

    public float getRoarProgress(float partialTick) {
        return Math.max(0f, roarAnim - partialTick) / ROAR_ANIM_TICKS;
    }

    public TRexEntity(EntityType<? extends TRexEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1, 10.0f, 3.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        // นิสัยก้าวร้าว: ตัวป่า (ยังไม่เชื่อง) จะวิ่งไล่ผู้เล่นและสัตว์อื่นทันที
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !this.isTame()));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, 10, true, false,
                (target, level) -> !this.isTame() && !(target instanceof TRexEntity)));
        // ตัวที่เชื่องแล้วจะปกป้องเจ้าของจากมอนสเตอร์
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                (target, level) -> this.isTame()));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (isBigFish(stack)) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    if (this.random.nextInt(4) == 0) {          // โอกาสเชื่อง 25% ต่อปลา 1 ตัว
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.setOrderedToSit(false);
                        this.level().broadcastEntityEvent(this, (byte) 7);   // หัวใจ
                        this.playSound(ModSounds.TAME, 1.6f, 1.0f);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);   // ควัน = ไม่สำเร็จ
                        if (this.random.nextInt(3) == 0) {      // บางทีโกรธแล้วหันมากัดคนให้ปลา
                            this.setTarget(player);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // เชื่องแล้ว
        if (this.isOwnedBy(player)) {
            if (isBigFish(stack) && this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    this.heal(15.0f);
                }
                return InteractionResult.SUCCESS;
            }
            if (!this.level().isClientSide()) {       // คลิกมือเปล่า = นั่ง/ยืน
                this.setOrderedToSit(!this.isOrderedToSit());
                this.navigation.stop();
                this.setTarget(null);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && this.getTarget() == null && !this.level().isClientSide() && this.roarCooldown <= 0) {
            this.playSound(ModSounds.ROAR, 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
            this.level().broadcastEntityEvent(this, (byte) 10);   // สั่งเล่นท่าคำราม
            this.roarCooldown = 100;
        }
        super.setTarget(target);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.roarCooldown > 0) this.roarCooldown--;
        if (this.roarAnim > 0) this.roarAnim--;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.roarAnim = ROAR_ANIM_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** ทุกครั้งที่กัด จะมีเสียงกัดดังๆ */
    @Override
    public void swing(InteractionHand hand) {
        if (!this.level().isClientSide()) {
            this.playSound(ModSounds.BITE, 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
        }
        super.swing(hand);
    }

    // ---------- เสียง ----------
    @Override protected SoundEvent getAmbientSound() { return ModSounds.AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.HURT; }
    @Override protected SoundEvent getDeathSound() { return ModSounds.DEATH; }
    @Override protected float getSoundVolume() { return 1.6f; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.STEP, 0.9f, 0.85f + this.random.nextFloat() * 0.3f);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    // ทีเร็กซ์ผสมพันธุ์ไม่ได้
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
