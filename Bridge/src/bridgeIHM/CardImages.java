//		Images des cartes

package bridgeIHM;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

/*
 * 			image des cartes
 */

public class CardImages {
	static final TransparentFilter transparentFilter = new TransparentFilter(Color.white.getRGB() & 0xFFFFFF);
	static final int[] bidSuits = { 3, 2, 1, 0 };
	static final boolean[] redSuits = { false, true, true, false };

	public static Image[] fans, cims, suits, bigsuits;
	public static SizedImage[] bids;
	public static Dimension CardSize = new Dimension(71, 96);
	private static Container ctr;

	public static void Init(Container parent) {
		ctr = parent;
		makeCards("American");
		makeFans("Curio");
		makeSuits();
	}

	static void makeCards(String style) {
		cims = new Image[53];
		int width = CardSize.width;
		int height = CardSize.height;
		byte cbits[][] = new byte[52][width * height];
		TransparentFilter tf = new TransparentFilter(0xe100ff);
		try {
			String filename = "CardIHM/fronts/" + style;
			InputStream f = ClassLoader.getSystemResourceAsStream(filename);
			int size;
			byte r[] = new byte[256];
			byte g[] = new byte[256];
			byte b[] = new byte[256];
			IndexColorModel icm;
			for (int c = 51; c >= 0; c--) {
				size = f.read();
				f.read(r, 0, size);
				f.read(g, 0, size);
				f.read(b, 0, size);
				f.read(cbits[c]);
				icm = new IndexColorModel(8, size, r, g, b, 0);
				cims[c] = ctr.createImage(
						new FilteredImageSource(new MemoryImageSource(width, height, icm, cbits[c], 0, width), tf));
			}
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void makeFans(String style) {
		MediaTracker mt = new MediaTracker(ctr);
		Object obj = null;
		fans = new Image[6];
		for (int i = 0; i < 6; i++) {
			String filename = "CardIHM/backs/" + style + "/fan" + i + ".gif";
			try {
				obj = ClassLoader.getSystemResource(filename).getContent();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
			fans[i] = ctr.createImage(
					new FilteredImageSource(getImageProducer(obj, filename), new TransparentFilter(0xffc0ff)));
			mt.addImage(fans[i], 0);
		}
		try {
			mt.waitForAll();
		} catch (InterruptedException ex) {
		}
		cims[52] = fans[0];
	}

	// Get suit symbols from their files, and make backgrounds transparent.

	static void makeSuits() {
		suits = new Image[4];
		bigsuits = new Image[4];
		MediaTracker mt = new MediaTracker(ctr);
		for (int i = 0; i < 4; i++) {
			Object obj = null, obj2 = null;
			String filename = "CardIHM/suit" + i + ".gif";
			String bigfilename = "CardIHM/bigsuit" + i + ".gif";
			try {
				obj = ClassLoader.getSystemResource(filename).getContent();
				obj2 = ClassLoader.getSystemResource(bigfilename).getContent();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
			suits[i] = ctr.createImage(new FilteredImageSource(getImageProducer(obj, filename), transparentFilter));
			bigsuits[i] = ctr.createImage(new FilteredImageSource(getImageProducer(obj2, filename), transparentFilter));
			mt.addImage(suits[i], 0);
			mt.addImage(bigsuits[i], 0);
		}
		try {
			mt.waitForAll();
		} catch (InterruptedException ex) {
		}
	}

	/* Here we make the bids; P, X, XX and then 35 others. */

	static void makeBids(Image[] suits) {
		bids = new SizedImage[38];
		SizedImage.initFont(ctr, 16);
		bids[0] = new SizedImage(ctr, suits, "Passe", 4, "", Color.black);
		bids[1] = new SizedImage(ctr, suits, "Contre", 4, "", Color.red);
		bids[2] = new SizedImage(ctr, suits, "Surcontre", 4, "", Color.blue);
		for (int lev = 1; lev < 8; lev++) {
			for (int s = 0; s < 4; s++)
				bids[3 + 5 * (lev - 1) + s] = new SizedImage(ctr, suits, "" + lev, bidSuits[s], "",
						redSuits[s] ? Color.red : Color.black);
			bids[3 + 5 * (lev - 1) + 4] = new SizedImage(ctr, suits, "" + lev + "SA", 4, "", Color.black);
		}
	}

	static ImageProducer getImageProducer(Object obj, String filename) {
		if (obj instanceof Image) {
			return ((Image) obj).getSource();
		} else if (obj instanceof ImageProducer) {
			return (ImageProducer) obj;
		} else if (obj instanceof String) {
			throw new RuntimeException(filename + " is string, not image: " + obj);
		} else if (obj == null) {
			throw new RuntimeException(filename + " yielded null resource");
		} else {
			throw new RuntimeException("unknown content of " + filename + ": " + obj);
		}
	}

}

/* Filter to map a given background color to transparency. */
class TransparentFilter extends RGBImageFilter {
	int bg;

	public TransparentFilter(int bg) {
		canFilterIndexColorModel = true;
		this.bg = bg;
	}

	public int filterRGB(int x, int y, int rgb) {
		if ((rgb & 0XFFFFFF) == bg)
			return 0;
		return rgb;
	}
}

/*
 * Because the suit symbols are not part of a standard Java font, it is really a
 * pain to type something like "4S". So what we do is to define a SizedImage to
 * be such a string, displayed as an image. We also record the width of the
 * image.
 * 
 * To construct one, we first set up a place to work, a new panel in the given
 * container. Now we use 16 point font (which matches the pixel size of the suit
 * symbols we've got), and then print out the before string; the overall width
 * is the width of before, plus the symbol, plus the width of the after. The
 * height is from the font. Now we just remap it so that if the color is the
 * same as the background, we make it the given color using TransparentFilter.
 */

class SizedImage {
	static final int suit_widths[] = { 13, 13, 13, 13, 0 };

	static Font font;
	static FontMetrics fm;
	static ImageFilter transparentFilter;
	Image i;
	int width;

	static void initFont(Container ctr, int pointSize) {
		font = new Font("SansSerif", Font.BOLD, pointSize);
		fm = ctr.getFontMetrics(font);
		transparentFilter = new TransparentFilter(Color.white.getRGB() & 0xFFFFFF);
	}

	SizedImage(Container ctr, Image suits[], String before, int suit, String after, Color c) {
		JPanel p = new JPanel();
		ctr.add(p);
		p.setFont(font);
		p.setBackground(Color.white);
		p.setForeground(c);
		int bef = (before.length() != 0) ? 2 : 0;
		int aft = (after.length() != 0) ? 2 : 0;
		width = fm.stringWidth(before) + suit_widths[suit] + fm.stringWidth(after) + bef;
		int height = fm.getHeight();
		int base = fm.getLeading() + fm.getAscent();
		int leading = fm.getLeading();
		// int pix[] = new int[width * height];
		i = p.createImage(width, height); // retourne null ???
		Graphics og = i.getGraphics();
		og.drawString(before, 0, base);
		if (suit < 4) {
			og.drawImage(suits[suit], fm.stringWidth(before) + bef, leading + 2, null);
			og.drawString(after, fm.stringWidth(before) + bef + suits[suit].getWidth(null) + aft, base);
		} else {
			og.drawString(after, fm.stringWidth(before) + bef, base);
		}
		i = p.createImage(new FilteredImageSource(i.getSource(), transparentFilter));
		ctr.remove(p);
	}
}
