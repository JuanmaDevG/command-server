import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {
  private static SocketAddress addr = new InetSocketAddress("localhost", 5000);
  private static final int TIMEOUT_MS = 2000;
  private static String help_msg = """
      Usage:
      <command> <arg1> [arg2] ...

      Options:
      h || help             -- prints this message
      sum <int1> <int2>     -- sum numbers""";

  public static void main(String[] args) throws IOException {
    Socket socket = new Socket();

    while (!socket.isConnected()) {
      try {
        socket.connect(addr, TIMEOUT_MS);
        break;
      } catch (UnknownHostException uhex) {
        System.err.println("-- Server unavailable, trying again --");
      }
    }
    String s = """
        hello
        nigger""";
    System.out.println("-- Connected, welcome to the server command line --");
    System.out.println(help_msg);

    try (
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        Scanner sc = new Scanner(System.in);) {

      while (true) {
        System.out.print("$ ");
        String input = sc.nextLine();
        if (input.equals("h") || input.equals("help")) {
          System.out.println(help_msg);
          continue;
        }

        out.writeUTF(input);
        input = in.readUTF();
        System.out.println(input);
      }

    } catch (IOException ioex) {
      System.err.println(ioex.getMessage());
    }
  }
}
