package vexatious.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EvokerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import vexatious.duck.VexEntityExtension;

@Mixin(targets = "net.minecraft.entity.mob.EvokerEntity$SummonVexGoal")
public class EvokerEntity$SummonVexGoalMixin {
    @Final
    @Shadow
    EvokerEntity field_7267;

    @ModifyArg(
            method = "castSpell",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"
            )
    )
    private Entity modifyVexSpawn(Entity vex) {
        if (vex instanceof VexEntityExtension extendedVex) {
            extendedVex.vexatious$setOwnerUUID(this.field_7267.getUuid());
        }
        return vex;
    }

}
