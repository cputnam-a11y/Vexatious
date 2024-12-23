package vexatious;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import vexatious.util.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("LoggingSimilarMessage")
public class ActiveEvokerRegistry extends PersistentState {
    public static final Type<ActiveEvokerRegistry> TYPE = new Type<>(
            ActiveEvokerRegistry::new,
            ActiveEvokerRegistry::fromNbt,
            null
    );
    static final Codec<ActiveEvokerRegistry> CODEC =
            EntityReference.<EvokerEntity>createCodec()
                    .listOf()
                    .<List<EntityReference<EvokerEntity>>>xmap(
                            ArrayList::new,
                            Function.identity()
                    )
                    .xmap(
                            ActiveEvokerRegistry::new,
                            (r) -> r.activeEvokers
                    );
    private List<EntityReference<EvokerEntity>> activeEvokers = new ArrayList<>();

    public ActiveEvokerRegistry() {
        super();
    }

    ActiveEvokerRegistry(List<EntityReference<EvokerEntity>> activeEvokers) {
        this.activeEvokers = activeEvokers;
    }

    public static ActiveEvokerRegistry get(ServerWorld world) {
        return Objects.requireNonNull(
                        world.getServer()
                                .getWorld(ServerWorld.OVERWORLD)
                )
                .getPersistentStateManager()
                .getOrCreate(TYPE, "active_evokers");
    }

    public void addEvoker(EvokerEntity evoker) {
        activeEvokers.add(EntityReference.of(evoker));
        markDirty();
    }

    public void removeEvoker(UUID uuid) {
        activeEvokers.removeIf(ref -> Objects.equals(ref.uuid(), uuid));
        markDirty();
    }

    public void removeEvoker(EvokerEntity evoker) {
        activeEvokers.removeIf(ref -> Objects.equals(ref.uuid(), evoker.getUuid()));
        markDirty();
    }

    public boolean containsUUID(UUID uuid) {
        return activeEvokers.stream().anyMatch(ref -> Objects.equals(ref.uuid(), uuid));
    }
    public void forEachRef(Consumer<EntityReference<EvokerEntity>> consumer) {
        activeEvokers.forEach(consumer);
    }
    ImmutableList<EntityReference<EvokerEntity>> getActiveEvokers() {
        return ImmutableList.copyOf(activeEvokers);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound activeEvokersNbt = new NbtCompound();
        RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, registryLookup);
        CODEC.encodeStart(registryOps, this)
                .ifError(
                        err -> {
                            Vexatious.LOGGER.info("Failed to encode active evokers to NBT!");
                            Vexatious.LOGGER.info("Skipping...");
                            Vexatious.LOGGER.info("See Debug log for more information.");
                            Vexatious.LOGGER.debug(err.message());
                        }
                ).ifSuccess(
                        element -> activeEvokersNbt.put(
                                "activeEvokers",
                                element
                        )
                );
        return activeEvokersNbt;
    }

    protected static ActiveEvokerRegistry fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, registryLookup);
        return CODEC.parse(registryOps, nbt.get("activeEvokers")).resultOrPartial(str -> {
            Vexatious.LOGGER.info("Failed to read active evokers from NBT!");
            Vexatious.LOGGER.info("Defaulting to empty registry...");
            Vexatious.LOGGER.info("See Debug log for more information.");
            Vexatious.LOGGER.debug(str);
        }).orElse(new ActiveEvokerRegistry());
    }

}
