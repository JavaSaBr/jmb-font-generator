package com.ss.editor.font.generator.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.ss.rlib.common.concurrent.util.ThreadUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

/**
 * The test ot test loading generated fonts.
 *
 * @author JavaSaBr
 */
public class FontTest extends SetUpTest {

    @NotNull
    private static final String TEST = "qazxswedcиеепитрнг1231224";

    @Test
    public void loadFontTest() {

        final SimpleApplication application = getApplication();
        final AssetManager assetManager = application.getAssetManager();
        final BitmapFont testFont = assetManager.loadFont("testFont.fnt");
        final BitmapFont sdkFont = assetManager.loadFont("sdkFont.fnt");
        final BitmapFont defaultFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        final BitmapText testText = new BitmapText(testFont, false);
        testText.setSize(testFont.getPreferredSize() * 2f);
        testText.setText(TEST);
        testText.setLocalTranslation(0, testText.getHeight() * 6 + 100, 0);

        final BitmapText sdkText = new BitmapText(sdkFont, false);
        sdkText.setSize(sdkFont.getPreferredSize() * 2f);
        sdkText.setText(TEST);
        sdkText.setLocalTranslation(0, testText.getHeight() * 3 + 100, 0);

        final BitmapText defaultText = new BitmapText(defaultFont, false);
        defaultText.setSize(defaultFont.getPreferredSize() * 2f);
        defaultText.setText(TEST);
        defaultText.setLocalTranslation(0, defaultText.getHeight() + 100, 0);

        application.enqueue(() -> application.getGuiNode().attachChild(testText));
        application.enqueue(() -> application.getGuiNode().attachChild(sdkText));
        application.enqueue(() -> application.getGuiNode().attachChild(defaultText));

        ThreadUtils.sleep(10000);
    }
}
