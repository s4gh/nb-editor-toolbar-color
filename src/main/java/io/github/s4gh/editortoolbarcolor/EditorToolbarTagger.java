package io.github.s4gh.editortoolbarcolor;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ContainerEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * Puts the FlatLaf style class {@value #STYLE_CLASS} on the toolbar shown at
 * the top of editor windows, so FlatLaf can give it its own colors through
 * {@code [style].nbEditorToolbar = ...} in the custom FlatLaf properties.
 * <p>
 * The strip consists of (classes are NetBeans internals, not public API):
 * <ul>
 * <li>the multiview bar - a {@link JToolBar} added to
 * {@code org.netbeans.core.multiview.TabsComponent} holding the
 * Source / History / ... toggle buttons;</li>
 * <li>the toolbar of the active multiview element, added into that bar and
 * marked by the client property {@value #MULTIVIEW_PANEL_KEY}; for source
 * editors it is {@code org.netbeans.modules.editor.NbEditorToolBar}.</li>
 * </ul>
 * Both are created lazily and re-added when switching views, so a global
 * container listener is used to catch them whenever they are added.
 * <p>
 * Without a matching {@code [style]} entry, or under a look and feel other
 * than FlatLaf, the style class has no effect.
 */
final class EditorToolbarTagger implements AWTEventListener {

    /** FlatLaf style class put on the editor toolbars. */
    static final String STYLE_CLASS = "nbEditorToolbar"; //NOI18N

    /** FlatLaf client property holding the style class(es) of a component. */
    private static final String STYLE_CLASS_KEY = "FlatLaf.styleClass"; //NOI18N
    /** Client property the multiview component puts on the element toolbar it shows. */
    private static final String MULTIVIEW_PANEL_KEY = "MultiViewPanel"; //NOI18N
    private static final String TABS_COMPONENT_CLASS = "org.netbeans.core.multiview.TabsComponent"; //NOI18N
    private static final String EDITOR_TOOLBAR_CLASS = "org.netbeans.modules.editor.NbEditorToolBar"; //NOI18N

    void install() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.CONTAINER_EVENT_MASK);
        // toolbars that already exist (e.g. module enabled in a running IDE)
        SwingUtilities.invokeLater(() -> forAllComponents(this::tagIfEditorToolbar));
    }

    void uninstall() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        SwingUtilities.invokeLater(() -> forAllComponents(EditorToolbarTagger::untag));
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event.getID() != ContainerEvent.COMPONENT_ADDED) {
            return;
        }
        Component child = ((ContainerEvent) event).getChild();
        if (EventQueue.isDispatchThread()) {
            tagIfEditorToolbar(child);
        } else {
            SwingUtilities.invokeLater(() -> tagIfEditorToolbar(child));
        }
    }

    private void tagIfEditorToolbar(Component c) {
        if (!(c instanceof JComponent jc)) {
            return;
        }
        Container parent = c.getParent();
        if (jc.getClientProperty(MULTIVIEW_PANEL_KEY) != null) {
            // toolbar of the active multiview element (editor actions, History actions, ...)
            tag(jc);
            tagToolBarsInside(jc);
            // the bar it lives in (main bar or the bar of the split view)
            if (parent instanceof JToolBar bar) {
                tag(bar);
            }
        } else if (c instanceof JToolBar bar && parent != null
                && TABS_COMPONENT_CLASS.equals(parent.getClass().getName())) {
            // Source / History toggle buttons
            tag(bar);
        } else if (EDITOR_TOOLBAR_CLASS.equals(c.getClass().getName())) {
            // editor toolbar outside of a multiview window
            tag(jc);
        }
    }

    /** Element toolbars may be panels wrapping one or more real toolbars. */
    private static void tagToolBarsInside(Container container) {
        for (Component child : container.getComponents()) {
            if (child instanceof JToolBar bar) {
                tag(bar);
            }
            if (child instanceof Container nested && !(child instanceof JToolBar)) {
                tagToolBarsInside(nested);
            }
        }
    }

    private static void tag(JComponent c) {
        Object current = c.getClientProperty(STYLE_CLASS_KEY);
        if (current == null) {
            c.putClientProperty(STYLE_CLASS_KEY, STYLE_CLASS);
        } else if (current instanceof String s && !hasStyleClass(s)) {
            c.putClientProperty(STYLE_CLASS_KEY, s + " " + STYLE_CLASS);
        } else {
            return;
        }
        syncEditorToolbarBorder(c);
    }

    private static void untag(Component comp) {
        if (!(comp instanceof JComponent c)
                || !(c.getClientProperty(STYLE_CLASS_KEY) instanceof String s)
                || !hasStyleClass(s)) {
            return;
        }
        String rest = String.join(" ", Arrays.stream(s.trim().split("\\s+"))
                .filter(cls -> !cls.equals(STYLE_CLASS))
                .toList());
        c.putClientProperty(STYLE_CLASS_KEY, rest.isEmpty() ? null : rest);
        syncEditorToolbarBorder(c);
    }

    private static boolean hasStyleClass(String styleClasses) {
        for (String cls : styleClasses.trim().split("\\s+")) {
            if (cls.equals(STYLE_CLASS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code NbEditorToolBar.addNotify()} sets a 1px {@link LineBorder} in its
     * background color at that moment. If the toolbar was already displayable
     * when it got (un)tagged, that border still has the old color and would
     * show up as a frame, so repaint it with the current background.
     */
    private static void syncEditorToolbarBorder(JComponent c) {
        if (!EDITOR_TOOLBAR_CLASS.equals(c.getClass().getName())) {
            return;
        }
        Border border = c.getBorder();
        Color bg = c.getBackground();
        if (border instanceof LineBorder line && line.getClass() == LineBorder.class
                && bg != null && !bg.equals(line.getLineColor())) {
            c.setBorder(new LineBorder(bg, line.getThickness()));
        }
    }

    private static void forAllComponents(Consumer<Component> action) {
        for (Window w : Window.getWindows()) {
            walk(w, action);
        }
    }

    private static void walk(Component c, Consumer<Component> action) {
        action.accept(c);
        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                walk(child, action);
            }
        }
    }
}
