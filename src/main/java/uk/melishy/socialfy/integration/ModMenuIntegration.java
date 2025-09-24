package uk.melishy.socialfy.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import uk.melishy.socialfy.Socialfy;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            if (Socialfy.config != null) {
                return Socialfy.config.getConfigScreen(parent);
            }
            return null;
        };
    }
}