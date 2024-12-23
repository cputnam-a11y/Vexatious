package vexatious.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public record EntityReference<T extends Entity>(UUID uuid, EntityType<T> type) {
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
