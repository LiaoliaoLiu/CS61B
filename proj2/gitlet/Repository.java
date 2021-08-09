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
     * The files are staged will be moved to be the blobs of the commit,
     * whether or not they're missing from the working directory or changed in the working directory.
     * If no files have been staged, abort. Print the message "No changes added to the commit."
     * Every commit must have a non-blank message. If not, print the error message "Please enter a commit message."
     * */
    public static void commit(String msg, State stateToCommit) {
        Main.terminateWithMsg(stateToCommit.isNoChangeCommit(), "No changes added to the commit.");

        Commit newCommit = new Commit(msg, stateToCommit.getHeadCommit(), stateToCommit);
        stateToCommit.commitStage(newCommit.save());
        stateToCommit.save();
    }

    /**
     * TODO: Unstage the file if it is currently staged for addition.
     * TODO: Stage the file for removal and remove it if it's tracked in current commit.
     * TODO: Don't remove it unless it's tracked in current commit.
     * */
    public static void rm(String filename, State currentState) {
        currentState.rmFile(filename);
        currentState.save();
    }

    public static void log(State currentState) {
        Commit commit = currentState.getHeadCommit();
        do {
            System.out.println("===");
            System.out.println("commit " + commit.getHash());
            //Print Merge here
            System.out.println("Date: " + commit.getDate());
            System.out.println(commit.getMessage());
            System.out.println();
            commit = commit.getFirstParent();
        } while (commit != null);
    }

    public static void debug(String msg) {
        System.out.println("DEBUG: " + msg);
    }
}