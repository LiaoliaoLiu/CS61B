package gitlet;

// TODO: any imports you need here
import static gitlet.Utils.*;
import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** The timestamp of this Commit. */
    private String timestamp;

    /* TODO: fill in the rest of this class. */
    public Commit(String msg) {
        this.message = msg;
        this.timestamp = msg == "initial commit" ? new Date(0).toString() : new Date().toString();
    }

    /** Save a commit and return the sha1 string of the commit. */
    public String save() {
        byte[] data = serialize(this);
        String commitS = sha1(data);
        File commit = join(Repository.COMMITS_DIR, commitS);
        writeContents(commit, data);
        return commitS;
    }
}
