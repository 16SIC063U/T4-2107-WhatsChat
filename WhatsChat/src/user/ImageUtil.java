package user;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtil {

	public static String IMAGE_PATH = "users/username_";

	// User folder that store other user images
	public static String getUserFolderPath(String username) {
		return IMAGE_PATH + username + "/";
	}

	// Delete user folder
	public static void deleteUserFolder(String username) {
		File userFolder = new File(getUserFolderPath(username));
		String[] entries = userFolder.list();
		if (entries != null) {
			for (String s : entries) {
				File currentFile = new File(userFolder.getPath(), s);
				currentFile.delete();
			}
		}
		userFolder.delete();
	}

	public static File createFile(String fileName, byte[] byteArray) {
		File file = new File(fileName);
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			fos.write(byteArray);
			fos.close();
			return file;
		} catch (IOException e) {
			System.out.println("Unable to create file");
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] fileToByte(File imgFile) {
		BufferedImage originalImage;
		try {
			originalImage = ImageIO.read(imgFile);
			int IMG_WIDTH = 512;
			int IMG_CLAHEIGHT = 512;
			BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_CLAHEIGHT, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_CLAHEIGHT, null);
			g.dispose();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(resizedImage, "jpg", baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();

			return imageInByte;
		} catch (IOException e) {
			System.out.println("Error reducing image file size");
			return null;
		}
	}

	public static File byteToFile(File fileToWrite, byte[] fileByte) {
		// Write reduced image to file
		try (FileOutputStream fos = new FileOutputStream(fileToWrite)) {
			fos.write(fileByte);
			fos.close();
			return fileToWrite;
		} catch (IOException e) {
			System.out.println("error reducing file size");
			e.printStackTrace();
			return null;

		}
	}

	public static File reduceImageSize(File imgFile) {
		byte[] fileByte = fileToByte(imgFile);
		if (fileByte == null) {
			return null;
		}
		return byteToFile(imgFile, fileByte);

	}
}
