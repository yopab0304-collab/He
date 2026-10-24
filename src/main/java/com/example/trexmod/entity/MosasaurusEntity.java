package com.example.trexmod.entity;

import com.example.trexmod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * โมซาซอรัส: สัตว์ทะเลยักษ์ ก้าวร้าว ล่าปลา/สัตว์น้ำ/ผู้เล่นในน้ำ
 * วิธีจับ: ว่ายไปใกล้ๆ แล้วป้อน "ปลาคอดดิบ" (โอกาส 20% ต่อตัว พลาดอาจโดนงับ!)
 */
public class MosasaurusEntity extends TamableAnimal {

    private static boolean isTameFood(ItemStack stack) {
        return stack.is(Items.COD);
    }

    private int roarCooldown = 0;
    private int roarAnim = 0;
    public static final int ROAR_ANIM_TICKS = 50;

    public MosasaurusEntity(EntityType<? extends MosasaurusEntity> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public float getRoarProgress(float partialTick) {
        return Math.max(0f, roarAnim - partialTick) / ROAR_ANIM_TICKS;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 1.1)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    public static boolean checkSpawnRules(EntityType<MosasaurusEntity> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.above()).is(FluidTags.WATER)
                && pos.getY() < level.getSeaLevel() - 4;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(2, new SwimFollowOwnerGoal(this, 1.2));
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0, 10));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        // ตัวป่า: ล่าผู้เล่นที่อยู่ในน้ำ (หรือนั่งเรืออยู่) 
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !this.isTame()
                        && (target.isInWater() || (target.getVehicle() != null && target.getVehicle().isInWater()))));
        // ตัวป่า: ล่าสัตว์น้ำทุกชนิด (ปลา หมึก โลมา ฯลฯ)
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                (target, level) -> !this.isTame()
                        && !(target instanceof MosasaurusEntity)
                        && (target.getType().getCategory() == MobCategory.WATER_CREATURE
                        || target.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE
                        || target.getType().getCategory() == MobCategory.WATER_AMBIENT)));
        // ตัวที่เชื่องแล้ว: ปกป้องเจ้าของจากมอนสเตอร์ในน้ำ
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                (target, level) -> this.isTame() && target.getType().getCategory() == MobCategory.MONSTER
                        && target.isInWater()));
    }

    // ---------------- วิธีจับ ----------------
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (isTameFood(stack)) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    if (this.random.nextInt(5) == 0) {                    // 20% ต่อปลา 1 ตัว
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.setOrderedToSit(false);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.playSound(ModSounds.MOSA_TAME, 1.8f, 1.0f);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                        if (this.random.nextInt(2) == 0) {                // พลาดแล้ว 50% จะโกรธและงับคุณ
                            this.setTarget(player);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (this.isOwnedBy(player)) {
            if (isTameFood(stack) && this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    this.heal(20.0f);
                }
                return InteractionResult.SUCCESS;
            }
            if (!this.level().isClientSide()) {          // มือเปล่า = สั่ง "อยู่เฉยๆ" / "ตามมา"
                this.setOrderedToSit(!this.isOrderedToSit());
                this.navigation.stop();
                this.setTarget(null);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // ---------------- ว่ายน้ำ ----------------
    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    public boolean isPushedByFluid() { return false; }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.isOrderedToSit() ? 0.6 : 0.9));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.roarCooldown > 0) this.roarCooldown--;
        if (this.roarAnim > 0) this.roarAnim--;
        if (this.isTame() && this.isOrderedToSit()) {
            this.navigation.stop();
        }
    }

    // ---------------- คำราม / กัด / เสียง ----------------
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && this.getTarget() == null && !this.level().isClientSide() && this.roarCooldown <= 0) {
            this.playSound(ModSounds.MOSA_ROAR, 2.5f, 0.9f + this.random.nextFloat() * 0.2f);
            this.level().broadcastEntityEvent(this, (byte) 10);
            this.roarCooldown = 120;
        }
        super.setTarget(target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.roarAnim = ROAR_ANIM_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void swing(InteractionHand hand) {
        if (!this.level().isClientSide()) {
            this.playSound(ModSounds.MOSA_BITE, 1.6f, 0.9f + this.random.nextFloat() * 0.2f);
        }
        super.swing(hand);
    }

    @Override protected SoundEvent getAmbientSound() { return ModSounds.MOSA_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.MOSA_HURT; }
    @Override protected SoundEvent getDeathSound() { return ModSounds.MOSA_DEATH; }
    @Override protected float getSoundVolume() { return 1.8f; }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return null; }

    // ---------------- เป้าหมาย: ว่ายตามเจ้าของ ----------------
    private static class SwimFollowOwnerGoal extends Goal {
        private final MosasaurusEntity mob;
        private final double speed;
        private int recalc;

        SwimFollowOwnerGoal(MosasaurusEntity mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = mob.getOwner();
            return mob.isTame() && !mob.isOrderedToSit() && owner != null && !owner.isSpectator()
                    && mob.distanceToSqr(owner) > 100.0;       // ไกลกว่า 10 บล็อก
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity owner = mob.getOwner();
            return owner != null && !mob.isOrderedToSit() && mob.distanceToSqr(owner) > 16.0;
        }

        @Override
        public void start() { recalc = 0; }

        @Override
        public void stop() { mob.getNavigation().stop(); }

        @Override
        public void tick() {
            LivingEntity owner = mob.getOwner();
            if (owner == null) return;
            mob.getLookControl().setLookAt(owner, 10.0f, (float) mob.getMaxHeadXRot());
            if (--recalc <= 0) {
                recalc = 10;
                mob.getNavigation().moveTo(owner, speed);
            }
        }
    }
}
