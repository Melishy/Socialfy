package melishy.socialfy.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import melishy.socialfy.Main;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            if (Main.config != null) {
                return Main.config.getConfigScreen(parent);
            }
            return null;
        };
    }
}