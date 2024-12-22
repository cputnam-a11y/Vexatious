package vexatious;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("LoggingSimilarMessage")
public class ActiveEvokerRegistry extends PersistentState {
    public static final Type<ActiveEvokerRegistry> TYPE = new Type<>(
            ActiveEvokerRegistry::new,
            ActiveEvokerRegistry::fromNbt,
            null
    );
    static final Codec<ActiveEvokerRegistry> CODEC = EntityReference.<EvokerEntity>createCodec().listOf().xmap(
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
        activeEvokers.removeIf(ref -> Objects.equals(ref.uuid, uuid));
        markDirty();
    }
    public void removeEvoker(EvokerEntity evoker) {
        activeEvokers.removeIf(ref -> Objects.equals(ref.uuid, evoker.getUuid()));
        markDirty();
    }

    public boolean containsUUID(UUID uuid) {
        return activeEvokers.stream().anyMatch(ref -> Objects.equals(ref.uuid, uuid));
    }

    public void listEvokers(ServerCommandSource source) {
        for (EntityReference<EvokerEntity> ref : activeEvokers) {
            MutableText text = Text.translatable("vexatious.evoker_prefix")
                    .append(Text.of(ref.uuid));
            source.sendFeedback(() -> text, false);
        }
    }

    ImmutableList<EntityReference<EvokerEntity>> getActiveEvokers() {
        return ImmutableList.copyOf(activeEvokers);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound activeEvokersNbt = new NbtCompound();
        RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, registryLookup);
        try {
            activeEvokersNbt.put("activeEvokers", CODEC.encodeStart(registryOps, this).getOrThrow());
        } catch (Exception e) {
            Vexatious.LOGGER.info("Failed to write active evokers to NBT!");
            Vexatious.LOGGER.info("Skipping...");
            Vexatious.LOGGER.info("See Debug log for more information.");
            Vexatious.LOGGER.debug(e.getMessage());
        }
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

    record EntityReference<T extends Entity>(UUID uuid, EntityType<T> type) {
        public static <T extends Entity> Codec<EntityReference<T>> createCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                            Uuids.CODEC.fieldOf("uuid").forGetter(EntityReference::uuid),
                            Registries.ENTITY_TYPE.getCodec()
                                    .xmap(
                                            EntityReference::<T>castEntityType,
                                            Function.identity()
                                    )
                                    .fieldOf("type")
                                    .forGetter(EntityReference::type)
                    ).apply(instance, EntityReference::new)
            );
        }

        public static <T extends Entity> EntityReference<T> of(T entity) {
            //noinspection unchecked
            return new EntityReference<>(entity.getUuid(), (EntityType<T>) entity.getType());
        }

        Optional<T> getEntity(ServerWorld world) {
            for (ServerWorld world1 : world.getServer().getWorlds()) {
                Optional<Entity> maybeEntity = Optional.ofNullable(world1.getEntity(uuid));
                if (maybeEntity.isPresent() && maybeEntity.get().getType() == type) {
                    //noinspection unchecked
                    return (Optional<T>) maybeEntity;
                }
            }
            return Optional.empty();
        }

        // sure... it's safe... right?
        @SuppressWarnings("unchecked")
        private static <T extends Entity> EntityType<T> castEntityType(EntityType<?> type) {
            return (EntityType<T>) type;
        }
    }
}
