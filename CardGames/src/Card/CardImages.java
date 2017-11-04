package Card;

import java.awt.*;
import java.io.*;

import javax.imageio.ImageIO;

/**
 *  Images des cartes
 *	  constitution du jeu de 54 cartes
 *	  de 0 à 51 classé TKCP du 2 à l'As
 *	  52 = dos, 53 et 54 les jokers
 *
 */
public class CardImages {

	/**
	 *  représentation de chaque carte du jeu
	 */
	public static Image[] cims = new Image[55];

	/**
	 * chargement des images des cartes
	 */
	public static void makeCardsGif()  {
		String nameImage = "cardImage/cover.gif";
		try {
			nameImage = "cardImage/cover.gif";
			cims[52] = ImageIO.read(ClassLoader.getSystemResourceAsStream(nameImage));
			nameImage = "cardImage/joker1.gif";			
			cims[53] = ImageIO.read(ClassLoader.getSystemResourceAsStream(nameImage));
			nameImage = "cardImage/joker2.gif";			
			cims[54] = ImageIO.read(ClassLoader.getSystemResourceAsStream(nameImage));
			int n = 0;
			for ( int c = 0; c < 4; c++ )  {
				for ( int i = 0; i < 13; i++ )  {
					nameImage = String.format("cardImage/c%01d%02d.gif", c, i);
					cims[n++] = ImageIO.read(ClassLoader.getSystemResourceAsStream(nameImage));
				}
			}
		} catch (IOException e) {
			System.out.println("Image introuvable : " + nameImage);
			e.printStackTrace();
		}		
	}

}
