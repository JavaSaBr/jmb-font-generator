package com.ss.editor.font.generator;

import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.font.generator.creator.BitmapFontFileCreator;
import com.ss.editor.manager.FileIconManager;
import com.ss.editor.plugin.EditorPlugin;
import com.ss.editor.ui.component.creator.FileCreatorRegistry;
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
        name = "jME font Generator",
        description = "The plugin to generate native jME fonts from system fonts"
)
public class FontGeneratorEditorPlugin extends EditorPlugin {

    @NotNull
    public static final String FONT_EXTENSION = "fnt";

    /**
     * @param pluginContainer the plugin container.
     */
    public FontGeneratorEditorPlugin(@NotNull final PluginContainer pluginContainer) {
        super(pluginContainer);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final FileCreatorRegistry registry) {
        super.register(registry);
        registry.register(BitmapFontFileCreator.DESCRIPTION);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final FileIconManager iconManager) {
        iconManager.register((path, extension) -> {

            if (FONT_EXTENSION.equals(extension)) {
                return "com/ss/editor/font/generator/icons/text.svg";
            }

            return null;
        });
    }
}
