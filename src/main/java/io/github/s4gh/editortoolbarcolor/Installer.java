package io.github.s4gh.editortoolbarcolor;

import org.openide.modules.ModuleInstall;

/**
 * Starts tagging the editor toolbars when the module starts and removes the
 * tags (restoring the stock look) when it is disabled or uninstalled.
 */
public class Installer extends ModuleInstall {

    private static final EditorToolbarTagger TAGGER = new EditorToolbarTagger();

    @Override
    public void restored() {
        // runs before the main window is shown, so the listener sees the
        // editor toolbars as they are created
        TAGGER.install();
    }

    @Override
    public void uninstalled() {
        TAGGER.uninstall();
    }
}
