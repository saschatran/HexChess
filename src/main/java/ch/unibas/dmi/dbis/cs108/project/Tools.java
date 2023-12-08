package ch.unibas.dmi.dbis.cs108.project;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * methods that are used several times in many classes
 */
public class Tools {

  /**
   * is used in method lcg
   *
   * @param rngVal initialized random number
   */
  static int rngVal = 28032021;

  /**
   * hexDec to dec
   *
   * @param hex get hex number
   * @return cx: the converted number of hex to dec in long
   */
  public static long hexDec(char[] hex) {
    if (hex.length > 15) {  //prevents overflow
      return 0;
    }
    long cx = 0;
    for (int i = 0; i < hex.length; i++) {
      cx *= 16;
      hex[i] = Character.toUpperCase(hex[i]);

      switch (hex[i]) {
        case '0':
          cx += 0;
          break;
        case '1':
          cx += 1;
          break;
        case '2':
          cx += 2;
          break;
        case '3':
          cx += 3;
          break;
        case '4':
          cx += 4;
          break;
        case '5':
          cx += 5;
          break;
        case '6':
          cx += 6;
          break;
        case '7':
          cx += 7;
          break;
        case '8':
          cx += 8;
          break;
        case '9':
          cx += 9;
          break;
        case 'A':
          cx += 10;
          break;
        case 'B':
          cx += 11;
          break;
        case 'C':
          cx += 12;
          break;
        case 'D':
          cx += 13;
          break;
        case 'E':
          cx += 14;
          break;
        default:
          cx += 15;
          break;
      }
    }
    return cx;
  }

  /**
   * add random number to not unique username as suggestion
   * (will be implemented)
   *
   * @return random number in int
   */
  public static int lcg() {
    //simple RNG for benchmarking our sorting algorithm
    /*parameters from en.wikipedia.org/wiki/Linear_congruential_generator*/
    long a = 1664525;
    long m = 1l << 32;
    byte c = 1;
    rngVal = (int) ((a * rngVal + c) % m);
    return rngVal;
  }

  /**
   * makes message network-protocol-readable
   *
   * @param msg the message in String to be sent
   * @param net kind of message, e.g. "Nam", "Txt", ...
   * @param out OutputStream of ClientThread
   */
  public static void netMsg(String msg, String net, OutputStream out) {
    String tmp = "00000000"; //length of line in hexDec
    tmp = tmp + Integer.toHexString(msg.length() + 15);
    //last eight digit visible
    tmp = tmp.substring(tmp.length() - 8);
    msg = net + " cx " + tmp + msg;
    try {
      if (out != null) {
        out.write(msg.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * String.strip() doesn't seem to work for all versions
   * strips initial and trailing spaces
   *
   * @param msg       is anything saved as String such as username, message and so on
   * @param recursive boolean to check possible initial spaces
   * @return empty String if msg only consists of spaces...
   * ...or the msg without initial and trailing spaces.
   */
  public static String strip(String msg, boolean recursive) {  //Strips string for use in chat

    //check if msg is empty
    Pattern pattern = Pattern.compile("\\A(\\s)*\\z");
    Matcher matcher = pattern.matcher(msg);
    if (matcher.find()) {   //msg only consists of spaces
      return "";
    }

    //check for initial spaces
    pattern = Pattern.compile("\\A\\s");
    matcher = pattern.matcher(msg);
    while (matcher.find()) {
      msg = msg.substring(1);
      matcher = pattern.matcher(msg);
    }

    //all initial and trailing spaces stripped
    if (!recursive) {
      return msg;
    }

    //check for trailing spaces
    pattern = Pattern.compile("\\s\\z");
    matcher = pattern.matcher(msg);
    while (matcher.find()) {
      msg = msg.substring(0, msg.length() - 1);
      matcher = pattern.matcher(msg);
    }
    if (msg.charAt(0) == '/') {
      msg = "/" + strip(msg, false);
    } else if (msg.charAt(0) == '\\') {
      msg = "\\" + strip(msg, false);
    }
    return msg;
  }
}
