package vexatious;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vexatious.command.VexatiousCommand;

public class Vexatious implements ModInitializer {
    public static final String MOD_ID = "vexatious";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("More Vexatious than ever!");
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof EvokerEntity evoker))
                return;
            if (!(world instanceof ServerWorld serverWorld))
                return;
            ActiveEvokerRegistry registry = ActiveEvokerRegistry.get(serverWorld);
            if (registry.containsUUID(evoker.getUuid()))
                return;
            registry.addEvoker(evoker);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof EvokerEntity evoker))
                return;
            if (!(entity.getWorld() instanceof ServerWorld serverWorld))
                return;
            ActiveEvokerRegistry.get(serverWorld).removeEvoker(evoker);
        });
        CommandRegistrationCallback.EVENT.register(new VexatiousCommand());
    }
}