package user;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class ProfilePicture {

	private static final long serialVersionUID = 11L;
	private String username;
	private BufferedImage image;

	public ProfilePicture(String username) {
		this.username = username;
		// Try and get image from path
		try {
			image = ImageIO.read(new File(ImageUtil.getUserFolderPath(username) + username + ".jpg"));
		} catch (IOException e) {
			image = null;
		}
	}

	/**
	 * Add profile picture to path
	 * 
	 * @return
	 */
	public boolean selectProfilePic() {

		// Choose file
		String filePath = chooseFile();
		if (filePath == null || filePath.isEmpty()) {
			return false;
		}

		// Create file
		File createdFile = copyFile(new File(filePath),
				new File(ImageUtil.getUserFolderPath(username) + username + ".jpg"));

		// Resize image
		createdFile = ImageUtil.reduceImageSize(createdFile);

		return createdFile != null;
	}

	public String getImagePath() {
		return ImageUtil.getUserFolderPath(username) + username + ".jpg";
	}

	/**
	 * Create image directory if directory does not exist
	 */
	private void createDir() {
		// if the directory does not exist, create it
		File theDir = new File(ImageUtil.getUserFolderPath(username));
		if (!theDir.exists()) {
			try {
				theDir.mkdirs();
			} catch (SecurityException se) {
				// handle it
			}
		}
	}

	private File copyFile(File source, File dest) {
		createDir();
		try {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new FileInputStream(source);
				os = new FileOutputStream(dest);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} finally {
				is.close();
				os.close();
			}
		} catch (IOException e) {
			System.out.println("Unable to add image: " + e);
			return null;
		}
		try {
			image = ImageIO.read(dest);
		} catch (IOException e) {
			System.out.println("error adding pic to frame");
		}
		return dest;
	}

	private String chooseFile() {
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "png", "jpg", "jpeg");
		jfc.addChoosableFileFilter(filter);
		int returnValue = jfc.showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();
			return selectedFile.getAbsolutePath();
		}
		return null;

	}

	public File getProfilePic() {
		return new File(getImagePath());
	}

	public ImageIcon getImageIconProfilePic(JComponent lblProfilePic) {
		Image dimg = null;
		if (image == null) {
			try {
				image = ImageIO.read(new File("no_img.png"));
				dimg = image.getScaledInstance(lblProfilePic.getWidth(), lblProfilePic.getHeight(), Image.SCALE_SMOOTH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			dimg = image.getScaledInstance(lblProfilePic.getWidth(), lblProfilePic.getHeight(), Image.SCALE_SMOOTH);
		}

		return new ImageIcon(dimg);
	}

	public ImageIcon getCheckBoxIcon() {
		Image dimg = image.getScaledInstance(30, 30, Image.SCALE_SMOOTH);

		return new ImageIcon(dimg);
	}
}
