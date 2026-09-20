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

  public CommandException(InetAddress addr, String msg) {
    super(msg);
    this.addr = addr;
  }

  @Override
  public String getMessage() {
    return ""; // TODO: no response to this.addr: super.getMessage()
  }
}

public class Server {
  public static void main(String[] args) throws IOException {
    try (ServerSocket sv = new ServerSocket(5000);) {
      Socket sk = sv.accept();
      Thread response = new Thread(() -> computeCommand(sk));
      response.start();
    } catch (IOException | SecurityException ex) {
      System.out.println(ex.getMessage());
    }
  }

  public static void computeCommand(Socket sk) {
    try (sk;
        DataInputStream in = new DataInputStream(sk.getInputStream());
        DataOutputStream out = new DataOutputStream(sk.getOutputStream());) {

      String[] command = in.readUTF().split(" +"); // Split, no spaces
      System.out.printf("%s requested %s: ", sk.getInetAddress().toString(), command[0]);
      switch (command[0]) {
        case "sum":
          if (command.length != 3) { // TODO: support for more than 2 operands
          }

          int x = Integer.parseInt(command[1]), y = Integer.parseInt(command[2]), r = x + y;
          System.out.printf("%i + %i = %i", x, y, r);
          out.writeUTF(Integer.toString(r));
          break;
        default:
      } // TODO: catch CommandException with custom message

    } catch (IOException ioex) {
      System.err.print("Server unavailable to give a response: ");
      System.err.println(ioex.getMessage());
    }
  }
}
