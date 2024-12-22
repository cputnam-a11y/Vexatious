package vexatious.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vexatious.ActiveEvokerRegistry;
import vexatious.duck.VexEntityExtension;

import java.util.Optional;
import java.util.UUID;

@Mixin(VexEntity.class)
public abstract class VexEntityMixin extends HostileEntity implements VexEntityExtension {
    @Unique
    @Nullable
    private UUID ownerUUID;

    protected VexEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void onPostTickVex(CallbackInfo ci) {
        if (this.getWorld() instanceof ServerWorld serverWorld && !ActiveEvokerRegistry.get(serverWorld).containsUUID(ownerUUID)) {
            this.kill();
        }
    }

    @Override
    public void vexatious$setOwnerUUID(@Nullable final UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    @Override
    public Optional<UUID> vexatious$getOwnerUUID() {
        return Optional.ofNullable(this.ownerUUID);
    }
}
