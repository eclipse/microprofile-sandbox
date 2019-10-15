package org.eclipse.microprofile.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The different Levels at which a Log statement can be logged.
 */
public class Level {
  
  /** This MUST be first so it's available in the constructor */
  private static final List<Level> LEVELS = new ArrayList<>();
  
  /**
   * Special Level used to turn off logging.
   */
  public static Level OFF = new Level("OFF", Integer.MAX_VALUE);
  
  /**
   * Message indicating a serious failure has occurred and may be of 
   * particular interest to Administrators.
   * 
   * <p>The level is initialized at 1000</p>
   */
  public static Level ERROR = new Level("ERROR", 1000);
  
  /**
   * Message indicating a potential problem that may be of particular 
   * interest to Administrators.
   * 
   * <p>The level is initialized at 900</p>
   */
  public static Level WARN = new Level("WARN", 900);
  
  /**
   * Message providing informational content typically useful to most users 
   * including End Users, Administrators, Support, Developers, etc.
   * 
   * <p>The level is initialized at 800</p>
   */
  public static Level INFO = new Level("INFO", 800);
  
  /**
   * Message for tracing information, typically more useful to Developers 
   * and Support.
   * 
   * <p>The level is initialized at 500</p>
   */
  public static Level DEBUG = new Level("DEBUG", 500);
  
  /**
   * Message for more detailed tracing information, typically more useful 
   * to Developers.
   * 
   * <p>The level is initialized at 400</p>
   */
  public static Level TRACE = new Level("TRACE", 400);
  
  /**
   * Level to indicate ALL logging
   */
  public static Level ALL = new Level("ALL", Integer.MIN_VALUE);
  
  public static Level parse(String name) {
    int intValue = -1;
    boolean isInt = false;
    try {
      intValue = Integer.parseInt(name);
      isInt = true;
    } catch (NumberFormatException nfe) {
      
    }
    
    Iterator<Level> levelIt = LEVELS.iterator();
    while (levelIt.hasNext()) {
      Level level = levelIt.next();
      if (level.getName().equals(name)) {
        return level;
      } else if (isInt && level.intValue() == intValue) {
        return level;
      }
    }
    
    if (isInt) {
      return new Level(name, intValue);
    }
    
    throw new IllegalArgumentException("Not a valid Level \"" + name + "\"");
  }

  private final String name;
  private final int value;
  

  private Level(String name, int value) {
    this.name = name;
    this.value = value;
    LEVELS.add(this);
  }

  /**
   * Get the name of the Level
   * 
   * @return The name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the integer value of this level. The level is used to infer hierarchy between levels.
   * 
   * @return The integer value for this level.
   */
  public int intValue() {
    return value;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 47 * hash + this.value;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Level other = (Level) obj;
    return this.value == other.value;
  }

  @Override
  public String toString() {
    return getName();
  }
}
