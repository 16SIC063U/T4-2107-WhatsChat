package user;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.JList;

import whatschat.WhatsChat;

public class UserImageMap implements Serializable {
	String username;
	File userImage;
	byte[] fileByte;

	public UserImageMap(String username, File userImage) {
		assert username != null;
		assert userImage != null;
		this.username = username;
		this.userImage = userImage;
	}

	public String getUsername() {
		return username;
	}

	public byte[] getFileByte() {
		return fileByte;
	}

	public UserImageMap(byte[] bytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			UserImageMap userImageMap = (UserImageMap) in.readObject();
			this.username = userImageMap.getUsername();

			this.fileByte = userImageMap.getFileByte();

			System.out.println("received file byte:" + fileByte.length);

			File outputfile = new File(ImageUtil.getUserFolderPath(WhatsChat.username) + "he.jpg");
			ImageIO.write(ImageIO.read(new ByteArrayInputStream(userImageMap.getFileByte())), "jpg", outputfile);

			BufferedImage originalImage = ImageIO.read(outputfile);
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

			// Write reduced image to file
			try (FileOutputStream fos = new FileOutputStream(outputfile)) {
				fos.write(imageInByte);
				fos.close();
			} catch (IOException e) {
				System.out.println("error reducing file size");
				e.printStackTrace();
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("error reading user image: " + e);
		} finally {

			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				System.out.println("error reading user image");
				// ignore close exception
			}
		}
	}

	public String getByteMessage() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			try {
				fileByte = Files.readAllBytes(userImage.toPath());
				System.out.println("sent file byte:" + fileByte.length);
				out = new ObjectOutputStream(bos);
				out.writeObject(this);
				out.flush();
				String s = new String(bos.toByteArray());
				return s;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("error sending");
				e.printStackTrace();
			}
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				System.out.println("error parsing");
				// ignore close exception
			}
		}
		return null;
	}
}
