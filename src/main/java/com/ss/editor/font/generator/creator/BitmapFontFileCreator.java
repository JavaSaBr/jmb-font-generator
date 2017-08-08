package com.ss.editor.font.generator.creator;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.FileExtensions;
import com.ss.editor.font.generator.Messages;
import com.ss.editor.ui.component.creator.FileCreatorDescription;
import com.ss.editor.ui.component.creator.impl.AbstractFileCreator;
import com.ss.editor.util.EditorUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * THe example of a file creator.
 *
 * @author JavaSaBr
 */
public class BitmapFontFileCreator extends AbstractFileCreator {

    /**
     * The constant DESCRIPTION.
     */
    @NotNull
    public static final FileCreatorDescription DESCRIPTION = new FileCreatorDescription();

    static {
        DESCRIPTION.setFileDescription(Messages.EXAMPLE_FILE_CREATOR_TITLE);
        DESCRIPTION.setConstructor(BitmapFontFileCreator::new);
    }

    @NotNull
    @Override
    protected String getTitleText() {
        return Messages.EXAMPLE_FILE_CREATOR_TITLE;
    }

    @NotNull
    @Override
    protected String getFileExtension() {
        return FileExtensions.GLSL_VERTEX;
    }

    @Override
    protected void processOk() {
        super.processOk();

        final Path fileToCreate = notNull(getFileToCreate());
        try {
            Files.createFile(fileToCreate);
            Files.write(fileToCreate, "Hello World".getBytes());
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return;
        }

        notifyFileCreated(fileToCreate, true);
    }
}
