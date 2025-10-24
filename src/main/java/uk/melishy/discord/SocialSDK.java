package uk.melishy.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.melishy.socialfy.Socialfy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SocialSDK {
    private static final Logger LOGGER = LoggerFactory.getLogger("Socialfy-RPC");
    private static CreateParams params;
    private static Core core;
    private static Activity activity;
    private static ScheduledExecutorService executor;
    private static final ReentrantLock lock = new ReentrantLock();

    public static void startRPC() {
        lock.lock();
        try {
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

                executor = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "Socialfy-RPC");
                    t.setDaemon(true);
                    return t;
                });

                executor.scheduleAtFixedRate(() -> {
                    lock.lock();
                    try {
                        if (core != null) {
                            core.runCallbacks();
                        }
                    } catch (Exception e) {
                        LOGGER.error("[Socialfy] Error in callback loop: {}", e.getMessage());
                    } finally {
                        lock.unlock();
                    }
                }, 0, 16, TimeUnit.MILLISECONDS);

                LOGGER.info("[Socialfy] Discord RPC started successfully");
            } catch (Exception e) {
                LOGGER.error("[Socialfy] Failed to start RPC: {}", e.getMessage());
                stopRPC();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void stopRPC() {
        lock.lock();
        try {
            if (executor != null) {
                executor.shutdownNow();
                try {
                    if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                        LOGGER.warn("[Socialfy] Executor did not terminate in time");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("[Socialfy] Interrupted while waiting for executor termination");
                }
                executor = null;
            }

            if (core != null) {
                try {
                    core.close();
                } catch (Exception e) {
                    LOGGER.warn("[Socialfy] Error closing core: {}", e.getMessage());
                }
                core = null;
            }

            if (params != null) {
                params = null;
            }

            activity = null;
            LOGGER.info("[Socialfy] Discord RPC stopped");
        } finally {
            lock.unlock();
        }
    }

    public static void forceReconnect() {
        lock.lock();
        try {
            LOGGER.info("[Socialfy] Manual reconnection requested");

            if (executor != null) {
                executor.shutdownNow();
                try {
                    if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                        LOGGER.warn("[Socialfy] Reconnect executor did not terminate in time");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                executor = null;
            }

            if (core != null) {
                try {
                    core.close();
                } catch (Exception ignored) {
                }
                core = null;
            }

            params = null;
            activity = null;

            startRPC();
            Socialfy.updateActivitySafe();
        } catch (Exception e) {
            LOGGER.error("[Socialfy] Error during reconnection: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
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

        if (executor != null && !executor.isShutdown()) {
            executor.execute(() -> updateActivityInternal(state, details, partySize, maxPartySize));
        }
    }

    private static void updateActivityInternal(Component state, String details, int partySize, int maxPartySize) {
        lock.lock();
        try {
            if (core == null || activity == null) {
                startRPC();
                if (core == null || activity == null) return;
            }

            activity.setDetails(details);
            activity.party().size().setCurrentSize(partySize);
            activity.party().size().setMaxSize(maxPartySize);
            activity.setState(state != null ? state.getString() : null);
            core.activityManager().updateActivity(activity);
        } catch (Exception e) {
            LOGGER.error("[Socialfy] Failed to update activity: {}", e.getMessage());
            stopRPC();
        } finally {
            lock.unlock();
        }
    }

    public static void clearActivity() {
        if (executor != null && !executor.isShutdown()) {
            executor.execute(SocialSDK::clearActivityInternal);
        }
    }

    private static void clearActivityInternal() {
        lock.lock();
        try {
            if (core == null || activity == null) return;

            activity.setState(null);
            activity.setDetails(null);
            activity.party().size().setCurrentSize(0);
            activity.party().size().setMaxSize(0);
            core.activityManager().updateActivity(activity);
        } catch (Exception e) {
            LOGGER.warn("[Socialfy] Error clearing activity: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}