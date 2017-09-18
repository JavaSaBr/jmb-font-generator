package com.ss.editor.font.generator;

import static com.ss.editor.plugin.api.messages.MessagesPluginFactory.getResourceBundle;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * The class with localised all plugin messages.
 *
 * @author JavaSaBr
 */
public interface PluginMessages {

    @NotNull ResourceBundle RESOURCE_BUNDLE = getResourceBundle(FontGeneratorEditorPlugin.class,
            "com/ss/editor/font/generator/messages/messages");

    @NotNull String FONT_GENERATOR_DESCRIPTION = RESOURCE_BUNDLE.getString("FontGeneratorDescription");
    @NotNull String FONT_GENERATOR_TITLE = RESOURCE_BUNDLE.getString("FontGeneratorTitle");
    @NotNull String FONT_GENERATOR_PROP_FONT = RESOURCE_BUNDLE.getString("FontGeneratorPropFont");
    @NotNull String FONT_GENERATOR_PROP_IMAGE_SIZE = RESOURCE_BUNDLE.getString("FontGeneratorPropImageSize");
    @NotNull String FONT_GENERATOR_PROP_FONT_SIZE = RESOURCE_BUNDLE.getString("FontGeneratorPropFontSize");
    @NotNull String FONT_GENERATOR_PROP_FONT_STYLE = RESOURCE_BUNDLE.getString("FontGeneratorPropFontStyle");
    @NotNull String FONT_GENERATOR_PROP_PADDING_X = RESOURCE_BUNDLE.getString("FontGeneratorPropPaddingX");
    @NotNull String FONT_GENERATOR_PROP_PADDING_Y = RESOURCE_BUNDLE.getString("FontGeneratorPropPaddingY");
    @NotNull String FONT_GENERATOR_PROP_LETTER_SPACING = RESOURCE_BUNDLE.getString("FontGeneratorPropLetterSpacing");
    @NotNull String FONT_GENERATOR_PROP_FIRST_CHAR = RESOURCE_BUNDLE.getString("FontGeneratorPropFirstChar");
    @NotNull String FONT_GENERATOR_PROP_LAST_CHAR = RESOURCE_BUNDLE.getString("FontGeneratorPropLastChar");
}
