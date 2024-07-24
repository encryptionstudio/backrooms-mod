package com.kpabr.backrooms.entity;

import com.kpabr.backrooms.config.BackroomsConfig;
import com.kpabr.backrooms.util.SACallbackManager;
import com.kpabr.backrooms.util.ServerAnimationCallback;
import name.trimsky.lib_ai.LibAI;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Optional;
import java.util.function.Consumer;

public final class WretchEntity extends PathAwareEntity implements GeoAnimatable {
    private static final TrackedData<Integer> CURRENT_ANIMATION = DataTracker.registerData(WretchEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<Text>> AI_TASK = DataTracker.registerData(WretchEntity.class,
            TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final long uniqueId;

    public WretchEntity(EntityType<WretchEntity> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = true;

        this.uniqueId = LibAI.generateNewUniqueId(world, new WretchEntityTasks.IdleTask(this));
    }

    @Override
    public void onDeath(DamageSource source) {
        LibAI.removeEntity(this.getWorld(), uniqueId);
        super.onDeath(source);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CURRENT_ANIMATION, AnimationEnum.IDLING.ordinal());
        this.dataTracker.startTracking(AI_TASK, Optional.empty());
    }

    public static DefaultAttributeContainer.Builder createWretchAttributes() {
        return HostileEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.134)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0);
    }

    @Range(from = 0, to = 3)
    public int getAnimation() {
        return this.dataTracker.get(CURRENT_ANIMATION);
    }

    public void setAnimation(AnimationEnum animationEnum) {
        this.dataTracker.set(CURRENT_ANIMATION, animationEnum.ordinal());
    }

    /**
     * @return Current AI task
     */
    public Text getAiTask() {
        return this.dataTracker.get(AI_TASK).orElse(null);
    }

    public void setAiTask(@NotNull Text text) {
        this.dataTracker.set(AI_TASK, Optional.of(text));
    }

    @Override
    public Text getName() {
        MutableText firstName = super.getName().copy();

        if (BackroomsConfig.getInstance().aiDebug) {
            Text aiTask = this.getAiTask();
            firstName.append("; ");
            if (aiTask != null)
                firstName.append(aiTask);
            return firstName;
        }
        return firstName;
    }

    @Override
    public boolean isCustomNameVisible() {
        return super.isCustomNameVisible() || BackroomsConfig.getInstance().aiDebug;
    }

    public void setAnimationCallback(ServerAnimationCallback callback, long milliseconds) {
        SACallbackManager.addNewCallback(callback, milliseconds);
    }

    private PlayState predicate(AnimationState<WretchEntity> event) {
        AnimationEnum.values()[this.getAnimation()].animation.accept(event);

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (damageSource.isOf(DamageTypes.HOT_FLOOR)) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        return super.damage(source, amount);
    }

    public enum AnimationEnum {
        IDLING((event) -> event.getController().setAnimation(
                RawAnimation.begin().thenPlay("animation.wretch.idle"))),

        MOVING((event) -> event.getController().setAnimation(
                RawAnimation.begin().thenPlay("animation.wretch.walk"))),

        ATTACKING((event) -> event.getController().setAnimation(
                RawAnimation.begin().thenPlay("animation.wretch.attack"))),

        SEARCHING((event) -> event.getController().setAnimation(
                RawAnimation.begin().thenPlay("animation.wretch.search"))),
        NONE((event) -> {
        });

        private final Consumer<AnimationState<WretchEntity>> animation;

        AnimationEnum(Consumer<AnimationState<WretchEntity>> animation) {
            this.animation = animation;
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return RenderUtils.getCurrentTick();
    }
}
