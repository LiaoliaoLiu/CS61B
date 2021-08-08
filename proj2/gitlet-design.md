# Gitlet Design Document

**Liaoliao Liu**:

## Classes and Data Structures

### Main

This is the entry point to our program. It takes in arguments from the command line and based on the command (the first element of the `args` array) calls the corresponding command in `Repository` which will actually execute the logic of the command. It also validates the arguments based on the command to ensure that enough arguments were passed in.

#### Fields

This class has no fields and hence no associated state: it simply validates arguments and defers the execution to the `Repository` class.

### Repository

This is where the main logic of our program will live. This file will handle all of the actual gitlet commands by manipulating the `State` and `Commit` object as it's a static class.

This class defers specific object manipulation logics to the `State` and `Commit` class.

#### Fields

1. `public static final File CWD = new File(System.getProperty("user.dir"))` The Current Working Directory.
2. `public static final File GITLET_DIR = join(CWD, ".gitlet")` The hidden `.gitlet` directory. This is where all of the state of the `Repository` will be stored, including `blob` and `commit`
3. `public static final File COMMITS_DIR = join(GITLET_DIR, "commits")` The commits directory. Each `Commit` object' persistence will be stored here with its sha1 value as its file name.
4. `public static final File BLOBS_DIR = join(GITLET_DIR, "blobs")` The blobs directory. This is where the blobs in commited commits will be stored with its sha1 value as its file name.

### Commit

This class represents a `Commit` that will be stored in a file. The file's name will be the corresponding sha1 string of the commit to make it unique.

All `Commit` objects are serialized within the `commits` which is within the `.gitlet`. The save method will serialize the `Commit` to a file named its sha1 string and return the string.

#### Fields

1. `private String message` The message of this commit.
2. `private Date timestamp` The timestamp of this commit.
3. `private HashMap<String, String> blobs` The name-sha1 map of blobs that this commit tracks.
4. `private String[] parents` The parent commit(s) hash.

### State

This class represents the state of the current repository, including branches, HEAD pointer and the staging area. There is only one `State` object that are serialized within the `.gitlet`. It will provide the API for the state  of current repository.

#### Fields

1. `private HashMap<String, String> branches` The map of `branch name` - `commit hash`.
2. `public String HEAD` The branch name that HEAD points at. Because Gitlet doesn't support detached HEAD state, it's just a String varible that stores a existing branch name.
3. `private HashSet<String> removedFiles` The set of files that are staged for removal.
4. `private HashMap<String, String> addedFiles` The map of `file name` - `file hash` that are staged for add.
5. `public static final File STATE = join(Repository.GITLET_DIR, "state")` The persistence file for the current repository state.
6. `public static final File STAGE_DIR = join(Repository.GITLET_DIR, "stage")` The directory that stores the staged files.

## Algorithms

### TODO
1. For each class, include a high-level description of the methods in that class, including any edge cases you are accounting for.
2. Simple explanations are preferred. Here are some formatting tips:
   1. For complex tasks, we recommend that you split the task into parts.

### Main.java

1. `public static void main(String[] args)` The main method that deals with user input.

### Repository.java

1. `public static void init()` The `init` command implementation. The runtime objects of gitlet will be first set and serialized here.
2. `public static void add(String filename)` The `add [filename]` command implememntation. It changes the staging area by manipulating the `currentState` which points to the repository's `State` object. See [the add section](###add).
3. `public static void commit(String msg, State stateToCommit)` The `commit [message]` command implementation. Files in staging area will become blobs by moving them to the `blobs` folder. A new commit is made acorrding to the parent commit and the current repository state.

### State.java

1. `public State(String sha1)` The constructor for a `State` object which tracks the current repository state. It should only be called by the `Repository.init()` method.
2. `public void addFile(String filename)` The function that changes the file's status in staging area. It checks the file's existence and compares the file's hash value with the file in the HEAD commit, if the same filename appeared in the HEAD commit.
3. `public void commitStage()` The `commit` command's utility function. It will turn all the staged files to blobs and clean the staging area.

### add
The majority of implementation is done in the `State.addFile()` method.

Flow:
1. Check the file's existence.
2. Check the edge case.
3. Change the staging area.

#### Edge case: the file is identical to the version in the current commit.
The identity check is done by comparing the hash of the two files and the edge case is handled in the if statement.

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
2. `public void save()` - Serialize the `State` Object to the `state` file. The `state` already existed will be overwrote because there is only one state for each gitlet repository.
3. `public static State readState()` - Retrieve the only state object from file `state`. It must exist because every call to this method is after the initial commit was created.

All the blob files are directly moved from the files in the staging  (the `stage` folder) using `java.nio.Files.move`. All the files that are staged will be copy to the `stage` from the working directory using `java.nio.Files.copy` with its name changed to its sha1 hash value. `copyFile` and `moveFilesInFolder` are helper methods for this purpose.

The `Commit` class will handle the serialization of `Commit` objects. It has two methods that are useful for this:
1. `public String save()` - Serialize this `Commit` object to the `COMMIT_DIR` in a file whose name is the sha1 hash value of this object. Because the `Commit` object's fields, this sha1 hash is considered unique. The collision of hash value is ignored as it's a fundamental bug that in practice never occurs.
2. `public static Commit readCommit(String hash)` - Given the sha1 hash of a `Commit` object, which is also its file name, it retrieves the serialized data from the `COMMIT_DIR`.