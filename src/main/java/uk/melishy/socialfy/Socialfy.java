package uk.melishy.socialfy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.melishy.discord.SocialSDK;
import uk.melishy.socialfy.config.Config;

import java.time.Instant;

public class Socialfy implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Socialfy");
    public static Config config;
    public static Instant launchTime = Instant.now();
    private static int tickCounter = 0;

    public static void updateActivitySafe() {
        if (!config.enabled) {
            SocialSDK.clearActivity();
            return;
        }

        Minecraft client = Minecraft.getInstance();
        ServerData serverData = client.getCurrentServer();
        boolean isSingleplayer = client.isSingleplayer();

        boolean isLanOpen = isSingleplayer && client.getSingleplayerServer() != null &&
                client.getSingleplayerServer().isPublished();

        boolean isRealm = serverData != null && serverData.isRealm();

        SocialSDK.startRPC();

        int partySize = 0;
        int maxPartySize = 0;

        if (client.level != null && config.showPlayerCount) {
            if (!isSingleplayer || isLanOpen) {
                partySize = client.level.players().size();

                if (isLanOpen) {
                    maxPartySize = 8;
                } else if (isRealm) {
                    maxPartySize = 10;
                } else {
                    maxPartySize = 0; // ignore the warning - this is needed to avoid silly bug lmao
                }
            }
        }

        Component state = null;
        if (client.level != null && config.showState) {
            if (isSingleplayer && !isLanOpen) {
                state = Component.translatable("socialfy.state.singleplayer");
            } else if (isRealm) {
                if (!serverData.name.isEmpty()) {
                    state = Component.translatable("socialfy.state.realm.named", serverData.name);
                } else {
                    state = Component.translatable("socialfy.state.realm");
                }
            } else if (serverData != null && config.showServerIp && !serverData.ip.isEmpty()) {
                state = Component.translatable("socialfy.state.multiplayer.ip", serverData.ip);
            } else {
                state = Component.translatable("socialfy.state.multiplayer");
            }
        }

        SocialSDK.updateActivity(state, null, partySize, maxPartySize);
    }

    @Override
    public void onInitializeClient() {
        config = Config.initialize();
        LOGGER.info("[Socialfy] Initializing");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> updateActivitySafe());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SocialSDK.clearActivity());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!config.enabled || client.level == null) {
                return;
            }
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                updateActivitySafe();
            }
        });

        if (config.enabled) {
            LOGGER.info("[Socialfy] Starting Discord RPC");
            SocialSDK.startRPC();
            updateActivitySafe();
        } else {
            LOGGER.info("[Socialfy] Discord RPC disabled");
        }
    }
}