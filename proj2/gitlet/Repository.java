package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static void add(String filename, State currentState) {
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
    public static void commit(String msg, State currentState) {
        Main.terminateWithMsg(currentState.isNoChangeCommit(), "No changes added to the commit.");

        Commit newCommit = new Commit(msg, currentState.getHeadCommit(), currentState);
        currentState.commitStage(newCommit.save());
        currentState.save();
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * Stage the file for removal and remove it if it's tracked in current commit.
     * Don't remove it unless it's tracked in current commit.
     * */
    public static void rm(String filename, State currentState) {
        currentState.rmFile(filename);
        currentState.save();
    }

    public static void log(State currentState) {
        Commit commit = currentState.getHeadCommit();
        do {
            Repository.printlog(commit);
            commit = commit.getFirstParent();
        } while (commit != null);
    }

    private static void printlog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getHash());
        //Print Merge here
        System.out.println("Date: " + commit.getDate());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void globallog() {
        List<String> fileArray = plainFilenamesIn(COMMITS_DIR);
        for (String commitFilenames : fileArray) {
            Commit commit = Commit.readCommit(commitFilenames);
            printlog(commit);
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line.
     * The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks.
     */
    public static void find(String message) {
        boolean foundCommit = false;
        List<String> fileArray = plainFilenamesIn(COMMITS_DIR);
        for (String commitFilenames : fileArray) {
            Commit commit = Commit.readCommit(commitFilenames);
            String commitMsg = commit.getMessage();
            if (commitMsg.contains(message)) {
                System.out.println(commit.getHash());
                foundCommit = true;
            }
        }
        Main.terminateWithMsg(!foundCommit, "Found no commit with that message.");
    }

    /**
     * Entries should be listed in lexicographic order, using the Java string-comparison order.
     * A file in the working directory is “modified but not staged” if it is
     * - Tracked in the current commit, changed in the working directory, but not staged; or
     * - Staged for addition, but with different contents than in the working directory; or
     * - Staged for addition, but deleted in the working directory; or
     * - Not staged for removal, but tracked in the current commit and deleted from the working directory.
     * The final category (“Untracked Files”) is for files present in the working directory
     * - but neither staged for addition nor tracked. This includes files that
     * - have been staged for removal, but then re-created without Gitlet’s knowledge.
     * */
    public static void status(State currentState) {
        typeOfStatus(currentState, "branches", new BranchPrintFunction());
        typeOfStatus(currentState, "Staged Files");
        typeOfStatus(currentState, "Removed Files");
        typeOfStatus(currentState, "Modifications Not Staged For Commit");
        typeOfStatus(currentState, "Untracked Files");
    }

    private static void typeOfStatus(State currentState, String type) {
        typeOfStatus(currentState, type, new DefaultPrintFunction());
    }

    private static void typeOfStatus(State currentState, String type, EntryPrintFunction f) {
        ArrayList<String> entries = new ArrayList<>();
        for (String entry : currentState.getStatusIter(type)) {
            entries.add(entry);
        }
        Collections.sort(entries);
        System.out.println("=== " + type + " ===");
        for (String entry : entries) {
            f.print(entry, currentState);
        }
        System.out.println();
    }

    private interface EntryPrintFunction {
        void print(String entry, State currentState);
    }

    private static class DefaultPrintFunction implements EntryPrintFunction {
        public void print(String entry, State currentState) {
            System.out.println(entry);
        }
    }

    private static class BranchPrintFunction implements EntryPrintFunction {
        public void print(String branch, State currentState) {
            if (branch.equals(currentState.HEAD)) System.out.println("*" + branch);
            else System.out.println(branch);
        }
    }

    /**
     * Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged. */
    public static void checkoutFile(String filename, State currentState) {
        checkoutFile(filename, currentState.getBranchHash(currentState.HEAD), currentState);
    }

    /**
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * */
    public static void checkoutFile(String filename, String commitID, State currentState) {
        File commitFile = getCommitHashWithShortID(commitID);
        Main.terminateWithMsg(commitFile == null, "No commit with that id exists.");
        Commit commit = Commit.readCommit(commitFile);
        Main.terminateWithMsg(!commit.containsBlob(filename), "File does not exist in that commit.");

        currentState.checkoutFile(filename, commit);
        currentState.save();
    }

    private static File getCommitHashWithShortID(String substring) {
        File dir = COMMITS_DIR;
        File[] files = dir.listFiles((d, name) -> name.contains(substring));
        return files.length > 0 ? files[0] : null;
    }

    public static void debug(String msg) {
        System.out.println("DEBUG: " + msg);
    }
}