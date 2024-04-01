package opengl3d.utils.text;

// Source:
// github.com/SilverTiger/lwjgl3-tutorial/blob/master/src/silvertiger/tutorial/lwjgl/text/Font.java

// Tutorial:
// https://github.com/SilverTiger/lwjgl3-tutorial/wiki/Fonts

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import opengl3d.Settings;
import opengl3d.utils.Shader;

import static java.awt.Font.MONOSPACED;
import static java.awt.Font.PLAIN;
import static java.awt.Font.TRUETYPE_FONT;

/**
 * This class contains a font texture for drawing text.
 *
 * @author Heiko Brumme
 */
public class Font {

	/**
	 * Contains the glyphs for each char.
	 */
	private final Map<Character, Glyph> glyphs;
	/**
	 * Contains the font texture.
	 */
	private final int texture;

	/**
	 * Height of the font.
	 */
	private int fontHeight;

	private int textureWidth, textureHeight;
	private int unicodeLength;
	private int maxWidth;

	private TextRenderer renderer;

	private boolean isReady = false;

    /**
     * Creates a default antialiased font with monospaced glyphs and default
     * size 16.
     */
    public Font() {
        this(new java.awt.Font(MONOSPACED, PLAIN, 24), true);
    }

    /**
     * Creates a default font with monospaced glyphs and default size 16.
     *
     * @param antiAlias Wheter the font should be antialiased or not
     */
    public Font(boolean antiAlias) {
        this(new java.awt.Font(MONOSPACED, PLAIN, 24), antiAlias);
    }

    /**
     * Creates a default antialiased font with monospaced glyphs and specified
     * size.
     *
     * @param size Font size
     */
    public Font(int size) {
        this(new java.awt.Font(MONOSPACED, PLAIN, size), true);
    }

    /**
     * Creates a default font with monospaced glyphs and specified size.
     *
     * @param size      Font size
     * @param antiAlias Wheter the font should be antialiased or not
     */
    public Font(int size, boolean antiAlias) {
        this(new java.awt.Font(MONOSPACED, PLAIN, size), antiAlias);
    }

    /**
     * Creates a antialiased Font from an input stream.
     *
     * @param in   The input stream
     * @param size Font size
     *
     * @throws FontFormatException if fontFile does not contain the required
     *                             font tables for the specified format
     * @throws IOException         If font can't be read
     */
    public Font(InputStream in, int size) throws FontFormatException, IOException {
        this(in, size, true);
    }

    /**
     * Creates a Font from an input stream.
     *
     * @param in        The input stream
     * @param size      Font size
     * @param antiAlias Wheter the font should be antialiased or not
     *
     * @throws FontFormatException if fontFile does not contain the required
     *                             font tables for the specified format
     * @throws IOException         If font can't be read
     */
    public Font(InputStream in, int size, boolean antiAlias) throws FontFormatException, IOException {
        this(java.awt.Font.createFont(TRUETYPE_FONT, in).deriveFont(java.awt.Font.PLAIN , size), antiAlias);
    }

    /**
     * Creates a antialiased font from an AWT Font.
     *
     * @param font The AWT Font
     */
    public Font(java.awt.Font font) {
        this(font, true);
    }

    /**
     * Creates a font from an AWT Font.
     *
     * @param font      The AWT Font
     * @param antiAlias Wheter the font should be antialiased or not
     */
	public Font(java.awt.Font font, boolean antiAlias) {
		glyphs = new HashMap<>();
		texture = createFontTexture(font, antiAlias);
		renderer = new TextRenderer();
		isReady = true;
	}

	public void saveTexture(BufferedImage image, String path) {
		try {
			File outputfile = new File(path);
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isImagesEqual(BufferedImage img1, BufferedImage img2) {
		if(img1 == null || img2 == null) return false;
		if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
			return false;
		}
		
		for (int x = 0; x < img1.getWidth(); x++) {
			for (int y = 0; y < img1.getHeight(); y++) {
				if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
					return false;
				}
			}
		}
		
		return true;
	}

	public boolean isFontReady() {
		return isReady;
	}

    /**
     * Creates a font texture from specified AWT font.
     *
     * @param font      The AWT font
     * @param antiAlias Wheter the font should be antialiased or not
     *
     * @return Font texture
     */
	private int createFontTexture(java.awt.Font font, boolean antiAlias) {
		unicodeLength = Settings.ASCIICharOnly ? 0xFF : 0xFFFF;
		maxWidth = Settings.ASCIICharOnly ? font.getSize()*11 : font.getSize()*211;

		System.out.println("Creating font texture: \""+font.getFontName()+"\" (Size: "+font.getSize()+") started.");
		System.out.println("PROGRESS:-----------------------"+"--------------------------------");
		int progress = ((unicodeLength+1)*2)/64;

		/* Loop through the characters to get charWidth and charHeight */
		int imageWidth = 0;
		int imageHeight = 0;

		int currentWidth = 0;
		int currentHeight = 0;
		BufferedImage nullCharImage = createCharImage(font, (char)0x0080, antiAlias);
		/* Start at char #32, because ASCII 0 to 31 are just control codes */
		for (int i = 0x00; i <= unicodeLength; i++) {
			if(i%progress == progress-1) System.out.print('█');
			if (i == 127) {
					/* ASCII 127 is the DEL control code, so we can skip it */
				continue;
			}

			char c = (char) i;
			BufferedImage ch = createCharImage(font, c, antiAlias);
			if (ch == null) {
				/* If char image is null that font does not contain the char */
				continue;
			}
			if(i!=0x0080) if( isImagesEqual(nullCharImage, ch) ) continue;

			// Calculate width and height
			currentWidth += ch.getWidth();
			currentHeight = Math.max(currentHeight, ch.getHeight());
			fontHeight = Math.max(currentHeight, fontHeight);
			if(currentWidth-maxWidth >= 0 || i == unicodeLength){
				imageWidth = Math.max(imageWidth, currentWidth);
				imageHeight += currentHeight;
				currentWidth = 0;
				currentHeight = 0;
			}

//			imageWidth += ch.getWidth();
//			imageHeight = Math.max(imageHeight, ch.getHeight());
		}

//		fontHeight = imageHeight;

		/* Image for the texture */
		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();

		int x = 0;
		int y = 0;
		int currentLineHeight = 0;

		/* Create image for the standard chars, again we omit ASCII 0 to 31
		 * because they are just control codes */
		for (int i = 0; i <= unicodeLength; i++) {
			if(i%progress == progress-1) System.out.print('█');
			if (i == 127) {
				/* ASCII 127 is the DEL control code, so we can skip it */
				continue;
			}

			char c = (char) i;
			BufferedImage charImage = createCharImage(font, c, antiAlias);
			if (charImage == null) {
				/* If char image is null that font does not contain the char */
				continue;
			}
			if(i!=0x0080) if( isImagesEqual(nullCharImage, charImage) ) continue;

			int charWidth = charImage.getWidth();
			int charHeight = charImage.getHeight();

			/* Create glyph and draw char on image */
			currentLineHeight = Math.max(currentLineHeight, charHeight);

			Glyph ch = new Glyph(charWidth, charHeight, x, (image.getHeight()-y) - charHeight, 0f);
			g.drawImage(charImage, x, y, null);
			x += charWidth;

			if(x-maxWidth >= 0 || i == unicodeLength){
				y += currentLineHeight;
				currentLineHeight = 0;
				x = 0;
			}

			glyphs.put(c, ch);
		}
		System.out.print('\n');
		saveTexture(image, "C:/Users/fawwazhp/Pictures/FlashIntegro/AAAimage.png");

		/* Flip image Horizontal to get the origin to bottom left */
		//AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
		//transform.translate(0, -image.getHeight());
		//AffineTransformOp operation = new AffineTransformOp(transform,
		//                                                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		//image = operation.filter(image, null);

		/* Get charWidth and charHeight of image */
		int width = image.getWidth();
		int height = image.getHeight();

		System.out.println("PREPARE FOR RENDERING: \""+font.getFontName()+"\" ("+width+" x "+height+" px).");
		System.out.println("RENDERING:----------------------"+"--------------------------------");
		int progress_render = (width*height)/64;
		int progress_render_step = 1;

		/* Get pixel data of image */
		// int[] pixels = new int[width * height];
		// image.getRGB(0, 0, width, height, pixels, 0, width);

		/* Put pixel data into a ByteBuffer */
		ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 1);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if(progress_render_step%progress_render == progress_render-1) System.out.print('█');
				/* Pixel as RGBA: 0xAARRGGBB */
				//int pixel = pixels[i * width + j];
				/* Red component 0xAARRGGBB >> 16 = 0x0000AARR */
				//buffer.put((byte) ((pixel >> 16) & 0xFF));
				/* Green component 0xAARRGGBB >> 8 = 0x00AARRGG */
				//buffer.put((byte) ((pixel >> 8) & 0xFF));
				/* Blue component 0xAARRGGBB >> 0 = 0xAARRGGBB */
				//buffer.put((byte) (pixel & 0xFF));
				/* Alpha component 0xAARRGGBB >> 24 = 0x000000AA */
				//buffer.put((byte) ((pixel >> 24) & 0xFF));

				int color = image.getRGB(j, (height-1)-i);
				buffer.put((byte) ((color >> 16) & 0xFF));
				progress_render_step++;
			}
		}
		System.out.print('\n');
		/* Do not forget to flip the buffer! */
		buffer.flip();

		/* Create texture */
		int textureId = GL30.glGenTextures();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId);
		GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);

		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);

		if(buffer != null) GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_R8, width, height, 0, GL30.GL_RED, GL30.GL_UNSIGNED_BYTE, buffer);

		textureWidth = width;
		textureHeight = height;
		System.out.println("Font height = " + fontHeight + " pixel.");
		System.out.println("Font texture created: \""+font.getFontName()+"\" ("+width+" x "+height+" px).");
		MemoryUtil.memFree(buffer);
		return textureId;
	}

    /**
     * Creates a char image from specified AWT font and char.
     *
     * @param font      The AWT font
     * @param c         The char
     * @param antiAlias Wheter the char should be antialiased or not
     *
     * @return Char image
     */
    private BufferedImage createCharImage(java.awt.Font font, char c, boolean antiAlias) {
        /* Creating temporary image to extract character size */
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.dispose();

        /* Get char charWidth and charHeight */
        int charWidth = metrics.charWidth(c);
        int charHeight = metrics.getHeight();

        /* Check if charWidth is 0 */
        if (charWidth == 0) {
            return null;
        }

        /* Create image for holding the char */
        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_RGB);
        g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        g.setPaint(java.awt.Color.WHITE);
        g.drawString(String.valueOf(c), 0, metrics.getAscent());
        g.dispose();
        return image;
    }

    /**
     * Gets the width of the specified text.
     *
     * @param text The text
     *
     * @return Width of text
     */
    public int getWidth(CharSequence text) {
        int width = 0;
        int lineWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if( (int)c > unicodeLength ) c = (char)0x0080;
            if (c == '\n') {
                /* Line end, set width to maximum from line width and stored
                 * width */
                width = Math.max(width, lineWidth);
                lineWidth = 0;
                continue;
            }
            if (c == '\r') {
                /* Carriage return, just skip it */
                continue;
            }
            Glyph g = glyphs.get(c);
            lineWidth += g.width;
        }
        width = Math.max(width, lineWidth);
        return width;
    }

    /**
     * Gets the height of the specified text.
     *
     * @param text The text
     *
     * @return Height of text
     */
    public int getHeight(CharSequence text) {
        int height = 0;
        int lineHeight = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if( (int)c > unicodeLength ) c = (char)0x0080;
            if (c == '\n') {
                /* Line end, add line height to stored height */
                height += lineHeight;
                lineHeight = 0;
                continue;
            }
            if (c == '\r') {
                /* Carriage return, just skip it */
                continue;
            }
            Glyph g = glyphs.get(c);
            int charHeight = 0;
            if(g != null) charHeight = g.height;
            lineHeight = Math.max(lineHeight, charHeight);
        }
        height += lineHeight;
        return height;
    }

    /**
     * Draw text at the specified position and color.
     *
     * @param renderer The renderer to use
     * @param text     Text to draw
     * @param x        X coordinate of the text position
     * @param y        Y coordinate of the text position
     * @param c        Color to use
     */
    public void drawText(Shader shader, int[] res, float x, float y, float width, float height, CharSequence text, int globalColor) {
		int currentColor = globalColor;
//        int textHeight = getHeight(text);

		float startX = x - res[0];
		float startY = y + res[1];
		float drawX = startX;
		float drawY = startY;
//        if (textHeight > fontHeight) {
//            drawY += textHeight - fontHeight;
//        }

//      renderer.begin();

		boolean specialSeq = false;
		int sequence = -1;
		String color = "";
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if(i == 0) drawY -= fontHeight;
			if (ch == '\n') {
				/* Line feed, set x and y to draw at the next line */
				drawY -= fontHeight;
				drawX = startX;
				continue;
			}
			if (ch == '\r') {
				/* Carriage return, just skip it */
				continue;
			}
			if (ch == '$' && !specialSeq) {
				specialSeq = true;
				continue;
			}
			if (specialSeq) {
				switch(ch) {
					case '$': if(sequence == -1) {
						specialSeq = false;
						break;
					}
					case 'c': if(sequence == -1) {
						sequence = 1;
						continue;
					}
					default: {
						break;
					}
				}
				specialSeq = false;
			}
			if(sequence != -1) {
				switch(sequence) {
					case 1: {
						if(	color.length() < 8 && (
							Character.toLowerCase(ch) == 'a' ||
							Character.toLowerCase(ch) == 'b' ||
							Character.toLowerCase(ch) == 'c' ||
							Character.toLowerCase(ch) == 'd' ||
							Character.toLowerCase(ch) == 'e' ||
							Character.toLowerCase(ch) == 'f' ||
							ch == '0' ||
							ch == '1' ||
							ch == '2' ||
							ch == '3' ||
							ch == '4' ||
							ch == '5' ||
							ch == '6' ||
							ch == '7' ||
							ch == '8' ||
							ch == '9'
							)
						) {
							color = color + ch;
						} else {
							if(color.length() == 8) {
								try{
									currentColor = (int) Long.parseLong(color, 16);
								} catch(NumberFormatException e) {
									currentColor = globalColor;
								}
							}
							specialSeq = false;
							sequence = -1;
							color = "";
							break;
						}
						continue;
					}
					default: {
						specialSeq = false;
						sequence = -1;
						break;
					}
				}
			}

			Glyph g = glyphs.get(ch);
			int gx=0, gy=0, gw=0, gh=0;
			if(g != null){
				gx=g.x; gy=g.y; gw=g.width; gh=g.height;
			} else {
				Glyph unknowChar = glyphs.get((char)0x0080);
				gx=unknowChar.x; gy=unknowChar.y; gw=unknowChar.width; gh=unknowChar.height;
//				System.out.println("FONT ERROR drawing text character: u+" + Integer.toHexString(ch | 0x10000).substring(1) );
//				System.out.println("TEXT: \""+text+"\"");
			}
			if(drawX + gw > x + width) {
				drawY -= fontHeight;
				drawX = startX;
			}
			if(drawY < startY - height) break;
			// referensi render text dengan linebreak CJK support https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/text/BreakIterator.html
			renderer.render(shader, texture, res, textureWidth, textureHeight, drawX, drawY, gx, gy, gw, gh, currentColor);
//			renderer.drawTextureRegion(texture, drawX, drawY, g.x, g.y, g.width, g.height, color);
			drawX += gw;
		}
//        renderer.end();
    }

    /**
     * Draw text at the specified position.
     *
     * @param renderer The renderer to use
     * @param text     Text to draw
     * @param x        X coordinate of the text position
     * @param y        Y coordinate of the text position
     */
    public void drawText(Shader shader, int[] res, float x, float y, float w, float h, CharSequence text) {
    	drawText(shader, res, x, y, w, h, text, 0xFFFFFFFF);
    }

    /**
     * Disposes the font.
     */
    public void dispose() {
    	GL30.glDeleteTextures(texture);
    }

}