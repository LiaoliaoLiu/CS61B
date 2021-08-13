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
                terminateWithMsg(args.length != 2, "Incorrect operands.");
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                terminateWithMsg(args.length > 2, "Incorrect operands.");
                terminateWithMsg(args.length == 1, "Please enter a commit message.");
                Repository.commit(args[1], State.readState());
                break;
            case "rm":
                terminateWithMsg(args.length != 2, "Incorrect operands.");
                Repository.rm(args[1], State.readState());
                break;
            case "log":
                terminateWithMsg(args.length != 1, "Incorrect operands.");
                Repository.log(State.readState());
                break;
            case "global-log":
                terminateWithMsg(args.length != 1, "Incorrect operands.");
                Repository.globallog();
                break;
            case "find":
                terminateWithMsg(args.length != 2, "Incorrect operands.");
                Repository.find(args[1]);
                break;
            case "status":
                terminateWithMsg(args.length != 1, "Incorrect operands.");
                Repository.status(State.readState());
                break;
            case "checkout":
                if (args.length == 3)
                    Repository.checkoutFile(args[2], State.readState());
                else if (args.length == 4)
                    Repository.checkoutFile(args[3], args[1], State.readState());

                break;
            /*
            case "test":
                State state = State.readState();
                Commit second = state.getHeadCommit();
                System.out.println("Hash: " + second.getHash());
                Commit init = second.getFirstParent();
                System.out.println("Hash: " + init.getHash());
                break;
             */
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
