package gitlet;

import java.io.File;

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
     * Save a snapshot of tracked files.
     * The staging area is cleared after a commit.
     * It is not a failure for tracked files to be missing from the working directory or changed in the working directory.
     * If no files have been staged, abort. Print the message No changes added to the commit.
     * Every commit must have a non-blank message. If it doesn’t, print the error message Please enter a commit message.
     * */
    public static void commit(String msg, State stateToCommit) {
        Commit newCommit = new Commit(msg, stateToCommit.getHeadCommit(), stateToCommit);
        stateToCommit.commitStage(newCommit.save());
        stateToCommit.save();
    }

    public static void debug(String msg) {
        System.out.println("DEBUG: " + msg);
    }
}