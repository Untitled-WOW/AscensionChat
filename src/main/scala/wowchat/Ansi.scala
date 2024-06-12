/* This file holds the colours I used for the logo etc (and more)
I figured out later that Scala has 'AnsiColor' in their library, LOL.
I like this as a reference anyway. Plus, you can edit the values for them as you please. */

package wowchat

object Ansi {
  // ANSI escape codes for normal colours
  val GREY = "\u001b[30m"
  val RED = "\u001B[31m"
  val GREEN = "\u001B[32m"
  val YELLOW = "\u001B[33m"
  val BLUE = "\u001B[34m"
  val PURPLE = "\u001B[35m"
  val CYAN = "\u001B[36m"
  val WHITE = "\u001B[37m"

  // ANSI escape codes for bright colours
  val BGREY = "\u001B[90m"
  val BRED = "\u001B[91m"
  val BGREEN = "\u001B[92m"
  val BYELLOW = "\u001B[93m"
  val BBLUE = "\u001B[94m"
  val BPURPLE = "\u001B[95m"
  val BCYAN = "\u001B[96m"
  val BWHITE = "\u001B[97m"

  // ANSI escape code to reset all styles
  val CLR = "\u001B[0m"

  // ANSI escape codes for background colours
  val BLACK_BG = "\u001B[40m"
  val RED_BG = "\u001B[41m"
  val GREEN_BG = "\u001B[42m"
  val YELLOW_BG = "\u001B[43m"
  val BLUE_BG = "\u001B[44m"
  val PURPLE_BG = "\u001B[45m"
  val CYAN_BG = "\u001B[46m"
  val WHITE_BG = "\u001B[47m"

  // ANSI escape codes for bright background colours
  val BBLACK_BG = "\u001B[100m"
  val BRED_BG = "\u001B[101m"
  val BGREEN_BG = "\u001B[102m"
  val BYELLOW_BG = "\u001B[103m"
  val BBLUE_BG = "\u001B[104m"
  val BPURPLE_BG = "\u001B[105m"
  val BCYAN_BG = "\u001B[106m"
  val BWHITE_BG = "\u001B[107m"

  // ANSI escape codes for text styles
  val BOLD = "\u001B[1m"
  val FAINT = "\u001B[2m"
  val ITALIC = "\u001B[3m"
  val UNDERLINE = "\u001B[4m"
  val BLINK = "\u001B[5m"
  val RAPID_BLINK = "\u001B[6m"
  val REVERSE = "\u001B[7m"
  val CONCEAL = "\u001B[8m"
  val STRIKE_THROUGH = "\u001B[9m"

}
