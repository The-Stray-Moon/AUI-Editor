package cc.sighs.auieditor.editor.ore.palette;

import cc.sighs.auieditor.editor.ore.model.OreCanvasNode;
import cc.sighs.auieditor.editor.ore.model.OreComponentNode;
import cc.sighs.auieditor.editor.ore.model.OreContainerNode;

public record OreComponentDefinition(String id, String nameKey, String descriptionKey, boolean container,
                                     String tagName, String defaultContent) {
    public OreComponentDefinition(String id, String nameKey, String descriptionKey, boolean container) {
        this(id, nameKey, descriptionKey, container, container ? null : id, container ? null : "");
    }

    public OreCanvasNode createNode() {
        if (container) {
            OreContainerNode node = new OreContainerNode(false);
            node.flex().setDirection("column".equals(id) ? "column" : "row");
            node.style().set("min-height", "64px");
            node.style().set("padding", "8px");
            return node;
        }
        return new OreComponentNode(tagName == null || tagName.isBlank() ? "div" : tagName,
                defaultContent == null ? "" : defaultContent);
    }
}
