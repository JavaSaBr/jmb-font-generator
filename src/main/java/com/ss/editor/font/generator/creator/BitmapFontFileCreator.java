package com.ss.editor.font.generator.creator;

import static com.ss.editor.extension.property.EditablePropertyType.*;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.font.generator.FontGeneratorEditorPlugin;
import com.ss.editor.font.generator.PluginMessages;
import com.ss.editor.plugin.api.file.creator.GenericFileCreator;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.ui.component.creator.FileCreatorDescription;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The implementation of bitmap font generation which was took from
 * https://github.com/jMonkeyEngine/sdk/blob/master/jme3-angelfont/src/com/jme3/gde/angelfont/FontCreator.java
 *
 * @author JavaSaBr, normenhansen
 */
public class BitmapFontFileCreator extends GenericFileCreator {

    @NotNull
    private static final Color OPAQUE_WHITE = new Color(0xFFFFFFFF, true);

    @NotNull
    private static final Color TRANSPARENT_BLACK = new Color(0x00000000, true);

    @NotNull
    private static final String PROP_FONT = "font";

    @NotNull
    private static final String PROP_IMAGE_SIZE = "imageSize";

    @NotNull
    private static final String PROP_FONT_SIZE = "fontSize";

    @NotNull
    private static final String PROP_FONT_STYLE = "fontStyle";

    @NotNull
    private static final String PROP_PADDING_X = "paddingX";

    @NotNull
    private static final String PROP_PADDING_Y = "paddingY";

    @NotNull
    private static final String PROP_FIRST_CHAR = "firstChar";

    @NotNull
    private static final String PROP_LAST_CHAR = "lastChar";

    @NotNull
    private static final String PROP_LETTER_SPACING = "letterSpacing";

    @NotNull
    private static final Array<String> FONT_STYLES = ArrayFactory.asArray("Plain", "Italic", "Bold");

    /**
     * The constant DESCRIPTION.
     */
    @NotNull
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
    @FXThread
    protected void createPreview(@NotNull final BorderPane container) {
        super.createPreview(container);
        imageView = new ImageView();
        container.setCenter(imageView);
    }

    /**
     * @return the image view to show preview of font.
     */
    @FXThread
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

        final Array<PropertyDefinition> result = ArrayFactory.newArray(PropertyDefinition.class);
        result.add(new PropertyDefinition(AWT_FONT, PluginMessages.FONT_GENERATOR_PROP_FONT, PROP_FONT, null));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_FONT_SIZE, PROP_FONT_SIZE, 16, 4, 128));
        result.add(new PropertyDefinition(STRING_FROM_LIST, PluginMessages.FONT_GENERATOR_PROP_FONT_STYLE, PROP_FONT_STYLE, "Plain", FONT_STYLES));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_IMAGE_SIZE, PROP_IMAGE_SIZE, 256, 32, 4096));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_PADDING_X, PROP_PADDING_X, 0, -10, 10));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_PADDING_Y, PROP_PADDING_Y, 0, -10, 10));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_LETTER_SPACING, PROP_LETTER_SPACING, 0, -10, 10));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_FIRST_CHAR, PROP_FIRST_CHAR, 0, 0, Character.MAX_CODE_POINT));
        result.add(new PropertyDefinition(INTEGER, PluginMessages.FONT_GENERATOR_PROP_LAST_CHAR, PROP_LAST_CHAR, 256, 0, Character.MAX_CODE_POINT));

        return result;
    }

    @Override
    @FXThread
    protected boolean validate(@NotNull final VarTable vars) {
        if (!vars.has(PROP_FONT)) return false;
        final int fontStyle = getFontStyle(vars);
        if (fontStyle == -1) return false;

        final Font font = vars.get(PROP_FONT);
        final int imageSize = vars.getInteger(PROP_IMAGE_SIZE);
        final int fontSize = vars.getInteger(PROP_FONT_SIZE);
        final int paddingX = vars.getInteger(PROP_PADDING_X);
        final int paddingY = vars.getInteger(PROP_PADDING_Y);
        final char firstChar = (char) vars.getInteger(PROP_FIRST_CHAR);
        final char lastChar = (char) vars.getInteger(PROP_LAST_CHAR);

        final BufferedImage image = buildImage(font.getFontName(), imageSize, fontSize, fontStyle, paddingX, paddingY,
                firstChar, lastChar, true);

        final BorderPane previewContainer = getPreviewContainer();

        final ImageView imageView = getImageView();
        imageView.setImage(SwingFXUtils.toFXImage(image, null));

        if (imageSize + 12 > previewContainer.getWidth()) {
            imageView.fitWidthProperty().bind(previewContainer.widthProperty().subtract(12));
            imageView.fitHeightProperty().bind(previewContainer.heightProperty().subtract(12));
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
    private int getFontStyle(final @NotNull VarTable vars) {

        final String fontStyle = vars.get(PROP_FONT_STYLE);

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
    @FXThread
    protected void processOk() {
        super.processOk();

        final Path fontFile = notNull(getFileToCreate());
        final String filename = FileUtils.getNameWithoutExtension(fontFile);

        final Path parent = fontFile.getParent();
        final Path imageFile = parent.resolve(filename + "." + FileExtensions.IMAGE_PNG);

        final VarTable vars = getVars();
        final Font font = vars.get(PROP_FONT);
        final int imageSize = vars.getInteger(PROP_IMAGE_SIZE);
        final int fontSize = vars.getInteger(PROP_FONT_SIZE);
        final int paddingX = vars.getInteger(PROP_PADDING_X);
        final int paddingY = vars.getInteger(PROP_PADDING_Y);
        final int letterSpacing = vars.getInteger(PROP_LETTER_SPACING);
        final int fontStyle = getFontStyle(vars);
        final char firstChar = (char) vars.getInteger(PROP_FIRST_CHAR);
        final char lastChar = (char) vars.getInteger(PROP_LAST_CHAR);

        final BufferedImage image = buildImage(font.getFontName(), imageSize, fontSize, fontStyle, paddingX, paddingY,
                firstChar, lastChar, false);

        final String description = buildDescription(font.getFontName(), imageFile.getFileName().toString(), imageSize,
                fontSize, fontStyle, paddingX, paddingY, letterSpacing, firstChar, lastChar);

        try (final PrintWriter out = new PrintWriter(Files.newOutputStream(fontFile))) {
            out.print(description);
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return;
        }

        notifyFileCreated(fontFile, true);

        try (final OutputStream out = Files.newOutputStream(imageFile)) {
            ImageIO.write(image, "png", out);
        } catch (final IOException e) {
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
    private @NotNull BufferedImage buildImage(@NotNull final String fontName, final int bitmapSize, final int fontSize,
                                              final int style, final int paddingX, final int paddingY,
                                              final char firstChar, final char lastChar, final boolean debug) {

        final Font font = new Font(fontName, style, fontSize);
        final BufferedImage fontImage = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_4BYTE_ABGR);

        final Graphics2D graphics = (Graphics2D) fontImage.getGraphics();
        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(OPAQUE_WHITE);
        graphics.setBackground(TRANSPARENT_BLACK);

        final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
        final FontMetrics fontMetrics = graphics.getFontMetrics();

        int xPos = 0;
        int height = fontMetrics.getDescent() + fontMetrics.getAscent();
        int yPos = height + (paddingY * 2);

        for (int i = firstChar; i <= lastChar; i++) {

            final char ch = (char) i;

            if (!font.canDisplay(ch)) continue;

            final String str = Character.toString(ch);
            final TextLayout textLayout = new TextLayout(str, font, fontRenderContext);
            final Rectangle2D pixelBounds = textLayout.getPixelBounds(fontRenderContext, xPos, yPos);

            int width = (int) Math.ceil(pixelBounds.getWidth());
            int xOffset = (int) Math.round(pixelBounds.getX()) - xPos;

            if (xPos + width + (paddingX * 2) > bitmapSize) {
                xPos = 0;
                yPos += height + (paddingY * 2);
            }

            graphics.drawString(str, xPos + paddingX - xOffset, yPos + paddingY);

            if (debug) {
                graphics.setColor(Color.BLUE);
                graphics.drawRect(xPos, yPos - fontMetrics.getAscent(), width + (paddingX * 2),
                        height + (paddingY * 2));
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
    private @NotNull String buildDescription(@NotNull final String fontName, @NotNull final String fileName,
                                             final int bitmapSize, final int fontSize, final int style,
                                             final int paddingX, final int paddingY, final int letterSpacing,
                                             final char firstChar, final char lastChar) {

        System.out.println((int) lastChar);

        final Font font = new Font(fontName, style, fontSize);
        final StringBuilder locations = new StringBuilder();
        final BufferedImage fontImage = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_4BYTE_ABGR);

        final Graphics2D graphics = (Graphics2D) fontImage.getGraphics();
        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(OPAQUE_WHITE);
        graphics.setBackground(TRANSPARENT_BLACK);

        final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
        final FontMetrics fontMetrics = graphics.getFontMetrics();

        int xPos = 0;
        int height = fontMetrics.getDescent() + fontMetrics.getAscent();
        int yPos = height + (paddingY * 2);
        int count = 0;

        for (int i = firstChar; i <= lastChar; i++) {

            final char ch = (char) i;

            if (!font.canDisplay(ch)) continue;

            count++;

            final String str = Character.toString(ch);
            final TextLayout textLayout = new TextLayout(str, font, fontRenderContext);
            final Rectangle2D pixelBounds = textLayout.getPixelBounds(fontRenderContext, xPos, yPos);

            int width = (int) Math.ceil(pixelBounds.getWidth());
            int advance = (int) Math.ceil(textLayout.getAdvance());
            int xOffset = (int) Math.round(pixelBounds.getX()) - xPos;

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
