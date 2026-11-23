# Editor Toolbar Color

NetBeans IDE plugin that lets the **editor toolbar** have its own color, different from
the main IDE toolbar.

The editor toolbar is the strip at the top of every editor window, with the
**Source / History** buttons and the editor actions next to them (find, bookmarks,
comment, macro recording, ...). Normally it uses the same `ToolBar.background` color
as the main IDE toolbar, and NetBeans has no setting to change only one of them.

The plugin gives that strip the FlatLaf **style class `nbEditorToolbar`**. You choose
the color yourself in NetBeans' custom FlatLaf properties.

Works with the FlatLaf look and feels: FlatLaf Light, FlatLaf Dark, FlatLaf Mac Light
and FlatLaf Mac Dark. Under other look and feels (Metal, GTK, Windows, ...) the plugin
has no effect.

## Setting the color

1. Install the plugin (see [Build and install](#build-and-install)).
2. Open **Tools > Options > Appearance > Look and Feel**.
3. Click **Edit custom properties**. NetBeans opens the file `FlatLaf.properties`
   in the editor.
4. Add a line with the style class **`nbEditorToolbar`** and the color you want:

   ```properties
   [style].nbEditorToolbar = background: #dde6f3
   ```

5. Save the file and **restart NetBeans**. Custom FlatLaf properties are only
   read at startup.

The style name must be exactly `nbEditorToolbar`, and the line has to start with
`[style].`. Colors use the `#RRGGBB` format.

### Different colors for light and dark themes

Add the `[light]` or `[dark]` prefix so each theme gets its own color:

```properties
[light][style].nbEditorToolbar = background: #dde6f3
[dark][style].nbEditorToolbar  = background: #2b3a4a
```

### Only the toolbars, not their panels

Some editor views (not plain source editors) put their toolbar inside a panel, and the
plugin tags that panel too, so the whole strip gets the color. To color only real
toolbar components, use the `ToolBar` type in the key:

```properties
[style]ToolBar.nbEditorToolbar = background: #dde6f3
```

### Where the file is

Clicking **Edit custom properties** is the easiest way, but you can also edit the file
directly in the NetBeans user directory:

| OS      | File                                                                   |
|---------|------------------------------------------------------------------------|
| Linux   | `~/.netbeans/<version>/config/LookAndFeel/FlatLaf.properties`          |
| Windows | `%APPDATA%\NetBeans\<version>\config\LookAndFeel\FlatLaf.properties`  |
| macOS   | `~/Library/Application Support/NetBeans/<version>/config/LookAndFeel/FlatLaf.properties` |

For example, `~/.netbeans/29/config/LookAndFeel/FlatLaf.properties` for NetBeans 29
on Linux. (The exact user directory is shown in **Help > About** as *Userdir*.)

### Reverting

To get the stock look back, delete the `nbEditorToolbar` lines and restart. You can
also disable or uninstall the plugin; that restores the stock look immediately.

## What exactly is colored

- The multiview bar holding the **Source / History / ...** toggle buttons, including
  the second bar shown when the editor is split.
- The toolbar of the active view: the editor actions in *Source*, the history actions
  in *History*, and so on.
- The editor toolbar of editors that are not multiview windows.

The main IDE toolbar, window tabs and editor content are not changed.

## How it works

FlatLaf lets you style components through `[style].<class>` entries in its properties
files. The plugin only puts `nbEditorToolbar` into the `FlatLaf.styleClass` client
property of the toolbars above, and FlatLaf applies the colors you set.

The toolbars are found by NetBeans-internal class names
(`org.netbeans.core.multiview.TabsComponent`,
`org.netbeans.modules.editor.NbEditorToolBar`) and the `MultiViewPanel` client
property. These are not public API, so a future NetBeans release could change them.
If that happens, the plugin simply stops coloring the toolbar and nothing breaks.

## Build and install

```sh
mvn clean install
```

The installable module is `target/nb-editor-toolbar-color-1.0.nbm`
(**Tools > Plugins > Downloaded > Add Plugins...**).





Example config
```
# FlatLaf custom property overrides.
# Save file and restart NetBeans to apply.
# For full documentation see https://www.formdev.com/flatlaf/properties-files/
#                        and https://www.formdev.com/flatlaf/components/
#

# ---- Base colors (title bar, menu bar, toolbars and panels all follow @background) ----
[light]@background                   = #FAFAFD
[light]@foreground                   = #202020
[light]@selectionInactiveBackground  = #E7E7E8
[light]@selectionInactiveForeground  = #202020
[light]Component.borderColor         = #E5E5E5
[light]Separator.foreground          = #E5E5E5

# ---- Projects / Files tree (VS Code sidebar) ----
[light]Tree.background               = #FAFAFD

# ---- Editor tabs ----
[light]Nb.EmptyEditorArea.background = #FFFFFF
[light]EditorTab.background          = #EAEAEA
[light]EditorTab.activeBackground    = #EAEAEA
[light]EditorTab.hoverBackground     = #F2F2F2
[light]EditorTab.selectedBackground  = #FFFFFF
[light]EditorTab.foreground          = #606060
[light]EditorTab.activeForeground    = #606060
[light]EditorTab.selectedForeground  = #202020
[light]EditorTab.underlineHeight     = 0

# ---- Side panel tabs (Projects, Navigator, ...) look flat, like the "Explorer" header ----
[light]ViewTab.background            = #FAFAFD
[light]ViewTab.activeBackground      = #FAFAFD
[light]ViewTab.selectedBackground    = #FAFAFD
[light]ViewTab.hoverBackground       = #F0F0F3
[light]ViewTab.underlineHeight       = 0

# ---- Editor toolbar (your plugin): white, like the breadcrumb row ----
[light][style].nbEditorToolbar       = background: #FFFFFF
```