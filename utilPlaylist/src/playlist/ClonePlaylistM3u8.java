package playlist;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *  Recopie d'une playlist
 *   avec ajout "byte order mark" UTF-8 au début
 * 	 et en changeant l'extension m3u => m3u8
 *
 */
public class ClonePlaylistM3u8 {

	static File dirIn;
	static String dirOut;
	
		//	extension playlist
	static String ext = ".m3u";
	
		//	extension playlist UTF-8 avec BOM
	static String ext8 = ".m3u8";
	
		// bytes for BOM
	static byte[] bom = {(byte)239, (byte)187, (byte)191};
	
		//	code retour
	static int codeRetour = 0;
	
	static String newline = System.getProperty("line.separator");

	public static void main(String[] args) {

		if (args.length == 2)  {
			dirIn = new File(args[0]);
			dirOut = args[1];
		} else {
			usage();
		}
		
			//	traitement des playlists
		
		File[] listFile = dirIn.listFiles(new FileFilter() {
			public boolean accept(File dir) {
				return dir.isFile() && dir.getName().endsWith(ext);
			}
		});
		for (File f : listFile) {
			File fileOut = new File(dirOut + "/" + f.getName().replace(ext, ext8));
			convertPlaylist(f, fileOut);
		}

			// 	fin
		
		if (codeRetour > 0)	{
			System.out.println("*** Erreurs détectées ***");
		} else {
			System.out.println("*** Opération effectuée avec succès ***");
		}
		
		System.exit(codeRetour);

		
	}

	private static void convertPlaylist(File fileIn, File fileOut) {

		FileInputStream in;		
		FileOutputStream out;
		
		System.out.println("Traitement playlist : " + fileIn.getName());

		try
		{
			//  lecture
			in = new FileInputStream(fileIn);
			int lgFile = in.available();
			byte[] document = new byte[lgFile];
			in.read(document);
			//   ajout BOM
			byte[] withBom = new byte[bom.length + document.length];
			System.arraycopy(bom,0,withBom,0,bom.length);
			System.arraycopy(document,0,withBom,bom.length,document.length);
			//   écriture
			out = new FileOutputStream(fileOut);
			out.write(withBom);
			in.close();
			out.close();
			System.out.println("Playlist recopiée : " + fileOut.getName());
			
		} catch (Exception ex) {
			System.out.println("Erreur I/O : " + ex);
			ex.printStackTrace();
			codeRetour = 20;
		}
	}
		
		// Erreur de paramètres
	
	private static void usage() {
		System.out.println("Usage: java ClonePlaylistM3u8 <input dir> <output dir>");
		System.exit(10);
	}

}
