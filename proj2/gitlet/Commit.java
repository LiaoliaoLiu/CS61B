package gitlet;

// TODO: any imports you need here
import static gitlet.Utils.*;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
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
        File commitTempFile = join(Repository.COMMITS_DIR, "temp");
        writeObject(commitTempFile, this);

        Commit thisCommit = readObject(commitTempFile, Commit.class); // See below
        byte[] data = serialize(thisCommit);
        // I don't know why but the hash value calculated in object creating time is different from when deserializing.
        // That's:
        // String hash1 = sha1(serialize(this))
        // ## Save serialize(this) to a file and end the program.
        // String hash2 = sha1(readObject(FileOfPrevThis, Commit.class).getHash())
        // ## End the program
        // String hash3 = sha1(readObject(FileOfPrevThis, Commit.class).getHash())
        // hash2 == hash3 but != hash3
        // Though the only difference is that hash1 value is got from `this` at the runtime when the object was created
        // , and hash2 and hash3 are got from the object that is from deserializing the file,
        // which should be an identical to `this`.

        String commitHash = sha1(data);
        File commitFile = join(Repository.COMMITS_DIR, commitHash);
        commitTempFile.renameTo(commitFile);
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

    public Commit getFirstParent() {
        return this.parents == null ? null : readCommit(this.parents[0]);
    }

    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        return formatter.format(this.timestamp);
    }

    public String getMessage() {
        return this.message;
    }

    public HashMap<String, String> getBlobsMap() {
        return this.blobs;
    }
}
