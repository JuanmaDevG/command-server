import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.lang.Thread;

import java.lang.NumberFormatException;
import java.lang.SecurityException;

class CommandException extends Exception {
  private InetAddress addr;
  private String command;

  public CommandException(InetAddress addr, String msg, String command) {
    super(msg);
    this.addr = addr;
    this.command = command;
  }

  @Override
  public String getMessage() {
    return "%s: %s (user sent %s)".formatted(addr, super.getMessage(), command);
  }

  public String getClientMessage() {
    return "-- Request failed -- %s".formatted(super.getMessage());
  }
}

public class Server {
  public static void main(String[] args) throws IOException {
    try (ServerSocket sv = new ServerSocket(5000);) {
      while (true) {
        Socket sk = sv.accept();
        System.out.printf("Accepted %s session\n", sk.getInetAddress());
        Thread response = new Thread(() -> computeCommand(sk));
        response.start();
      }
    } catch (IOException | SecurityException ex) {
      System.out.println(ex.getMessage());
    }
  }

  public static void computeCommand(Socket sk) {
    try (sk;
        DataInputStream in = new DataInputStream(sk.getInputStream());
        DataOutputStream out = new DataOutputStream(sk.getOutputStream());) {

      while (!sk.isClosed()) {
        try {
          StringBuilder sb = new StringBuilder();
          String[] command = in.readUTF().split(" +"); // Split, no spaces
          System.out.printf("%s requested command %s\n", sk.getInetAddress().toString(), command[0]);
          switch (command[0]) {
            case "sum":
              if (command.length < 3) {
                throw new CommandException(
                    sk.getInetAddress(),
                    "not enough operands",
                    command[0]);
              }
              try {
                int result = Integer.parseInt(command[1]);
                sb.append(command[1]);
                for (int i = 2; i < command.length; i++) {
                  sb.append(" + ");
                  sb.append(command[i]);
                  result += Integer.parseInt(command[i]);
                }
                sb.append(" = ");
                sb.append(result);

                System.out.println(sb.toString());
                out.writeUTF(Integer.toString(result));
              } catch (NumberFormatException fmtex) {
                throw new CommandException(sk.getInetAddress(), "required only numbers", command[0]);
              }
              break;
            case "q":
            case "quit":
              sk.close();
              return;
            default:
              throw new CommandException(
                  sk.getInetAddress(), "this does not exist", command[0]);
          }
        } catch (CommandException coex) {
          out.writeUTF(coex.getClientMessage());
          System.err.println(coex.getMessage());
        }
      }

    } catch (IOException ioex) {
      System.err.print("Server unavailable to give a response: ");
      System.err.println(ioex.getMessage());
    }
  }
}
