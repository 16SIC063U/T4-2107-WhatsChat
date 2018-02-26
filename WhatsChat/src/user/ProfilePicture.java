package user;
import java.awt.Canvas;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class ProfilePicture extends JPanel  {

	private String username;
	private final static String IMAGE_DIRECTORY = "images";
	private BufferedImage image;

	/**
	 * Add profile picture to path
	 * @return 
	 */
	public boolean selectProfilePic(String username) {
		//Choose file
		String filePath = chooseFile();
		//Create file
		boolean result = copyFile(new File(filePath), new File(IMAGE_DIRECTORY + "/" + username + ".jpg"));
		if(result){
			this.username = username;
		}
		return result;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this); //
	}
	
	public String getImagePath(){
		return IMAGE_DIRECTORY + "/" + username + ".jpg";
	}

	/**
	 * Create image directory if directory does not exist
	 */
	private void createDir() {
		// if the directory does not exist, create it
		File theDir = new File(IMAGE_DIRECTORY);
		if (!theDir.exists()) {
			boolean result = false;

			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				// handle it
			}
			if (result) {
				System.out.println("DIR created");
			}
		}
	}

	private boolean copyFile(File source, File dest){
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
			return false;
		}
		try {
			image = ImageIO.read(dest);
		} catch (IOException e) {
			System.out.println("error adding pic to frame");
		}
		return true;
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

	public File getProfilePic(){
		return new File(getImagePath()); 
	}
	
	public ImageIcon getImageIconProfilePic(JComponent lblProfilePic) {
		Image dimg = image.getScaledInstance(lblProfilePic.getWidth(), lblProfilePic.getHeight(),
		        Image.SCALE_SMOOTH);
		
		return new ImageIcon(dimg);
	}
	
	public ImageIcon getCheckBoxIcon() {
		Image dimg = image.getScaledInstance(30, 30,
		        Image.SCALE_SMOOTH);
		
		return new ImageIcon(dimg);
	}
}
