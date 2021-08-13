package gitlet;

import static gitlet.Utils.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    /** The runtime object for status command. */
    private transient Status status = null;

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

    private class Status {
        Set<String> modFilesStrings;
        Set<String> stagedFilesStrings;
        Set<String> branchesStrings;
        Set<String> removedFileStrings;
        Set<String> untrackedFilesStrings;

        Status() {
            modFilesStrings = new HashSet<>();
            stagedFilesStrings = new HashSet<>();
            untrackedFilesStrings = new HashSet<>();
            handleWorkingDirFilesStatus();
            branchesStrings = State.this.branches.keySet();
            removedFileStrings = State.this.removedFiles;
        }

        void handleWorkingDirFilesStatus() {
            Commit head = State.this.getHeadCommit();
            HashMap<String, String> trackedFiles = head.getBlobsMap();
            HashMap<String, String> stagedAddedFiles = State.this.addedFiles;
            List<String> workingDirFiles = plainFilenamesIn(Repository.CWD);
            for (String filename : workingDirFiles) {
                // The status command won't change the state's persistence, so it should be fine to mutate.
                String trackedFileHash = trackedFiles.remove(filename);
                boolean isTracked = (trackedFileHash != null);
                String stagedFileHash = stagedAddedFiles.remove(filename);
                boolean isStaged = (stagedFileHash != null);
                String workingDirFileHash = getFileHash(filename, Repository.CWD);

                if (isTracked && !isStaged) {
                    if (State.this.removedFiles.contains(filename)) {
                        // Untracked files include files that have been staged for removal, but then re-created.
                        untrackedFilesStrings.add(filename);
                    } else if (!workingDirFileHash.equals(trackedFileHash)) {
                        // Tracked in the current commit, changed in the working directory, but not staged
                        modFilesStrings.add(filename + " (modified)");
                    }
                } else if (isStaged) {
                    if (!workingDirFileHash.equals(stagedFileHash)) {
                        // Staged for addition, but with different contents than in the working directory
                        modFilesStrings.add(filename + " (modified)");
                    } else {
                        // Otherwise, it's just a staged file.
                        stagedFilesStrings.add(filename);
                    }
                } else {
                    // Present in the working directory but neither staged for addition nor tracked.
                    untrackedFilesStrings.add(filename);
                }
            }

            // Not staged for removal, but tracked in the current commit and deleted from the working directory.
            for (String filename : trackedFiles.keySet()) {
                if (!State.this.removedFiles.contains(filename)) {
                    modFilesStrings.add(filename + " (deleted)");
                }
            }

            // Staged for addition, but deleted in the working directory
            for (String filename : stagedAddedFiles.keySet()) {
                modFilesStrings.add(filename+ " (deleted)");
            }
        }

    }

    public Iterable<String> getStatusIter(String type) {
        Iterable<String> typeIter = null;
        if (status == null) status = new Status();

        if (type.equals("branches")) typeIter = status.branchesStrings;
        else if (type.equals("Staged Files")) typeIter = status.stagedFilesStrings;
        else if (type.equals("Removed Files")) typeIter = status.removedFileStrings;
        else if (type.equals("Modifications Not Staged For Commit"))
            typeIter = status.modFilesStrings;
        else if (type.equals("Untracked Files")) typeIter = status.untrackedFilesStrings;
        return typeIter;
    }

    /** Save the changes to the file. */
    public void save() {
        writeObject(STATE, this);
    }

    /** Add a file to the staging area.
     * If the current working version of the file is identical to the version in the current commit,
     * do not stage it to be added,
     * and remove it from the staging area if it is already there
     *
     * */
    public void addFile(String filename) {
        File fileToAdd = join(Repository.CWD, filename);
        Main.terminateWithMsg(!fileToAdd.exists(), "File does not exist.");

        String fileHash = getFileHash(fileToAdd);
        Commit head = getHeadCommit();
        String blobHash = head.getBlobHash(filename);
        if (fileHash.equals(blobHash)) { // delete the file in STAGE if newly added file is the same as the file in commit
            if (isStaged(filename)) {
                String stagedFileHash = this.addedFiles.remove(filename);
                deleteIfExists(join(STAGE_DIR, stagedFileHash));
                this.removedFiles.remove(filename);
            }
        } else {
            this.addedFiles.put(filename, fileHash);
            File fileInStage = join(STAGE_DIR, fileHash);
            copyFile(fileToAdd, fileInStage);
        }
    }

    private boolean isStaged(String filename) {
        return addedFiles.containsKey(filename);
    }

    private String getFileHash(File file) {
        return sha1(readContents(file));
    }

    private String getFileHash(String name, File dir) {
        File file = join(dir, name);
        return file.exists() ? getFileHash(file) : null;
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
        moveFilesInFolder(STAGE_DIR, Repository.BLOBS_DIR);
        this.putBranch(HEAD, newCommitHash);
        this.addedFiles = new HashMap<>();
        this.removedFiles = new HashSet<>();
    }

    public boolean isNoChangeCommit() {
        return addedFiles.isEmpty() && removedFiles.isEmpty();
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

    public void rmFile(String filename) {
        boolean isAddedFile, isBlob;
        isAddedFile = addedFiles.containsKey(filename);
        isBlob = getHeadCommit().containsBlob(filename);
        if (!(isAddedFile || isBlob)) {
            Main.terminateWithMsg(true, "No reason to remove the file.");
        }
        if (isAddedFile) {
            this.unstage(filename);
        }
        if (isBlob) {
            stageForRm(filename);
            File file = join(Repository.CWD, filename);
            deleteIfExists(file);
        }
    }

    private void unstage(String filename) {
        this.addedFiles.remove(filename);
    }

    private void stageForRm(String filename) {
        this.removedFiles.add(filename);
    }

    /**
     * */
     public void checkoutFile(String filename, Commit commit) {
        File blob = join(Repository.BLOBS_DIR, commit.getBlobHash(filename));
        File thisFile = join(Repository.CWD, filename);
        copyFile(blob, thisFile);

        this.removedFiles.remove(filename);
        this.addedFiles.remove(filename);
    }

}

