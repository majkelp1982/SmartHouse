package pl.pomazanka.SmartHouse.backend.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
  public static final int ERROR = 0;
  public static final int WARN = 1;
  public static final int INFO = 2;
  public static final int DEBUG = 3;

  public static int level;

  private static String dt() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"));
  }

  public static void debug(String message) {
    if (level >= DEBUG) {
      System.out.println("[DEBUG]" + dt() + " " + message);
    }
  }

  public static void info(String message) {
    if (level >= INFO) {
      System.out.println("[INFO]" + dt() + " " + message);
    }
  }

  public static void warn(String message) {
    if (level >= INFO) {
      System.out.println("[WARN]" + dt() + " " + message);
    }
  }

  public static void error(String message) {
    if (level >= INFO) {
      System.out.println("[ERR]" + dt() + " " + message);
    }
  }
}
