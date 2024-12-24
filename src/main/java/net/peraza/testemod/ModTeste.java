package net.peraza.testemod;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mod(ModTeste.MOD_ID)
public class ModTeste {
    public static final String MOD_ID = "testemod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ModTeste() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(CommandLogger.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    public static class CommandLogger {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @SubscribeEvent
        public static void onCommandExecuted(RegisterCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("log")
                            .then(Commands.argument("message", StringArgumentType.string())
                                    .executes(context -> {
                                        String message = StringArgumentType.getString(context, "message");
                                        String playerName = context.getSource().getPlayerOrException().getName().getString();
                                        String timestamp = LocalDateTime.now().format(formatter);
                                        String logEntry = String.format("[%s] %s: %s", timestamp, playerName, message);

                                        logToFile(logEntry);

                                        context.getSource().sendSuccess(() -> Component.literal("Você executou o comando: " + message), false);
                                        return 1;
                                    })
                            )
            );
        }

        @SubscribeEvent
        public static void onChatMessage(ServerChatEvent event) {
            String message = String.valueOf(event.getMessage());
            String playerName = event.getPlayer().getName().getString();
            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = String.format("[%s] %s: %s", timestamp, playerName, message);

            logToFile(logEntry);
        }

        private static void logToFile(String message) {
            Path logPath = Paths.get("config/command_logs.txt");

            try (PrintWriter out = new PrintWriter(new FileWriter(logPath.toFile(), true))) {
                out.println(message);
            } catch (IOException e) {
                LOGGER.error("Erro ao gravar o log em arquivo: ", e);
            }
        }
    }
}