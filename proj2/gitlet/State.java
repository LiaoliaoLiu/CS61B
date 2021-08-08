package gitlet;

import static gitlet.Utils.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;

/** Represents the repository's persistence object.
 *
 *  @author TODO
 */
public class State implements Serializable {
    /** The branchName-SHA1 HashMap. */
    private HashMap<String, String> branches;
    public String HEAD;
    public HashSet<String> removedFiles;
    public HashMap<String, String> addedFiles;
    /** The file stores branches persistence. */
    public static final File STATE = join(Repository.GITLET_DIR, "state");
    /** The staging area directory. */
    public static final File STAGE_DIR = join(Repository.GITLET_DIR, "stage");

    /** Initialize the persistence of the repository. It should only be called by init(). */
    public State(String sha1) {
        branches = new HashMap<>();
        putBranch("master", sha1);
        HEAD = "master";
        removedFiles = new HashSet<>();
        addedFiles = new HashMap<>();
        STAGE_DIR.mkdir();
        save();
    }

    /** Add a branch called name and serialize the class. */
    public void putBranch(String name, String sha1) {
        branches.put(name, sha1);
    }

    /** Return the commit object to which the branch `name` points. */
    public Commit getBranchCommit(String name) { return Commit.readCommit(getBranchHash(name)); }

    /** Return the commit object to which the HEAD points. */
    public Commit getHeadCommit() {return getBranchCommit(HEAD);}

    /** Return the commit Hash to which the branch `name` points. */
    public String getBranchHash(String name) {
        return branches.get(name);
    }

    /** Save the changes to the file. */
    public void save() {
        writeObject(STATE, this);
    }

    /** Add a file to the staging area.
     * If the current working version of the file is identical to the version in the current commit,
     * do not stage it to be added,
     * and remove it from the staging area if it is already there */
    public void addFile(String filename) {
        File fileToAdd = join(Repository.CWD, filename);
        Main.terminateWithMsg(!fileToAdd.exists(), "File does not exist.");

        String fileHash = getFileHash(fileToAdd);
        Commit head = getHeadCommit();
        String blobHash = head.getBlobHash(filename);
        if (fileHash.equals(blobHash)) { // delete the file in STAGE if newly added file is the same as the file in commit
            String stagedFileHash = this.addedFiles.remove(filename);
            deleteIfExists(join(STAGE_DIR, stagedFileHash));
            this.removedFiles.remove(filename);
        } else {
            this.addedFiles.put(filename, fileHash);
            File fileInStage = join(STAGE_DIR, fileHash);
            copyFile(fileToAdd, fileInStage);
        }
    }

    private String getFileHash(File file) {
        return sha1(readContents(file));
    }

    private static void copyFile(File src, File dst) {
        try {
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteIfExists(File fileToDelete) {
        try {
            Files.deleteIfExists(fileToDelete.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static State readState() {
        return readObject(STATE, State.class);
    }

    public void commitStage(String newCommitHash) {
        Main.terminateWithMsg(addedFiles.isEmpty() && removedFiles.isEmpty(),
                "No changes added to the commit.");

        moveFilesInFolder(STAGE_DIR, Repository.BLOBS_DIR);
        this.putBranch(HEAD, newCommitHash);
        this.addedFiles = new HashMap<>();
        this.removedFiles = new HashSet<>();
    }

    private static void moveFilesInFolder(File srcFolder, File dstFolder) {
        File[] files = srcFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                Files.move(files[i].toPath(), dstFolder.toPath().resolve(files[i].getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

