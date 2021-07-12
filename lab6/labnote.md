# Lab 6: Getting Started on Project 2
## Intro
The goal of this lab is to prepare you for success on Project 2: Gitlet. In this lab, you will learn:

- How to run Java from the command line and run the tests for Capers (the tests for Gitlet are very very similar).
- How to work with files and directories in Java.
- How to serialize Java objects to a file and read them back later (also called Persistence).

Two methods to make the state of your program [persist](https://en.wikipedia.org/wiki/Persistence_(computer_science)) past the execution of your program: one through writing plain text to a file, and the other through serializing objects to a file.

## Persistence (Conceptually)

The key idea that makes this possible is to use your computer’s file system. By storing information to your hard drive, programs are able to leave information for later executions to utilize.

**Important note:** Static variables do NOT persist in Java between executions. When a program completes execution, all instance and static variables are completely lost. The only way we can maintain persistence between executions is to store data on the file system.

## Serializable
Serialization is the process of translating an object to a series of bytes that can then be stored in the file. We can then *deserialize* those bytes and get the original object back in a future invocation of the program.

To enable this feature for a given class in Java, this simply involves implementing the `java.io.Serializable` interface:
```java
import java.io.Serializable;

public class Model implements Serializable {
    ...
}
```
This interface has no methods; it simply marks its subtypes for the benefit of some special Java classes for performing I/O on objects. For example,
```java
Model m = ....;
File outFile = new File(saveFileName);
try {
    ObjectOutputStream out =
        new ObjectOutputStream(new FileOutputStream(outFile));
    out.writeObject(m);
    out.close();
} catch (IOException excp) {
    ...
}
```
will convert `m` to a stream of bytes and store it in the file whose name is stored in `saveFileName`. The object may then be reconstructed with a code sequence such as
```java
Model m;
File inFile = new File(saveFileName);
try {
    ObjectInputStream inp =
        new ObjectInputStream(new FileInputStream(inFile));
    m = (Model) inp.readObject();
    inp.close();
} catch (IOException | ClassNotFoundException excp) {
    ...
    m = null;
}
```

You’ll be serializing objects left and right, so to lower the amount of code you have to write we have provided helper function in `Utils.java` that handles reading and writing objects.

Note that the code above is pretty annoying, with lots of mystery classes and try/catch statements. If you use our helper functions from the `Utils` class, serializing is as easy as:
```java
Model m;
File outFile = new File(saveFileName);

// Serializing the Model object
writeObject(outFile, m);
```
And similarly, deserializing is simply:
```java
Model m;
File inFile = new File(saveFileName);

// Deserializing the Model object
m = readObject(inFile, Model.class);
```