package com.ss.editor.font.generator.creator;

import static com.ss.editor.extension.property.EditablePropertyType.*;
import static com.ss.rlib.common.util.ObjectUtils.notNull;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.font.generator.FontGeneratorEditorPlugin;
import com.ss.editor.font.generator.PluginMessages;
import com.ss.editor.plugin.api.file.creator.GenericFileCreator;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.ui.component.creator.FileCreatorDescription;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.VarTable;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

/**
 * The implementation of bitmap font generation which was took from
 * https://github.com/jMonkeyEngine/sdk/blob/master/jme3-angelfont/src/com/jme3/gde/angelfont/FontCreator.java
 *
 * @author JavaSaBr, normenhansen
 */
public class BitmapFontFileCreator extends GenericFileCreator {

    private static final Color OPAQUE_WHITE = new Color(0xFFFFFFFF, true);
    private static final Color TRANSPARENT_BLACK = new Color(0x00000000, true);

    private static final String PROP_FONT = "font";
    private static final String PROP_IMAGE_SIZE = "imageSize";
    private static final String PROP_FONT_SIZE = "fontSize";
    private static final String PROP_FONT_STYLE = "fontStyle";
    private static final String PROP_PADDING_X = "paddingX";
    private static final String PROP_PADDING_Y = "paddingY";
    private static final String PROP_FIRST_CHAR = "firstChar";
    private static final String PROP_LAST_CHAR = "lastChar";
    private static final String PROP_LETTER_SPACING = "letterSpacing";

    private static final Array<String> FONT_STYLES = ArrayFactory.asArray("Plain", "Italic", "Bold");

    public static final FileCreatorDescription DESCRIPTION = new FileCreatorDescription();

    static {
        DESCRIPTION.setFileDescription(PluginMessages.FONT_GENERATOR_DESCRIPTION);
        DESCRIPTION.setConstructor(BitmapFontFileCreator::new);
    }

    /**
     * The image view to show preview of font.
     */
    @Nullable
    private ImageView imageView;

    @Override
    @FxThread
    protected void createPreview(@NotNull BorderPane container) {
        super.createPreview(container);
        imageView = new ImageView();
        container.setCenter(imageView);
    }

    /**
     * Get the image view to show preview of font.
     *
     * @return the image view to show preview of font.
     */
    @FxThread
    private @NotNull ImageView getImageView() {
        return notNull(imageView);
    }

    @Override
    @FromAnyThread
    protected boolean needPreview() {
        return true;
    }

    @Override
    @FromAnyThread
    protected @NotNull Array<PropertyDefinition> getPropertyDefinitions() {

        var result = ArrayFactory.<PropertyDefinition>newArray(PropertyDefinition.class);
        result.add(new PropertyDefinition(AWT_FONT, PluginMessages.FONT_GENERATOR_PROP_FONT,
                PROP_FONT, null));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_FONT_SIZE,
                PROP_FONT_SIZE, 16, 4, 128));
        result.add(new PropertyDefinition(STRING_FROM_LIST, PluginMessages.FONT_GENERATOR_PROP_FONT_STYLE,
                PROP_FONT_STYLE, "Plain", FONT_STYLES));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_IMAGE_SIZE,
                PROP_IMAGE_SIZE, 256, 32, 4096));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_PADDING_X,
                PROP_PADDING_X, 0, -10, 10));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_PADDING_Y,
                PROP_PADDING_Y, 0, -10, 10));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_LETTER_SPACING,
                PROP_LETTER_SPACING, 0, -10, 10));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_FIRST_CHAR,
                PROP_FIRST_CHAR, 0, 0, Character.MAX_CODE_POINT));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_LAST_CHAR,
                PROP_LAST_CHAR, 256, 0, Character.MAX_CODE_POINT));

        return result;
    }

    @Override
    @FxThread
    protected boolean validate(@NotNull VarTable vars) {

        if (!vars.has(PROP_FONT)) {
            return false;
        }

        var fontStyle = getFontStyle(vars);
        if (fontStyle == -1) {
            return false;
        }

        var font = vars.get(PROP_FONT, Font.class);
        var imageSize = vars.getInteger(PROP_IMAGE_SIZE);
        var fontSize = vars.getInteger(PROP_FONT_SIZE);
        var paddingX = vars.getInteger(PROP_PADDING_X);
        var paddingY = vars.getInteger(PROP_PADDING_Y);
        var firstChar = (char) vars.getInteger(PROP_FIRST_CHAR);
        var lastChar = (char) vars.getInteger(PROP_LAST_CHAR);

        var image = buildImage(font.getFontName(), imageSize, fontSize, fontStyle, paddingX, paddingY,
                firstChar, lastChar, true);

        var previewContainer = notNull(getPreviewContainer());

        var imageView = getImageView();
        imageView.setImage(SwingFXUtils.toFXImage(image, null));

        if (imageSize + 12 > previewContainer.getWidth()) {

            imageView.fitWidthProperty()
                    .bind(previewContainer.widthProperty().subtract(12));
            imageView.fitHeightProperty()
                    .bind(previewContainer.heightProperty().subtract(12));

        } else {
            imageView.fitWidthProperty().unbind();
            imageView.setFitWidth(0);
            imageView.fitHeightProperty().unbind();
            imageView.setFitHeight(0);
        }

        return super.validate(vars);
    }

    /**
     * Gets the font style code.
     *
     * @param vars the variables.
     * @return the font style code.
     */
    @FromAnyThread
    private int getFontStyle(@NotNull VarTable vars) {

        var fontStyle = vars.getString(PROP_FONT_STYLE);

        switch (fontStyle) {
            case "Plain":
                return Font.PLAIN;
            case "Italic":
                return Font.ITALIC;
            case "Bold":
                return Font.BOLD;
            default:
                return -1;
        }
    }

    @Override
    @FromAnyThread
    protected @NotNull String getTitleText() {
        return PluginMessages.FONT_GENERATOR_TITLE;
    }

    @Override
    @FromAnyThread
    protected @NotNull String getFileExtension() {
        return FontGeneratorEditorPlugin.FONT_EXTENSION;
    }

    @Override
    @FxThread
    protected void processOk() {
        super.processOk();

        var fontFile = notNull(getFileToCreate());
        var filename = FileUtils.getNameWithoutExtension(fontFile);

        var parent = fontFile.getParent();
        var imageFile = parent.resolve(filename + "." + FileExtensions.IMAGE_PNG);

        var vars = getVars();
        var font = vars.get(PROP_FONT, Font.class);
        var imageSize = vars.getInteger(PROP_IMAGE_SIZE);
        var fontSize = vars.getInteger(PROP_FONT_SIZE);
        var paddingX = vars.getInteger(PROP_PADDING_X);
        var paddingY = vars.getInteger(PROP_PADDING_Y);
        var letterSpacing = vars.getInteger(PROP_LETTER_SPACING);
        var fontStyle = getFontStyle(vars);
        var firstChar = (char) vars.getInteger(PROP_FIRST_CHAR);
        var lastChar = (char) vars.getInteger(PROP_LAST_CHAR);

        var image = buildImage(font.getFontName(), imageSize, fontSize, fontStyle, paddingX, paddingY,
                firstChar, lastChar, false);

        var description = buildDescription(font.getFontName(), imageFile.getFileName().toString(), imageSize,
                fontSize, fontStyle, paddingX, paddingY, letterSpacing, firstChar, lastChar);

        try (var out = new PrintWriter(Files.newOutputStream(fontFile))) {
            out.print(description);
        } catch (IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return;
        }

        notifyFileCreated(fontFile, true);

        try (var out = Files.newOutputStream(imageFile)) {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return;
        }

        notifyFileCreated(imageFile, false);
    }

    /**
     * Build an image.
     *
     * @param fontName   the font name.
     * @param bitmapSize the bitmap size.
     * @param fontSize   the font size.
     * @param style      the style.
     * @param paddingX   the padding X.
     * @param paddingY   the padding Y.
     * @param firstChar  the first char.
     * @param lastChar   the last char.
     * @param debug      the debug.
     * @return the image.
     */
    @FromAnyThread
    private @NotNull BufferedImage buildImage(
            @NotNull String fontName,
            int bitmapSize,
            int fontSize,
            int style,
            int paddingX,
            int paddingY,
            char firstChar,
            char lastChar,
            boolean debug
    ) {

        var font = new Font(fontName, style, fontSize);
        var fontImage = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_4BYTE_ABGR);

        var graphics = (Graphics2D) fontImage.getGraphics();
        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(OPAQUE_WHITE);
        graphics.setBackground(TRANSPARENT_BLACK);

        var fontRenderContext = graphics.getFontRenderContext();
        var fontMetrics = graphics.getFontMetrics();

        int xPos = 0;
        int height = fontMetrics.getDescent() + fontMetrics.getAscent();
        int yPos = height + (paddingY * 2);

        for (int i = firstChar; i <= lastChar; i++) {

            var ch = (char) i;
            if (!font.canDisplay(ch)) {
                continue;
            }

            var str = Character.toString(ch);
            var textLayout = new TextLayout(str, font, fontRenderContext);
            var pixelBounds = textLayout.getPixelBounds(fontRenderContext, xPos, yPos);

            var width = (int) Math.ceil(pixelBounds.getWidth());
            var xOffset = (int) Math.round(pixelBounds.getX()) - xPos;

            if (xPos + width + (paddingX * 2) > bitmapSize) {
                xPos = 0;
                yPos += height + (paddingY * 2);
            }

            graphics.drawString(str, xPos + paddingX - xOffset, yPos + paddingY);

            if (debug) {
                graphics.setColor(Color.BLUE);
                graphics.drawRect(xPos, yPos - fontMetrics.getAscent(),
                        width + (paddingX * 2), height + (paddingY * 2));
                graphics.setColor(Color.WHITE);
            }

            xPos += width + (paddingX * 2);
        }

        return fontImage;
    }

    /**
     * Build an image.
     *
     * @param fontName      the font name.
     * @param fileName      the file name.
     * @param bitmapSize    the bitmap size.
     * @param fontSize      the font size.
     * @param style         the style.
     * @param paddingX      the padding X.
     * @param paddingY      the padding Y.
     * @param letterSpacing the letter spacing.
     * @param firstChar     the first char.
     * @param lastChar      the last char.
     * @return the description.
     */
    @FromAnyThread
    private @NotNull String buildDescription(
            @NotNull String fontName,
            @NotNull String fileName,
            int bitmapSize,
            int fontSize,
            int style,
            int paddingX,
            int paddingY,
            int letterSpacing,
            char firstChar,
            char lastChar
    ) {

        var font = new Font(fontName, style, fontSize);
        var locations = new StringBuilder();
        var fontImage = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_4BYTE_ABGR);

        var graphics = (Graphics2D) fontImage.getGraphics();
        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(OPAQUE_WHITE);
        graphics.setBackground(TRANSPARENT_BLACK);

        var fontRenderContext = graphics.getFontRenderContext();
        var fontMetrics = graphics.getFontMetrics();

        var xPos = 0;
        var height = fontMetrics.getDescent() + fontMetrics.getAscent();
        var yPos = height + (paddingY * 2);
        var count = 0;

        for (int i = firstChar; i <= lastChar; i++) {

            var ch = (char) i;
            if (!font.canDisplay(ch)) {
                continue;
            }

            count++;

            var str = Character.toString(ch);
            var textLayout = new TextLayout(str, font, fontRenderContext);
            var pixelBounds = textLayout.getPixelBounds(fontRenderContext, xPos, yPos);

            var width = (int) Math.ceil(pixelBounds.getWidth());
            var advance = (int) Math.ceil(textLayout.getAdvance());
            var xOffset = (int) Math.round(pixelBounds.getX()) - xPos;

            if (xPos + width + (paddingX * 2) > bitmapSize) {
                xPos = 0;
                yPos += height + (paddingY * 2);
            }

            locations.append("char id=" + i
                    + "    x=" + xPos
                    + "    y=" + (yPos - fontMetrics.getAscent())
                    + "    width=" + (width + (paddingX * 2))
                    + "    height=" + (fontMetrics.getHeight() + (paddingY * 2))
                    + "    xoffset=" + (xOffset)
                    + "    yoffset=0"
                    + "    xadvance=" + ((advance + letterSpacing) - 1) + " "
                    + "    page=0"
                    + "    chnl=0\n");

            xPos += width + (paddingX * 2);
        }

        return "info face=" + fileName + " "
                + "size=" + fontSize + " "
                + "bold=0 "
                + "italic=0 "
                + "charset=\"\" "
                + "unicode=1 "
                + "stretchH=100 "
                + "smooth=1 "
                + "aa=1 "
                + "padding=0,0,0,0 "
                + "spacing=1,1 "
                + "outline=0 "
                + "\n"
                + "common lineHeight=" + height + " "
                + "base=26 "
                + "scaleW=" + bitmapSize + " "
                + "scaleH=" + bitmapSize + " "
                + "pages=1 "
                + "packed=0 "
                + "\n"
                + "page id=0 file=\"" + fileName + "\"\n"
                + "chars count=" + count + "\n"
                + locations;
    }
}
