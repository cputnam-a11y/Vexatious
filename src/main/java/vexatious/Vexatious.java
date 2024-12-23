package vexatious;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vexatious.command.VexatiousCommand;

import java.util.Objects;

public class Vexatious implements ModInitializer {
    public static final String MOD_ID = "vexatious";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof EvokerEntity evoker) {
                ActiveEvokerRegistry.get(world).addEvoker(evoker);
            }
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof EvokerEntity evoker && entity.getWorld() instanceof ServerWorld serverWorld) {
                ActiveEvokerRegistry.get(serverWorld).removeEvoker(evoker);
            }
        });
        CommandRegistrationCallback.EVENT.register(new VexatiousCommand());
        LOGGER.info("More Vexatious than ever!");
    }
}