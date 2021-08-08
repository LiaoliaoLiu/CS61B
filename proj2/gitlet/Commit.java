package gitlet;

// TODO: any imports you need here
import static gitlet.Utils.*;
import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  This class should only deal with its own persistence.
 *  @author Liaoliao Liu
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
    private Date timestamp;
    /** The name-sha1 map of blobs */
    private HashMap<String, String> blobs;
    /** The parent commit(s) hash*/
    private String[] parents;

    /* TODO: fill in the rest of this class. */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.blobs = new HashMap<>();
        this.parents = null;
    }

    public Commit(String msg, Commit parent, State stateToCommit) {
        this.message = msg;
        this.timestamp = new Date();
        this.parents = new String[]{parent.getHash()};
        this.blobs = parent.blobs;
        updateBlobs(stateToCommit);
    }

    private void updateBlobs(State stateToCommit) {
        this.blobs.putAll(stateToCommit.addedFiles);
        for (String file : stateToCommit.removedFiles) {
            this.blobs.remove(file);
        }
    }


    /** Save a commit and return the sha1 string of the commit. */
    public String save() {
        byte[] data = serialize(this);
        String commitHash = sha1(data);
        File commit = join(Repository.COMMITS_DIR, commitHash);
        writeContents(commit, data);
        return commitHash;
    }

    public String getHash() {
        byte[] data = serialize(this);
        String commitHash = sha1(data);
        return commitHash;
    }

    public static Commit readCommit(String hash) {
        File commit = join(Repository.COMMITS_DIR, hash);
        return readObject(commit, Commit.class);
    }

    public String getBlobHash(String filename) {
        return blobs.get(filename);
    }

    public boolean containsBlob(String filename) {
        return blobs.containsKey(filename);
    }

}
