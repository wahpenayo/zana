package zana.test.java;

public enum Kolor { 
  RED, YELLOW, GREEN, CYAN, BLUE, MAGENTA; 

  @Override
  public final String toString () { 
    return "#zana.test.java.Kolor \"" + name() + "\""; } }
