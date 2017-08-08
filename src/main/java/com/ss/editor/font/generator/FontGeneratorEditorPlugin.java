package com.ss.editor.font.generator;

import com.ss.editor.plugin.EditorPlugin;
import com.ss.rlib.plugin.PluginContainer;
import com.ss.rlib.plugin.annotation.PluginDescription;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of an editor plugin.
 *
 * @author JavaSaBr
 */
@PluginDescription(
        id = "com.ss.editor.font.generator",
        version = "1.0.0",
        name = "Font Generator",
        description = "The plugin to generate bitmap fonts from system fonts"
)
public class FontGeneratorEditorPlugin extends EditorPlugin {

    /**
     * @param pluginContainer the plugin container.
     */
    public FontGeneratorEditorPlugin(@NotNull final PluginContainer pluginContainer) {
        super(pluginContainer);
    }
}
