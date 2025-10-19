package uk.melishy.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.melishy.socialfy.Socialfy;

public class SocialSDK {
    private static final Logger LOGGER = LoggerFactory.getLogger("Socialfy-RPC");
    private static CreateParams params;
    private static Core core;
    private static Activity activity;

    public static void startRPC() {
        if (Socialfy.config == null || !Socialfy.config.enabled || core != null) return;
        params = new CreateParams();
        params.setClientID(1420035796488028290L);
        params.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD);
        try {
            core = new Core(params);
            activity = new Activity();
            activity.assets().setLargeImage("mclogo");
            ensureSessionTimestamp();
            core.activityManager().updateActivity(activity);
            LOGGER.info("[Socialfy] Discord RPC started successfully");
        } catch (Exception e) {
            LOGGER.error("[Socialfy] Failed to start RPC: {}", e.getMessage());
            stopRPC();
        }
    }

    public static void stopRPC() {
        if (core == null) return;
        try {
            core.close();
        } catch (Exception ignored) {
        }
        core = null;
        params = null;
        activity = null;
        LOGGER.info("[Socialfy] Discord RPC stopped");
    }

    private static void ensureSessionTimestamp() {
        if (activity == null) return;
        try {
            activity.timestamps().setStart(Socialfy.launchTime);
        } catch (Exception ignored) {
        }
    }

    public static void updateActivity(Component state, String details, int partySize, int maxPartySize) {
        if (Socialfy.config == null || !Socialfy.config.enabled) return;
        if (core == null || activity == null) {
            startRPC();
            if (core == null || activity == null) return;
        }
        try {
            activity.setDetails(details);
            activity.party().size().setCurrentSize(partySize);
            activity.party().size().setMaxSize(maxPartySize);
            activity.setState(state != null ? state.getString() : null);
            core.activityManager().updateActivity(activity);
            core.runCallbacks();
        } catch (Exception e) {
            LOGGER.error("[Socialfy] Failed to update activity: {}", e.getMessage());
            stopRPC();
        }
    }

    public static void clearActivity() {
        if (core == null || activity == null) return;
        try {
            activity.setState(null);
            activity.setDetails(null);
            activity.party().size().setCurrentSize(0);
            activity.party().size().setMaxSize(0);
            core.activityManager().updateActivity(activity);
            core.runCallbacks();
        } catch (Exception ignored) {
        }
    }
}