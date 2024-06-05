package wowchat.game

import wowchat.common.{WowChatConfig, WowExpansion}

import scala.io.Source

object GameResources {

  // Lazily load area and achievement data based on the expansion
  lazy val AREA: Map[Int, String] = readIDNameFile(WowChatConfig.getExpansion match {
    case WowExpansion.Vanilla | WowExpansion.TBC | WowExpansion.WotLK => "pre_cata_areas.csv" // For Vanilla, TBC, and WotLK expansions, load pre-Cataclysm areas.
    case _ => "post_cata_areas.csv" // For other expansions, load post-Cataclysm areas.
  })

  lazy val ACHIEVEMENT: Map[Int, String] = readIDNameFile("achievements.csv") // Load achievements data.

  // Helper function to read ID-Name mappings from a CSV file
  private def readIDNameFile(file: String) = {
    Source
      .fromResource(file) // Read the CSV file from the resources directory.
      .getLines // Get lines from the file.
      .map(str => {
        val splt = str.split(",", 2) // Split each line by comma to get ID and Name.
        splt(0).toInt -> splt(1) // Return ID-Name pairs as a tuple.
      })
      .toMap // Convert the pairs into a Map.
  }
}
