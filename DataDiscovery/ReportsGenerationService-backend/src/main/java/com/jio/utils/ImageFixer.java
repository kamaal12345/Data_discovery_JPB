package com.jio.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Utility to fix corrupted or improperly structured images (e.g. JPEGs with
 * metadata issues).
 */
public class ImageFixer {

	public static File fixImage(File originalImageFile) throws IOException {
		BufferedImage original = ImageIO.read(originalImageFile);
		if (original == null) {
			System.err.println("❌ Unable to read image: " + originalImageFile.getAbsolutePath());
			return null;
		}

		BufferedImage cleanedImage = new BufferedImage(original.getWidth(), original.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		Graphics2D g = cleanedImage.createGraphics();
		g.drawImage(original, 0, 0, null);
		g.dispose();

		File fixedImageFile = new File(originalImageFile.getParent(), "fixed_" + originalImageFile.getName());
		boolean success = ImageIO.write(cleanedImage, "jpg", fixedImageFile);

		if (success) {
			System.out.println("✅ Fixed image saved: " + fixedImageFile.getAbsolutePath());
			return fixedImageFile;
		} else {
			System.err.println("❌ Failed to write fixed image.");
			return null;
		}
	}
}
