***MAKE SURE TO IMPORT THE WHOLE PROJECT, DIRECTORIES INCLUDED***

**Index.java: Magic Card Seach**
- Run the program in a Java IDE (must be able to take input). This is a Maven project.
- If you are for some reason missing the mtgIndex, look for the lines of code that are as so:
  
        // Comment this if mtgIndex is for some reason missing
        Index index = new Index(true, "mtgIndex");

        // Uncomment the below line if mtgIndex is for some reason missing
        // Index index = new Index(false, "oracleCards.json");

- Running Index.java in this state will generate a new index. Be sure to delete the old index first.
- You will need to unzip oracleCards.zip to access oracleCards.json
- Re-comment those lines after creating a new index since creating a new index each time may lead to duplicate cards in the index.

- To run Index.java:
- Follow prompts. Note that MTG does use some unusual spellings on occasion (Faerie vs. Fairy) and thus won't give results for traditional spellings.
- Example:
  		
		Welcome to Magic card search!
		Would you like to search card names or card text? (type n/t)
		n
		Please enter a search query:
		dragon
		Your search results are:
		
		Dragon Fangs G
		Enchant creature
		Enchanted creature gets +1/+1 and has trample.
		When a creature with mana value 6 or greater enters the battlefield, you may return Dragon Fangs from your graveyard to the battlefield attached to that creature.
		
		Dragon Scales W
		Enchant creature
		Enchanted creature gets +1/+2 and has vigilance.
		When a creature with mana value 6 or greater enters the battlefield, you may return Dragon Scales from your graveyard to the battlefield attached to that creature.
		
		Eternal Dragon W
		Flying
		{3}{W}{W}: Return Eternal Dragon from your graveyard to your hand. Activate only during your upkeep.
		Plainscycling {2} ({2}, Discard this card: Search your library for a Plains card, reveal it, put it into your hand, then shuffle.)
		
		...

**Commander.java: Card Recommendation System**
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

- When prompted, type in the full file name of the decklist (for example, "Krenko, Mob Boss.txt"). DO NOT PUT THE ABSOLUTE PATH, ONLY THE FILE NAME.
- Follow prompts as necessary
- Example:
		
		Welcome to Magic Commander Deck Helper!
		---------------------------------------
		Before using this program, make sure that you check the README!
		
		What is the filename of your deck?
		The Ur Dragon.txt
		---------------------------------------
		Showing top 10 results:
		
		Broodmate Dragon BGR
		Flying
		When Broodmate Dragon enters the battlefield, create a 4/4 red Dragon creature token with flying.
		
		Young Red Dragon // Bathe in Gold R
		Flying
		Young Red Dragon can't block.
		Create a Treasure token. (Then exile this card. You may cast the creature later from exile.)
		
		Hoarding Dragon R
		Flying
		When Hoarding Dragon enters the battlefield, you may search your library for an artifact card, exile it, then shuffle.
		When Hoarding Dragon dies, you may put the exiled card into its owner's hand.
		
		...


**Evaluation.java: Testing for Card Recommendation System**
- Requires Commander.java and Index.java
- Place complete commander decks as a .txt file in the Decklists directory using the same format as in Commander.java.
- Sample sizes and number of tests ran per k can be changed with the following variables:

        int[] subsetSizes = {25, 50, 75};
        int iteration = 100;

- Example:
		
		Current Subset Size: 25
			k = 1:
		    	hitRate: 0.2355
		    	meanPrecision: 0.2355
			k = 2:
		    	hitRate: 0.3515
		    	meanPrecision: 0.20825
			k = 3:
		    	hitRate: 0.4395
		    	meanPrecision: 0.20733333333333298
			k = 4:
		    	hitRate: 0.4755
		    	meanPrecision: 0.189625
			k = 5:
		    	hitRate: 0.5125
		    	meanPrecision: 0.1760999999999976
		  ...
