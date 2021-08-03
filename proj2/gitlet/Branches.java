package gitlet;

import static gitlet.Utils.*;
import java.io.File;
import java.util.HashMap;

public class Branches {
    /** The name-SHA1 HashMap. */
    private HashMap<String, String> branches;
    /** The file stores branches. */
    File BRANCHES = join(Repository.GITLET_DIR, "branches");

    /** Create a master branch. It should only be called by init(). */
    public Branches(String sha1) {
        branches = new HashMap<>();
        addBranch("master", sha1);
        addBranch("HEAD", sha1);
        save();
    }

    /** Add a branch called name and serialize the class. */
    public void addBranch(String name, String sha1) {
        branches.put(name, sha1);
    }

    /** Return the SHA1 of branch `name`. */
    public String getBranch(String name) {
        return branches.get(name);
    }

    /** Save the changes to the file. */
    public void save(){
        writeObject(BRANCHES, branches);
    }
}

