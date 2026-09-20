import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {
  private static SocketAddress addr = new InetSocketAddress("localhost", 5000);
  private static final int TIMEOUT_MS = 2000;
  private static String help_msg = """
      Usage:
      <command> <arg1> [arg2] ...

      Options:
      h || help                       -- prints this message
      q || quit                       -- Close connection and exit
      sum <int1> <int2> [int3] ...    -- sum numbers\n""";

  public static void main(String[] args) {
    Socket socket = new Socket();

    while (!socket.isConnected()) {
      try {
        socket.connect(addr, TIMEOUT_MS);
        break;
      } catch (UnknownHostException uhex) {
        System.err.println("-- DNS failed, the server could not be resolved --");
        return;

      } catch (SocketTimeoutException ex) {
        System.err.println("-- Server unavailable, trying again --");
        socket = new Socket();

      } catch (ConnectException coex) {
        System.err.println("-- Server unavailable, trying again --");
        try {
          Thread.sleep(TIMEOUT_MS);
        } catch (InterruptedException iex) {
          System.err.println("-- Wait timer to retry got interrupted, aborted --");
          return;
        }
        socket = new Socket();

      } catch (IOException ioex) {
        System.out.println("-- Unavailable to open socket --");
        return;
      }
    }
    System.out.println("-- Connected, welcome to the server command line --");
    System.out.println(help_msg);

    final Socket sk = socket; // captured pointer to be closeable
    try (sk;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        Scanner sc = new Scanner(System.in);) {

      while (true) {
        System.out.print("$ ");
        String input = sc.nextLine();
        switch (input) {
          case "h":
          case "help":
            System.out.println(help_msg);
            continue;
          case "":
            continue;
          default:
            out.writeUTF(input);
            input = in.readUTF();
            System.out.println(input);
        }
      }

    } catch (IOException ioex) {
      if (ioex.getMessage() != null)
        System.err.println(ioex.getMessage());
    }
  }
}
