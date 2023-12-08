package ch.unibas.dmi.dbis.cs108.tests;

import static org.junit.Assert.assertNotEquals;

import ch.unibas.dmi.dbis.cs108.project.Data;
import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Assert;

public class DataTest extends TestCase {

  private static Data data;

  @Override
  protected void setUp() throws IOException {
    data = new Data("name", null);
  }

  /**
   * ch.unibas.dmi.dbis.cs108.tests bPawn move from 0 1 to 0 2
   *
   * @throws IOException pls
   */
  public void testMovePawn() throws IOException {
    assertEquals("bPawn", data.figures[0][1]); // checks if figure is on starting field
    data.move(0, 1, 0, 2, 'b', "name"); // moves the pawn
    assertNotEquals("bPawn", data.figures[0][1]); // checks if figure is still on starting field
    assertEquals("bPawn", data.figures[0][2]); // checks if the move worked correctly
  }

  /**
   * ch.unibas.dmi.dbis.cs108.tests bRock move from 0 1 to 0 2
   *
   * @throws IOException pls
   */
  public void testMoveRock() throws IOException {
    assertEquals("bRock", data.figures[0][0]); // checks if figure is on starting field
    data.moveTest(0, 0, 0, 5, 'b', "name"); // makes invalid move
    assertEquals("bRock", data.figures[0][0]); // checks if figure is on starting field
    assertNotEquals("bRock", data.figures[0][5]); // checks if figure is not on moved field
    data.moveTest(0, 1, 0, 2, 'b', "name"); // 3 moves to open rock
    data.moveTest(0, 2, 0, 3, 'b', "name");
    data.moveTest(0, 3, 0, 4, 'b', "name");
    data.moveTest(0, 0, 0, 3, 'b', "name"); // moves rock valid
    assertEquals("bRock", data.figures[0][3]); // checks if the move worked correctly
    assertNotEquals("bRock", data.figures[0][0]); // checks if figure is not on starting field
  }

  /**
   * ch.unibas.dmi.dbis.cs108.tests wQueen move from 0 1 to 0 2
   *
   * @throws IOException pls
   */
  public void testMoveQueen() throws IOException {
    assertEquals("wQueen", data.figures[16][3]); // checks if figure is on starting field
    data.moveTest(16, 3, 14, 4, 'w', "name"); // makes invalid move
    assertNotEquals("wQueen", data.figures[14][4]); // checks if still on starting field
    data.moveTest(14, 4, 13, 4, 'w', "name"); // free the queen
    data.moveTest(16, 3, 4, 5, 'w', "name"); // moves queen
    assertEquals("wQueen", data.figures[4][5]); // checks if the move worked correctly
    assertNotEquals("wQueen", data.figures[16][3]); // checks if figure is not on starting field
  }

  /**
   * ch.unibas.dmi.dbis.cs108.tests wKnight move from 0 1 to 0 2
   *
   * @throws IOException pls
   */
  public void testMoveKnight() throws IOException {
    assertEquals("wKnight", data.figures[16][1]); // checks if figure is on starting field
    data.moveTest(16, 1, 13, 3, 'w', "name");
    assertNotEquals("wKnight", data.figures[16][1]); // checks if figure is not on starting field
    assertEquals("wKnight", data.figures[13][3]); // checks if the move worked correctly
  }

  /**
   * ch.unibas.dmi.dbis.cs108.tests bPawn move from 0 1 to 0 2
   *
   * @throws IOException pls
   */
  public void testMoveBishop() throws IOException {
    assertEquals("wBishop", data.figures[16][2]); // checks if figure is on starting field
    data.moveTest(14, 3, 13, 3, 'w', "name");
    data.moveTest(16, 2, 8, 6, 'w', "name");
    assertEquals("wBishop", data.figures[8][6]); // checks if the move worked correctly
    assertNotEquals("wBishop", data.figures[16][2]); // checks if figure is not on starting field
  }

  /**
   * ch.unibas.dmi.dbis.cs108.tests bPawn move from 0 1 to 0 2
   *
   * @throws IOException pls
   */
  public void testCheckMate() throws IOException {
    assertEquals("bKing", data.figures[5][0]); // checks if figure is on starting field
    assertEquals("wKing", data.figures[16][5]); // checks if figure is on starting field
    assertEquals("rKing", data.figures[3][11]); // checks if figure is on starting field
    data.moveTest(6, 2, 6, 4, 'b', "name");
    data.moveTest(14, 6, 12, 8, 'w', "name");
    data.moveTest(4, 10, 4, 8, 'r', "name");
    data.moveTest(3, 2, 3, 4, 'b', "name");
    data.moveTest(14, 3, 12, 3, 'w', "name");
    data.moveTest(3, 9, 3, 7, 'r', "name");
    data.moveTest(2, 0, 6, 8, 'b', "name");
    data.moveTest(6, 8, 3, 11, 'b', "name");
    data.moveTest(16, 2, 8, 6, 'w', "name");

    // NullPointerException gets thrown because no Lobby exists and it tries to tell the lobby that
    // the game is over.
    Assert.assertThrows(NullPointerException.class, () -> data.moveTest(8, 6, 5, 0, 'w', "name"));

    // Kings should be defeated
    assertNotEquals("bKing", data.figures[5][0]); // checks if figure is not on starting field
    assertNotEquals("rKing", data.figures[3][11]); // checks if figure is not on starting field
  }
}
