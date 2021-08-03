package gitlet;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        terminateWithMsg(args.length == 0, "Please enter a command.");
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                terminateWithMsg(args.length != 1, "Incorrect operands.");
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
            // TODO: FILL THE REST IN
            default:
                terminateWithMsg(true, "No command with that name exists.");
        }
    }

    /**
     * Print `msg` if `cond` is false.
     *
     * @param cond Condition
     * @param msg Message
     */
    public static void terminateWithMsg(boolean cond, String msg) {
        if (cond) {
            System.out.println(msg);
            System.exit(0);
        }
    }
}
