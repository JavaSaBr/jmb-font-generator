package com.ss.editor.font.generator;

import com.ss.editor.annotation.BackgroundThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.font.generator.creator.BitmapFontFileCreator;
import com.ss.editor.manager.FileIconManager;
import com.ss.editor.plugin.EditorPlugin;
import com.ss.editor.ui.component.creator.FileCreatorRegistry;
import com.ss.rlib.common.plugin.PluginContainer;
import com.ss.rlib.common.plugin.annotation.PluginDescription;
import com.ss.rlib.common.plugin.extension.ExtensionPointManager;
import com.ss.rlib.common.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * The implementation of an editor plugin.
 *
 * @author JavaSaBr
 */
@PluginDescription(
        id = "com.ss.editor.font.generator",
        version = "1.1.3",
        minAppVersion = "1.9.0",
        name = "jME Font Generator",
        description = "Providers a new file wizard to generate native jME fonts based on system fonts."
)
public class FontGeneratorEditorPlugin extends EditorPlugin {

    public static final String FONT_EXTENSION = "fnt";

    public FontGeneratorEditorPlugin(@NotNull PluginContainer pluginContainer) {
        super(pluginContainer);
    }

    @Override
    @BackgroundThread
    public void register(@NotNull ExtensionPointManager manager) {
        super.register(manager);

        manager.getExtensionPoint(FileCreatorRegistry.EP_DESCRIPTORS)
                .register(BitmapFontFileCreator.DESCRIPTOR);
        manager.getExtensionPoint(FileIconManager.EP_ICON_FINDERS)
                .register(makeIconFinder());
    }

    @FromAnyThread
    private @NotNull FileIconManager.IconFinder makeIconFinder() {
        return (path, extension) -> {

            if (FONT_EXTENSION.equals(extension)) {
                return "com/ss/editor/font/generator/icons/text.svg";
            }

            return null;
        };
    }

    @Override
    @FromAnyThread
    public @Nullable URL getHomePageUrl() {
        return Utils.get("https://github.com/JavaSaBr/jmb-font-generator", URL::new);
    }
}
