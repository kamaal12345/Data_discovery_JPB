package com.jio.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

import net.sourceforge.tess4j.Tesseract;

public class OCRUtils {

    public static String performOCRWithPreprocessing(File imageFile, String tessDataPath) {
        try {
            BufferedImage image = tryReadImage(imageFile);
            if (image == null) {
                System.err.println("⚠️ Skipping unreadable image: " + imageFile.getName());
                return "";
            }

            // Normalize to safe format
            image = normalizeToByteBGR(image);

            // Step 1: Preprocess image
            BufferedImage processedImage;
            try {
                processedImage = preprocessImageForOCR(image);
            } catch (Exception pe) {
                System.err.println("⚠️ Preprocessing failed for " + imageFile.getName() + ": " + pe.getMessage());
                return "";
            }

            // Step 2: OCR
            try {
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath(tessDataPath);
                tesseract.setLanguage("eng");
                tesseract.setOcrEngineMode(1);
                tesseract.setPageSegMode(11);

                return tesseract.doOCR(processedImage);
            } catch (Exception ocrEx) {
                System.err.println("⚠️ OCR failed for " + imageFile.getName() + ": " + ocrEx.getMessage());
            }

        } catch (Exception e) {
            System.err.println("⚠️ Unexpected error on " + imageFile.getName() + ": " + e.getMessage());

            // Retry with fixed image
            File fixed = fixCorruptedImage(imageFile);
            if (fixed != null) {
                try {
                    BufferedImage fixedImg = ImageIO.read(fixed);
                    fixedImg = normalizeToByteBGR(fixedImg);

                    BufferedImage processed = preprocessImageForOCR(fixedImg);

                    Tesseract tesseract = new Tesseract();
                    tesseract.setDatapath(tessDataPath);
                    tesseract.setLanguage("eng");
                    tesseract.setPageSegMode(7);

                    return tesseract.doOCR(processed);
                } catch (Exception ex) {
                    System.err.println("⚠️ Retry OCR failed for " + imageFile.getName() + ": " + ex.getMessage());
                } finally {
                    fixed.delete();
                }
            }
        }
        return "";
    }

    public static BufferedImage preprocessImageForOCR(BufferedImage input) {
        // Step 0: Upscale if too small
        int minWidth = 100;
        int minHeight = 40;

        if (input.getWidth() < minWidth || input.getHeight() < minHeight) {
            int scaleFactor = 2;
            int newWidth = Math.max(input.getWidth() * scaleFactor, minWidth);
            int newHeight = Math.max(input.getHeight() * scaleFactor, minHeight);
            input = resizeImage(input, newWidth, newHeight);
            System.out.println("🔍 Upscaled image to: " + newWidth + "x" + newHeight);
        }

        // Convert to Mat
        Mat mat = bufferedImageToMat(input);

        // Grayscale
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);

        // Adaptive thresholding
        Mat thresh = new Mat();
        opencv_imgproc.adaptiveThreshold(gray, thresh, 255,
                opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                opencv_imgproc.THRESH_BINARY, 11, 2);

        return matToBufferedImage(thresh);
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = resized.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private static Mat bufferedImageToMat(BufferedImage bi) {
        try {
            int channels = bi.getRaster().getNumBands();
            Mat mat;

            if (channels == 1) {
                mat = new Mat(bi.getHeight(), bi.getWidth(), opencv_core.CV_8UC1);
                byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                mat.data().put(pixels);
            } else if (channels == 3) {
                mat = new Mat(bi.getHeight(), bi.getWidth(), opencv_core.CV_8UC3);
                byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                mat.data().put(pixels);
            } else if (channels == 4) {
                byte[] srcPixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                byte[] bgrPixels = new byte[(bi.getWidth() * bi.getHeight()) * 3];
                for (int i = 0, j = 0; i < srcPixels.length; i += 4, j += 3) {
                    bgrPixels[j] = srcPixels[i + 2];     // B
                    bgrPixels[j + 1] = srcPixels[i + 1]; // G
                    bgrPixels[j + 2] = srcPixels[i];     // R
                }
                mat = new Mat(bi.getHeight(), bi.getWidth(), opencv_core.CV_8UC3);
                mat.data().put(bgrPixels);
            } else {
                throw new IllegalArgumentException("Unsupported number of channels: " + channels);
            }

            return mat;
        } catch (Exception e) {
            System.err.println("⚠️ Error converting image to Mat: " + e.getMessage());
            return new Mat(); 
        }
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        byte[] b = new byte[mat.channels() * mat.cols() * mat.rows()];
        mat.data().get(b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), b);
        return image;
    }

    private static BufferedImage tryReadImage(File file) {
        try {
            if (!file.exists() || file.length() == 0) {
                System.err.println("⚠️ File missing or empty: " + file.getAbsolutePath());
                return null;
            }
            return ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("⚠️ Image read error for " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }

    public static File fixCorruptedImage(File originalImage) {
        try {
            BufferedImage img = ImageIO.read(originalImage);
            if (img == null) {
                System.err.println("⚠️ Cannot fix unreadable image: " + originalImage.getName());
                return null;
            }

            img = normalizeToByteBGR(img);

            File fixedFile = File.createTempFile("fixed_", ".jpg");
            ImageIO.write(img, "jpg", fixedFile);
            System.out.println("✅ Fixed image written to: " + fixedFile.getAbsolutePath());
            return fixedFile;

        } catch (IOException e) {
            System.err.println("⚠️ Image fix failed for " + originalImage.getName() + ": " + e.getMessage());
            return null;
        }
    }

    // ✅ Normalization helper
    private static BufferedImage normalizeToByteBGR(BufferedImage img) {
        if (img == null) return null;

        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR ||
            img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return img;
        }

        BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return newImage;
    }
}
