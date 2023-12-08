package ch.unibas.dmi.dbis.cs108.tests;

import static org.junit.Assert.assertTrue;

import ch.unibas.dmi.dbis.cs108.project.Client;
import ch.unibas.dmi.dbis.cs108.project.Server;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests some Net things
 */
public class NetTest {

  /*
   * Streams to store system.out and system.err content
   */
  private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();

  /*
   * Here we store the previous pointers to system.out / system.err
   */
  private PrintStream outBackup;
  private PrintStream errBackup;

  /**
   * starts server before every test.
   */
  @BeforeClass
  public static void startServerAndClient() {
    new Thread(() -> new Server(8090)).start();
  }

  /**
   * stolen from HelloWorldTest
   * does something useful I don't know lol
   *
   * @param str String to remove line
   * @return String with removed line
   */
  private static String removeNewline(String str) {
    return str.replace("\n", "").replace("\r", "");
  }

  /**
   * This method is executed before each test.
   * It redirects System.out and System.err to our variables {@link #outStream} and {@link #errStream}.
   * This allows us to test their content later.
   */
  @Before
  public void redirectStdOutStdErr() {
    outBackup = System.out;
    errBackup = System.err;
    System.setOut(new PrintStream(outStream));
    System.setErr(new PrintStream(errStream));
  }

  /**
   * This method is run after each test.
   * It redirects System.out / System.err back to the normal streams.
   */
  @After
  public void reestablishStdOutStdErr() {
    System.setOut(outBackup);
    System.setErr(errBackup);
  }

  /**
   * Tests if Lobby can be joined
   *
   * @throws InterruptedException Time sleep
   * @throws IOException          the meme
   */
  @Test
  public void testLobbyJoined() throws Exception {
    Client client = new Client("localhost", 8090, "unitTest0");
    client.sendServerTest("$ 1"); // joins lobby 1
    TimeUnit.MILLISECONDS.sleep(100);
    String toTest = outStream.toString();
    toTest = removeNewline(toTest);
    assertTrue(toTest.contains("You have joined lobby number: 1"));
  }

  /**
   * Tests if full lobby can be joined
   *
   * @throws Exception basically everything
   */
  @Test
  public void testJoinFullLobby() throws Exception {

    Client[] clients = new Client[4];
    for (int i = 0; i < 4; i++) {
      clients[i] = new Client("localhost", 8090, "unitTest" + i);
      clients[i].sendServerTest("$ 2");
      TimeUnit.MILLISECONDS.sleep(100);
    }
    String toTest = outStream.toString();
    toTest = removeNewline(toTest);
    assertTrue(toTest.contains("Lobby's full. Please choose another one!"));
  }

  /**
   * Test for rename
   *
   * @throws Exception Exception
   */
  @Test
  public void renameTest() throws Exception {
    Client client = new Client("localhost", 8090, "unitTest0");
    client.sendServerNetTest("unitTest0", "Nam");
    client.sendServerTest("/ Hassan");
    TimeUnit.MILLISECONDS.sleep(100);
    String toTest = outStream.toString();
    toTest = removeNewline(toTest);
    assertTrue(toTest.contains("Your username is : Hassan"));
  }

  /**
   * Test if lobby can be left.
   *
   * @throws Exception Exception
   */
  @Test
  public void leaveLobbyTest() throws Exception {
    Client client = new Client("localhost", 8090, "unitTest0");
    client.sendServerTest("$ 1");
    TimeUnit.MILLISECONDS.sleep(100);
    client.sendServerNetTest("L1", "Lob");
    TimeUnit.MILLISECONDS.sleep(100);
    String toTest = outStream.toString();
    toTest = removeNewline(toTest);
    assertTrue(toTest.contains("You left the lobby!"));
  }
}
