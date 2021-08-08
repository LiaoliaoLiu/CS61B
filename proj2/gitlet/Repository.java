package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The Commits directory. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    /** The blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");

    /* TODO: fill in the rest of this class. */
    public static void init() {
        Main.terminateWithMsg(GITLET_DIR.exists(), "A Gitlet version-control system already exists in the current directory.");

        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        Commit initCommit = new Commit();
        new State(initCommit.save());
    }

    public static void add(String filename) {
        State currentState = State.readState();
        currentState.addFile(filename);
        currentState.save();
    }

    /**
     * TODO: save a snapshot of tracked files
     * TODO: The staging area is cleared after a commit.
     * TODO: It is not a failure for tracked files to be missing from the working directory or changed in the working directory.
     * TODO: Gitlet doesn't support detached HEAD state.
     * */
    public static void commit(String msg, State stateToCommit) {
        Commit newCommit = new Commit(msg, stateToCommit.getHeadCommit(), stateToCommit);
        moveFilesInFolder(State.STAGE_DIR, Repository.BLOBS_DIR);
        stateToCommit.clearStage();
        stateToCommit.putBranch(stateToCommit.HEAD, newCommit.save());
        stateToCommit.save();
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

    public static void debug(String msg) {
        System.out.println("DEBUG: " + msg);
    }
}