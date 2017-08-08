package com.ss.editor.font.generator;

import com.ss.editor.plugin.api.messages.MessagesPluginFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * The class with localised all plugin messages.
 *
 * @author JavaSaBr
 */
public class Messages {

    @NotNull
    private static final ResourceBundle RESOURCE_BUNDLE = MessagesPluginFactory.getResourceBundle(FontGeneratorEditorPlugin.class,
            "com/ss/editor/font/generator/messages/messages");

    static {
    }
}
