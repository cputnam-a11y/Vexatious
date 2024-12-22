package vexatious.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.VexEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vexatious.duck.VexEntityExtension;

@Mixin(targets = "net.minecraft.entity.mob.EvokerEntity$SummonVexGoal")
public class EvokerEntity$SummonVexGoalMixin {
    @Final
    @Shadow
    EvokerEntity field_7267;

    @Inject(
            method = "castSpell",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"
            )

    )
    private void onVexSpawned(CallbackInfo ci, @Local VexEntity vex) {
        if (vex instanceof VexEntityExtension vexExtension) {
            vexExtension.vexatious$setOwnerUUID(this.field_7267.getUuid());
        }
    }

}
