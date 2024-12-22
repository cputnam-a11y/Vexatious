package vexatious.duck;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface VexEntityExtension {
    void vexatious$setOwnerUUID(@Nullable final UUID ownerUUID);

    Optional<UUID> vexatious$getOwnerUUID();
}
