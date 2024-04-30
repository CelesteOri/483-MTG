***MAKE SURE TO IMPORT THE WHOLE PROJECT, DIRECTORIES INCLUDED***

**Index.java:**
- Run the program in a Java IDE (must be able to take input). If you are for some reason missing the mtgIndex, look for the lines of code that are as so:
  
        // Comment this if mtgIndex is for some reason missing
        Index index = new Index(true, "mtgIndex");

        // Uncomment the below line if mtgIndex is for some reason missing
        // Index index = new Index(false, "oracleCards.json");

- Follow prompts. Note that MTG does use some unusual spellings on occasion (Faerie vs. Fairy) and thus won't give results for traditional spellings.

**Commander.java**
- Requires Index.java
- Place a (partially constructed) commander deck as a .txt in the Decklists directory. Be sure to follow this format, where the commander is the first card in the deck (if you do not know what the commander is, look at [this article](https://mtg.fandom.com/wiki/Commander_(designation))). It should look something like this:

        1 Omnath, Locus of All
        1 Adarkar Wastes
        1 Ainok Survivalist
        1 Akroma, Angel of Fury
        1 Aphetto Runecaster
        1 Arcane Signet
        1 Ashcloud Phoenix
        ...

- When prompted, type in the full file name of the decklist (for example, "Omnath.txt")
- Follow prompts as necessary
