# Gitlet Design Document

**Liaoliao Liu**:

## Classes and Data Structures

### Main

This is the entry point to our program. It takes in arguments from the command line and based on the command (the first element of the `args` array) calls the corresponding command in `Repository` which will actually execute the logic of the command. It also validates the arguments based on the command to ensure that enough arguments were passed in.

#### Fields

This class has no fields and hence no associated state: it simply validates arguments and defers the execution to the `Repository` class.

### Repository

This is where the main logic of our program will live. Its implementation is above the abstraction of `Commit` and `State`.

#### Fields

1. `public static final File CWD = new File(System.getProperty("user.dir"))` The Current Working Directory.
2. `public static final File GITLET_DIR = join(CWD, ".gitlet")` The hidden `.gitlet` directory. This is where all of the state of the `Repository` will be stored, including `blob` and `commit`
3. `public static final File COMMITS_DIR = join(GITLET_DIR, "commits")` The commits directory. Each `Commit` object' persistence will be stored here with its sha1 value as its file name.
4. `public static final File BLOBS_DIR = join(GITLET_DIR, "blobs")` The blobs directory. This is where the blobs in committed commits will be stored with its sha1 value as its file name.

### Commit

This class represents a `Commit` that will be stored in a file. The file's name will be the corresponding sha1 string of the commit to make it unique.

All `Commit` objects are serialized within the `commits` folder which is within the `.gitlet` folder. The save method will serialize the `Commit` using the object's sha1 hash as the filename and return this sha1 string.

#### Fields

1. `private String message` The message of this commit.
2. `private Date timestamp` The timestamp of this commit.
3. `private HashMap<String, String> blobs` The name-sha1 map of blobs that this commit tracks.
4. `private String[] parents` The parent commit(s) hash.

### State

This class represents the state of the current repository, including branches, HEAD pointer and the staging area. Any operation that involves manipulating these three objects will be handled here. There is only one `State` object for each repository. The object will be serialized within the `.gitlet` using the filename `state` whenever the state of the repository has changed.

#### Fields

1. `private HashMap<String, String> branches` The map of `branch name` - `commit hash`.
2. `public String HEAD` The branch name that HEAD points at. Because Gitlet doesn't support detached HEAD state, it's just a String variable that stores an existing branch name.
3. `public HashSet<String> removedFiles` The set of files that are staged for removal.
4. `public HashMap<String, String> addedFiles` The map of `file name` - `file hash` that are staged for add.
5. `public static final File STATE = join(Repository.GITLET_DIR, "state")` The persistence file for the current repository state.
6. `public static final File STAGE_DIR = join(Repository.GITLET_DIR, "stage")` The directory that stores the staged files.
7. `private transient Status status = null` The runtime object that stores status strings for `gitlet status` command.

## Algorithms

### TODO
1. For each class, include a high-level description of the methods in that class, including any edge cases you are accounting for.
2. Simple explanations are preferred. Here are some formatting tips:
   1. For complex tasks, we recommend that you split the task into parts.

### Main.java

1. `public static void main(String[] args)` The main method that deals with user input.

### Repository.java

1. `public static void init()` The `init` command implementation. The runtime objects of gitlet will be first set and serialized here.
2. `public static void add(String filename, State currentState)` The `add [filename]` command implementation. It refers staging area manipulation logic to `State.addFile` method.  See [the add section](###add).
3. `public static void commit(String msg, State currentState)` The `commit [message]` command implementation. It refers staging area manipulation logic to `State.commitStage` method, and a new commit is made by calling the `Commit` constructor.
4. `public static void rm(String filename, State currentState)` The `rm [filename]` command implementation. It refers staging area manipulation logic to `State.rmFile` method.
5. `public static void log(State currentState)` The `log` command implementation. It prints commit hash, date and message from the HEAD to the initial command.
6. `public static void globallog()` The `global-log` command implementation. It will use the `Utils.plainFilenamesIn` method to get all the commit persistence filenames, iterates them and prints the status of all the commits accordingly.
7. `public static void find(String message)` The `find [message]` command implementation. It will print all the commit IDs which have the message as a substring.
8. `public static void status(State currentState)` The `status` command implementation. It calls the `typeOfStatus` function to print different sections of the repository's status in a message dispatching style.
9. `private static void typeOfStatus(State currentState, String type, EntryPrintFunction f)` The underlying logic for printing the status of a repository. It prints the status section of a given `type` using a HoF `f`. See [the status section](###status).
10. `public static void checkoutFile(String filename, String commitID, State currentState)` The part 1 and 2 of the checkout command implementation. It supports short commit ID by filtering the files of `Commit` object persistence to find the one contains the `commitID` substring. The collision of substrings is ignored, and it will only checkout the first commit that being found. It refers the staging area and working directory manipulation logic to the `State.checkoutFile` method.


### State.java

1. `public State(String sha1)` The constructor for a `State` object which tracks the current repository state. It should only be called by the `Repository.init()` method.
2. `public void addFile(String filename)` The function that adds the file to the staging area. It checks the file's existence and compares the file's hash value with the file in the HEAD commit, if the same filename appeared in the HEAD commit.
3. `public void commitStage()` The function that turns staged files to tracked files, move head pointer forward, and clean the staging area.
4. `public void rmFile(String filename)` The function that removes the file from the staging area according to the file's status (a tracked file, a staged file, or just an untracked file) in the repository.
5. `public void checkoutFile(String filename, Commit commit)` The function that changes the file in the working directory with the version in the `commit`. It will change the staging area accordingly.

### add
The underlying logic is referred to the `State.addFile()` method as the `add` command requires changing the staging area.

Flow:
1. Check the file's existence, if exists then
2. Check the edge case.
3. Change the staging area according to the edge case check. This includes copy or delete the file to `.gitlet/stage/` folder and put the `file name` - `file hash` to the `addedFiles` map.

#### Edge case: the file is identical to the version in the current commit.
The identity check is done by comparing the hash of the two files and the edge case is handled in the if statement.

### status
The status of a repository is divided into five sections: "branches", "Staged File", "Removed File", "Modifications Not Staged For Commit", and "Untracked Files". These sections' names are also the parameters of the `typeOfStatus`, which will print the specific section status accordingly. The `typeOfStatus` method gets the entries (a.k.a. the filenames) to be printed in these sections by calling the `currentState.getStatusIter(String type)` method, which will return a string `Iterable` of all the entries. It also requires a HoF that determines how the entries should be printed.

#### Repository.status
This method will just call the `typeOfStatus` method with the section name and an optional `PrintFunction`.

#### Repository.typeOfStatus
It will get an entries (a.k.a. filenames) list from the `State.getStatusIter` method, sort the list in Java string-comparison order and print each entry using the received print method. The default print method is just `System.out.println()`. There is only one special print method for the branch, which will put an * to the HEAD branch string.

#### State.getStatusIter
This method returns a string `Iterable` for the required section's entries to be printed from the `transient Status status` object in `currentState` class. If the `status` object is not initialized, then the constructor of the `State.Status` class will be called.

#### State.status
This class will store the five string iterables corresponding to the five status sections. Once its constructor is called, all the five string iterables will be created. The instance variables are named to be "XXXStrings" instead of "XXXFilenames" because for the "Modifications Not Staged For Commit" section, there should be an extra string indicated the file's status (deleted or modified). And considering the entries in the iterables are exactly the string to be sorted and printed, the "XXXStrings" would make more literal sense.

When it is initializing, it will traverse all the files in the working directory and computing sha1 hash for those files that are staged or tracked. And the iterables for "Staged File", "Modifications Not Staged For Commit", and "Untracked Files" will be completed after the traversal. The iterables for "branches" and "removed files" will directly be got from the `currentState`'s instance variables.

Because the status command requires no change in the state of the repository, which means `save` method will never be called, this constructing call use a way that will change some instance objects of the `currentState` and it won't be saved (it will not actually change the `currentState` persistence, so that the command after `gitlet status` won't be affected).

## Persistence
The directory structure looks like this:
```
CWD                             <==== Whatever the current working directory is.
└── .gitlet                     <==== All persistant data is stored within here
    ├── state                   <==== Where the repository's state is stored (a file)
    └── commits                 <==== All commits are stored in this directory
        ├── commit1             <==== A single commit instance stored to a file
        ├── commit2
        ├── ...
        └── commitN
    └── blobs                    <==== All blob are stored in this directory
        ├── blob1                <==== A single blob instance stored to a file
        ├── blob2
        ├── ...
        └── blobN
    └── stage                    <==== All staged files are stored in this directory
        ├── blobToBeCommited1    <==== A single blobToBeCommited instance stored to a file
        ├── blobToBeCommited2
        ├── ...
        └── blobToBeCommitedN
```

The `Repository` will set up all the directories, except `stage`, when `init` command is executed.

The `State` class will handle the serialization of the only `State` object for this repository:
1. `public State(String sha1)` - Given the hash value of the *initial commit*, the `stage` folder will be created, and the initial commit's state will be serialized by calling the `save()` method.
2. `public void save()` - Serialize the `State` Object to the `state` file. The `state` already existed will be overwritten because there is only one state for each gitlet repository. All the changes to the State need to be saved use this method.
3. `public static State readState()` - Retrieve the only state object from file `state`. It must exist because every call to this method is after the initial commit was created.

All the blob files are from the files in the staging area (the `stage` folder) using `java.nio.Files.move`. All the files that are staged will be copied to the `stage` from the working directory using `java.nio.Files.copy` with its name changed to its sha1 hash value. `copyFile` and `moveFilesInFolder` are helper methods for this purpose.

The `Commit` class will handle the serialization of `Commit` objects. It has two methods that are useful for this:
1. `public String save()` - Serialize this `Commit` object to the `COMMIT_DIR` in a file whose name is the sha1 hash value of this object. Because the `Commit` object's fields, this sha1 hash is considered unique. The collision of hash value is ignored as it's a fundamental bug that in practice never occurs.
2. `public static Commit readCommit(String hash)` - Given the sha1 hash of a `Commit` object, which is also its file name, it retrieves the serialized data from the `COMMIT_DIR`.