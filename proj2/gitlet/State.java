package gitlet;

import static gitlet.Utils.*;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/** Represents the repository's persistence object.
 *
 *  @author TODO
 */
public class State implements Serializable {
    /** The branchName-SHA1 HashMap. */
    private HashMap<String, String> branches;
    private HashSet<String> removedFiles;
    private HashMap<String, String> addedFiles;
    /** The file stores branches persistence. */
    public static final File STATE = join(Repository.GITLET_DIR, "state");

    /** Initialize the persistence of the repository. It should only be called by init(). */
    public State(String sha1) {
        branches = new HashMap<>();
        addBranch("master", sha1);
        addBranch("HEAD", sha1);
        removedFiles = new HashSet<>();
        addedFiles = new HashMap<>();
        save();
    }

    /** Add a branch called name and serialize the class. */
    public void addBranch(String name, String sha1) {
        branches.put(name, sha1);
    }

    /** Return the commit SHA1 to which branch `name` points. */
    public String getBranchCommit(String name) {
        return branches.get(name);
    }

    /** Save the changes to the file. */
    public void save() {
        writeObject(STATE, this);
    }

    /** Add a file to the staging area. */
    public void addFile(String filename) {
        File fileToAdd = join(Repository.CWD, filename);
        String commitHash = getBranchCommit("HEAD");
        Commit head = Commit.readCommit(commitHash);

        Main.terminateWithMsg(!fileToAdd.exists(), "File does not exist.");

        String fileHash = getFileHash(fileToAdd);
        String blobHash = head.getBlobHash(filename);
        if (fileHash == blobHash) {
            this.addedFiles.remove(filename);
            this.removedFiles.remove(filename);
        } else {
            this.addedFiles.put(filename, fileHash);
        }
    }

    private String getFileHash(File file) {
        return sha1(readContents(file));
    }

    public static State readState() {
        return readObject(STATE, State.class);
    }
}

