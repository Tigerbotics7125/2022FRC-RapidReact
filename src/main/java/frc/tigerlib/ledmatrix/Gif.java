/**
 * Copyright (C) 2022, Tigerbotics' team members and all other contributors.
 * Open source software; you can modify and/or share this software.
 */
package frc.tigerlib.ledmatrix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Gif {

    public File file;
    public ArrayList<Integer[][][]> frames; // ArrayList<int[widthOfGif][heightOfGif][RGBA]>
    public int frameDelay;
    public int width;
    public int height;

    public Gif(File file) throws Exception {
        this.file = file;
        isGif(file);
        objectize(file); // convert to object.
    }

    private void isGif(File file) throws Exception {
        if (!file.getAbsolutePath().endsWith(".gif")) {
            throw new Exception("The file must end with \".gif\"");
        }
    }

    /**
     * Converts a ".gif" file into a Java Object.
     *
     * @param file the ".gif" file
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void objectize(File file) throws FileNotFoundException, IOException {
        ImageFrame[] imageFrames = readGif(new FileInputStream(file));

        ImageFrame iFrame =
                imageFrames[0]; // assumes that all frames are the same widht height and delay for
        // simplicity sake.
        this.width = iFrame.getImage().getWidth();
        this.height = iFrame.getImage().getHeight();

        this.frames = new ArrayList<Integer[][][]>(); // sets a value in frames so it's not null.
        this.frameDelay = iFrame.getDelay(); // sets the delay.

        // fill the ArrayList with data.
        for (ImageFrame imageFrame : imageFrames) {

            Integer[][][] frame = new Integer[this.width][this.height][4];

            /**
             * fill in frame in row major order <code><pre>
             * 0 > 1 > 2 > 3
             *             v
             * <<<<<<<<<<<<<
             * v
             * 4 > 5 > 6 > 7
             * </code></pre>
             *
             * <p>iterate over row then column
             */
            for (int row = 0; row < this.width; row++) {
                for (int column = 0; column < this.height; column++) {
                    Color c = new Color(imageFrame.getImage().getRGB(row, column));

                    frame[row][column][0] = c.getRed();
                    frame[row][column][1] = c.getGreen();
                    frame[row][column][2] = c.getBlue();
                }
            }

            frames.add(frame); // Add the frame once everything is done.
        }
    }

    // https://stackoverflow.com/a/18425922/16294208
    // thank God.
    private ImageFrame[] readGif(FileInputStream stream) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<ImageFrame>(2);

        ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(stream));

        int width = -1;
        int height = -1;

        IIOMetadata metadata = reader.getStreamMetadata();
        if (metadata != null) {
            IIOMetadataNode globalRoot =
                    (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

            NodeList globalScreenDescriptor =
                    globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }
        }

        BufferedImage master = null;
        Graphics2D masterGraphics = null;

        for (int frameIndex = 0; ; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root =
                    (IIOMetadataNode)
                            reader.getImageMetadata(frameIndex)
                                    .getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce =
                    (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            int delay = Integer.valueOf(gce.getAttribute("delayTime"));
            String disposal = gce.getAttribute("disposalMethod");

            int x = 0;
            int y = 0;

            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else {
                NodeList children = root.getChildNodes();
                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    Node nodeItem = children.item(nodeIndex);
                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }
            }
            masterGraphics.drawImage(image, x, y, null);

            BufferedImage copy =
                    new BufferedImage(
                            master.getColorModel(),
                            master.copyData(null),
                            master.isAlphaPremultiplied(),
                            null);
            frames.add(new ImageFrame(copy, delay, disposal));

            if (disposal.equals("restoreToPrevious")) {
                BufferedImage from = null;
                for (int i = frameIndex - 1; i >= 0; i--) {
                    if (!frames.get(i).getDisposal().equals("restoreToPrevious")
                            || frameIndex == 0) {
                        from = frames.get(i).getImage();
                        break;
                    }
                }

                master =
                        new BufferedImage(
                                from.getColorModel(),
                                from.copyData(null),
                                from.isAlphaPremultiplied(),
                                null);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else if (disposal.equals("restoreToBackgroundColor")) {
                masterGraphics.clearRect(x, y, image.getWidth(), image.getHeight());
            }
        }
        reader.dispose();

        return frames.toArray(new ImageFrame[frames.size()]);
    }
}
