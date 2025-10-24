package uk.melishy.socialfy.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.ButtonOption;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import uk.melishy.discord.SocialSDK;
import uk.melishy.socialfy.Socialfy;

public class YaclConfig {
    private static final String LANG_PREFIX = "socialfy.config.";
    private static final String CATEGORY_PREFIX = LANG_PREFIX + "category.";

    public Screen getConfigScreen(Screen parent) {
        Config config = Socialfy.config;
        if (config == null) return parent;

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable(LANG_PREFIX + "title"))
                .save(config::save)

                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable(CATEGORY_PREFIX + "general"))
                        .tooltip(Component.translatable(CATEGORY_PREFIX + "general.description"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable(LANG_PREFIX + "general.enabled"))
                                .description(OptionDescription.of(Component.translatable(LANG_PREFIX + "general.enabled.description")))
                                .binding(config.enabled, () -> config.enabled, newVal -> {
                                    config.enabled = newVal;
                                    if (config.enabled) SocialSDK.startRPC();
                                    else SocialSDK.stopRPC();
                                    Socialfy.updateActivitySafe();
                                })
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable(LANG_PREFIX + "general.reconnect"))
                                .description(OptionDescription.of(Component.translatable(LANG_PREFIX + "general.reconnect.description")))
                                .action((yaclScreen, thisOption) -> {
                                    Thread reconnectThread = new Thread(() -> {
                                        try {
                                            SocialSDK.forceReconnect();
                                        } catch (Exception ignored) { }
                                    }, "Socialfy-Reconnect");
                                    reconnectThread.setDaemon(true);
                                    reconnectThread.start();
                                })
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable(LANG_PREFIX + "general.show_state"))
                                .description(OptionDescription.of(Component.translatable(LANG_PREFIX + "general.show_state.description")))
                                .binding(config.showState, () -> config.showState, newVal -> {
                                    config.showState = newVal;
                                    Socialfy.updateActivitySafe();
                                })
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable(LANG_PREFIX + "general.show_player_count"))
                                .description(OptionDescription.of(Component.translatable(LANG_PREFIX + "general.show_player_count.description")))
                                .binding(config.showPlayerCount, () -> config.showPlayerCount, newVal -> {
                                    config.showPlayerCount = newVal;
                                    Socialfy.updateActivitySafe();
                                })
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable(LANG_PREFIX + "general.show_server_ip"))
                                .description(OptionDescription.of(Component.translatable(LANG_PREFIX + "general.show_server_ip.description")))
                                .binding(config.showServerIp, () -> config.showServerIp, newVal -> {
                                    config.showServerIp = newVal;
                                    Socialfy.updateActivitySafe();
                                })
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())
                        .build())

                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable(CATEGORY_PREFIX + "advanced"))
                        .tooltip(Component.translatable(CATEGORY_PREFIX + "advanced.description"))

                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable(LANG_PREFIX + "advanced.update_interval"))
                                .description(OptionDescription.of(Component.translatable(LANG_PREFIX + "advanced.update_interval.description")))
                                .binding(config.updateInterval, () -> config.updateInterval, newVal -> config.updateInterval = newVal)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 30).step(1))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable(LANG_PREFIX + "advanced.debug_mode"))
                                .description(OptionDescription.of(Component.translatable(LANG_PREFIX + "advanced.debug_mode.description")))
                                .binding(config.debugMode, () -> config.debugMode, newVal -> config.debugMode = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .build())

                .build()
                .generateScreen(parent);
    }
}