package gitlet;

import static gitlet.Utils.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/** Represents the repository's persistence object.
 *
 *  @author TODO
 */
public class State implements Serializable {
    /** The branchName-SHA1 HashMap. */
    private HashMap<String, String> branches;
    /** The HEAD pointer. */
    public String HEAD;
    /** The detached head flag. */
    public boolean isDetached = false;
    /** The removed files' filename set. */
    public HashSet<String> removedFiles;
    /** The added files' filename-SHA1 HashMap. */
    public HashMap<String, String> addedFiles;
    /** The file stores branches persistence. */
    public static final File STATE = join(Repository.GITLET_DIR, "state");
    /** The staging area directory. */
    public static final File STAGE_DIR = join(Repository.GITLET_DIR, "stage");
    /** The runtime object for status command. */
    private transient Status status = null;
    /** The runtime set for storing all tracked filenames. */
    private transient HashSet<String> trackedFiles = null;

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

    /** Add a branch called `name`.*/
    public void putBranch(String name, String sha1) { branches.put(name, sha1); }

    /** Remove a branch called `name`. */
    public void rmBranch(String name) {
        Main.terminateWithMsg(!branches.containsKey(name), "A branch with that name does not exist.");
        Main.terminateWithMsg(HEAD.equals(name), "Cannot remove the current branch.");

        branches.remove(name);
    }

    /** Return the commit object to which the branch `name` points. */
    public Commit getBranchCommit(String name) { return Commit.readCommit(getBranchHash(name)); }

    /** Return the commit object to which the HEAD points. */
    public Commit getHeadCommit() {
        if (isDetached) {
            return Commit.readCommit(HEAD);
        }
        return getBranchCommit(HEAD);
    }

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

                if (isTracked && !isStaged) {
                    String workingDirFileHash = getFileHash(filename, Repository.CWD);
                    if (State.this.removedFiles.contains(filename)) {
                        // Untracked files include files that have been staged for removal, but then re-created.
                        untrackedFilesStrings.add(filename);
                    } else if (!workingDirFileHash.equals(trackedFileHash)) {
                        // Tracked in the current commit, changed in the working directory, but not staged
                        modFilesStrings.add(filename + " (modified)");
                    }
                } else if (isStaged) {
                    String workingDirFileHash = getFileHash(filename, Repository.CWD);
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

    /**
     * Add a file to the staging area.
     * If the current working version of the file is identical to the version in the current commit,
     * do not stage it to be added,
     * and remove it from the staging area if it is already there
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
            if (!this.isDuplicateFile(fileHash)) {
                copyFile(fileToAdd, fileInStage);
            }
        }
    }

    private boolean isDuplicateFile(String hash) {
        return isDuplicateFile(Repository.BLOBS_DIR, hash) || isDuplicateFile(STAGE_DIR, hash);
    }

    private boolean isDuplicateFile(File dir, String filename) {
        List<String> filenamesInDir = plainFilenamesIn(dir);
        for (String f : filenamesInDir) {
            if (f.equals(filename)) {
                return true;
            }
        }
        return false;
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
        if (!isDetached) {
            this.putBranch(HEAD, newCommitHash);
        }
        cleanStagingArea();
    }

    private void cleanStagingArea() {
        this.addedFiles = new HashMap<>();
        this.removedFiles = new HashSet<>();
        cleanDir(STAGE_DIR);
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

    /**
     * TODO: Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     * TODO: overwriting the versions of the files that are already there if they exist.
     * TODO: the given branch will now be considered the current branch
     * TODO: Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     * TODO: The staging area is cleared, unless the checked-out branch is the current branch
     * */
    public void checkoutBranch(String name) {
        this.checkoutCommit(this.getBranchCommit(name));
        HEAD = name;
        isDetached = false;
    }

    private void checkoutCommit(Commit commit) {
        File workingDir = Repository.CWD;
        // Because there's no .gitignore in gitlet,
        // which means all the files that are not staged or in blobs are untracked files.
        // So I just delete all the working dir files.
        cleanDir(workingDir);

        for (Map.Entry<String, String> entry: commit.getBlobsMap().entrySet()) {
            File blob = join(Repository.BLOBS_DIR, entry.getValue());
            File file = join(Repository.CWD, entry.getKey());
            copyFile(blob, file);
        }

        cleanStagingArea();
    }

    private void cleanDir(File dir) {
        List<String> filenamesInDir = plainFilenamesIn(dir);
        for (String filename : filenamesInDir) {
            File file = join(Repository.CWD, filename);
            deleteIfExists(file);
        }
    }

    public boolean isTrackedFiles(String filename) {
        if (trackedFiles == null) {
            trackedFiles = new HashSet<>();
            trackedFiles.addAll(addedFiles.keySet());
            trackedFiles.addAll(getHeadCommit().getBlobsMap().keySet());
        }

        return trackedFiles.contains(filename);
    }

    public void reset(Commit commit) {
        this.checkoutCommit(commit);
        putBranch(HEAD, commit.getHash());
    }
}

