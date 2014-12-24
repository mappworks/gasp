package jasp.core.model;

/**
 * Base class for objects containined (optionally) within a folder.
 */
public class ContainedObject extends NamedObject {

    Folder folder;

    public Folder folder() {
        return folder;
    }

    public ContainedObject folder(Folder folder) {
        this.folder = folder;
        return this;
    }
}
