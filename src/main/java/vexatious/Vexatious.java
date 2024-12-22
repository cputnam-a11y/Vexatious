package vexatious;

import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandNode<ServerCommandSource> MAIN = CommandManager.literal(MOD_ID).build();
            CommandNode<ServerCommandSource> LIST = CommandManager.literal("list").executes(context -> {
                ActiveEvokerRegistry.get(context.getSource().getWorld()).listEvokers(context.getSource());
                return 1;
            }).build();
            CommandNode<ServerCommandSource> REMOVE = CommandManager.literal("remove").build();
            CommandNode<ServerCommandSource> REMOVE_UUID_ARG = CommandManager.argument(
                            "uuid",
                            UuidArgumentType.uuid()
                    )
                    .executes(
                            context -> {
                                ActiveEvokerRegistry registry = ActiveEvokerRegistry.get(context.getSource().getWorld());
                                registry.removeEvoker(UuidArgumentType.getUuid(context, "uuid"));
                                return 1;
                            }
                    )
                    .build();
            REMOVE.addChild(REMOVE_UUID_ARG);
            MAIN.addChild(LIST);
            MAIN.addChild(REMOVE);
            dispatcher.getRoot().addChild(MAIN);
        });
        LOGGER.info("More Vexatious than ever!");
    }
}