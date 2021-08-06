# Gitlet Design Document

**Liaoliao Liu**:

## Classes and Data Structures

### Main

This is the entry point to our program. It takes in arguments from the command line and based on the command (the first element of the `args` array) calls the corresponding command in `Repository` which will actually execute the logic of the command. It also validates the arguments based on the command to ensure that enough arguments were passed in.

#### Fields

This class has no fields and hence no associated state: it simply validates arguments and defers the execution to the `Repository` class.

### Repository

This is where the main logic of our program will live. This file will handle all of the actual gitlet commands by reading/writing from/to the correct file, setting up persistence, and additional error checking.

This class defers all `Branch` specific logic to the `Branch` class and `Commit` specific logic to the `Commit` class.

#### Fields

1. `public static final File CWD = new File(System.getProperty("user.dir"))` The Current Working Directory.
2. `public static final File GITLET_DIR = join(CWD, ".gitlet")` The hidden `.gitlet` directory. This is where all of the state of the `Repository` will be stored, including `blob` and `commit`
3. `public static final File COMMITS_DIR = join(GITLET_DIR, "commits")` The commits directory. Each `Commit` object' persistence will be stored here with its sha1 value as its file name.
4. `public static final File BLOBS_DIR = join(GITLET_DIR, "blobs")` The blobs directory. This is where the blobs in commited commits will be stored with its sha1 value as its file name.

### Commit

This class represents a `Commit` that will be stored in a file. The file's name will be the corresponding sha1 string of the commit to make it unique.

All `Commit` objects are serialized within the `.commits` which is within the `.gitlet`. The save method will write the `Commit` to a file named its sha1 string and return the string.

#### Fields

1. `private String message` The message of this commit.
2. `private Date timestamp` The timestamp of this commit.

### State

This class represents the state of the current repository, including branches and the staging area. The HEAD pointer is also represented as a branch.

There is only one `State` object that are serialized within the `.gitlet`.

#### Fields


1. `private HashMap<String, String> branches` The map of `branch name` - `commit hash`.
2. `private HashSet<String> removedFiles` The set of files that are staged for removal.
3. `private HashMap<String, String> addedFiles` The map of `file name` - `file hash` that are staged for add.
4. `public static final File STATE = join(Repository.GITLET_DIR, "state")` The persistence file.

## Algorithms

## Persistence
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
```