package opengl3d.utils.text;

// Source:
// github.com/SilverTiger/lwjgl3-tutorial/blob/master/src/silvertiger/tutorial/lwjgl/text/Font.java
// with many changes

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
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import opengl3d.Settings;
import opengl3d.ui.UIRenderer;
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
	private final Map<Character, Glyph> glyphs;
	private final int texture;


	private int fontHeight;

	private int textureWidth, textureHeight;
	private int unicodeLength;
	private int maxWidth;

	private TextRenderer renderer;

	private boolean isReady = false;
	private boolean debugMode = false;

    public Font() {
        this(new java.awt.Font(MONOSPACED, PLAIN, 24), true);
    }

    public Font(boolean antiAlias) {
        this(new java.awt.Font(MONOSPACED, PLAIN, 24), antiAlias);
    }

    public Font(int size) {
        this(new java.awt.Font(MONOSPACED, PLAIN, size), true);
    }

    public Font(int size, boolean antiAlias) {
        this(new java.awt.Font(MONOSPACED, PLAIN, size), antiAlias);
    }

    public Font(InputStream in, int size) throws FontFormatException, IOException {
        this(in, size, true);
    }

    public Font(InputStream in, int size, boolean antiAlias) throws FontFormatException, IOException {
        this(java.awt.Font.createFont(TRUETYPE_FONT, in).deriveFont(java.awt.Font.PLAIN , size), antiAlias);
    }

    public Font(java.awt.Font font) {
        this(font, true);
    }

	public Font(java.awt.Font font, boolean antiAlias) {
		debugMode = Settings.fontDebug;
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
		
		for (int x = 0; x < img1.getWidth(); x+=2) {
			for (int y = 0; y < img1.getHeight(); y+=2) {
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

	private int createFontTexture(java.awt.Font font, boolean antiAlias) {
		unicodeLength = Settings.ASCIICharOnly ? 0xFF : 0xFFFF;
		maxWidth = Settings.ASCIICharOnly ? font.getSize()*11 : font.getSize()*211;

		System.out.println("Creating font texture: \""+font.getFontName()+"\" (Size: "+font.getSize()+") started.");
		System.out.println("PROGRESS:-----------------------"+"--------------------------------"+"--------------------------------"+"--------------------------------");
		int progress = ((unicodeLength+1)*2)/128;

		int imageWidth = 0;
		int imageHeight = 0;

		int currentWidth = 0;
		int currentHeight = 0;
		BufferedImage nullCharImage = createCharImage(font, (char)0x0080, antiAlias, false);
		Vector<Character> controlChar = new Vector<>();

		for (int i = 0x00; i <= unicodeLength; i++) {
			if(i%progress == progress-1) System.out.print('█');
			if (i == 127) {
				continue;
			}

			char c = (char) i;
			BufferedImage ch = createCharImage(font, c, antiAlias, false);
			if (ch == null) {
				continue;
			}
			if(c!=0x0080 && isImagesEqual(nullCharImage, ch) ) {
				controlChar.add(c);
				continue;
			}

			// Calculate width and height
			currentWidth += ch.getWidth();
			currentHeight = Math.max(currentHeight, ch.getHeight());
			fontHeight = Math.max(currentHeight, fontHeight);
			if(currentWidth-maxWidth > 0){
				imageWidth = Math.max(imageWidth, currentWidth);
				imageHeight += currentHeight;
				currentWidth = 0;
				currentHeight = 0;
			}

//			imageWidth += ch.getWidth();
//			imageHeight = Math.max(imageHeight, ch.getHeight());
		}
		imageWidth = Math.max(imageWidth, currentWidth);
		imageHeight += currentHeight;

//		fontHeight = imageHeight;

		/* Image for the texture */
		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
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
			if(i!=0x0080 && controlChar.contains(c) ) continue;
			BufferedImage charImage = createCharImage(font, c, antiAlias, debugMode);
			if (charImage == null) {
				/* If char image is null that font does not contain the char */
				continue;
			}

			int charWidth = charImage.getWidth();
			int charHeight = charImage.getHeight();

			/* Create glyph and draw char on image */
			currentLineHeight = Math.max(currentLineHeight, charHeight);

			Glyph ch = new Glyph(charWidth, charHeight, x, (image.getHeight()-y) - charHeight, 0f);
			g.drawImage(charImage, x, y, null);
			x += charWidth;

			if(x-maxWidth > 0){
				y += currentLineHeight;
				currentLineHeight = 0;
				x = 0;
			}

			glyphs.put(c, ch);
		}
		y += currentLineHeight;

		System.out.print('\n');
		saveTexture(image, "C:/Users/fawwazhp/Pictures/FlashIntegro/"+font.getName()+".png");

		int width = image.getWidth();
		int height = image.getHeight();

		System.out.println("PREPARE FOR RENDERING: \""+font.getFontName()+"\" ("+width+" x "+height+" px).");
		System.out.println("RENDERING:----------------------"+"--------------------------------"+"--------------------------------"+"--------------------------------");
		int progress_render = (width*height)/128;
		int progress_render_step = 1;

		ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 1);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if(progress_render_step%progress_render == progress_render-1) System.out.print('█');

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

    private BufferedImage createCharImage(java.awt.Font font, char c, boolean antiAlias, boolean debug) {
        /* Creating temporary image to extract character size */
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
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
        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_BYTE_GRAY);
        g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        g.setPaint(java.awt.Color.WHITE);
		g.drawString(String.valueOf(c), 0, metrics.getAscent());

		if(debug) {
			g.scale(0.455, 0.455);
			g.drawString(Integer.toHexString(c), 0, metrics.getAscent());
		}

        g.dispose();
        return image;
    }

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
			if(g != null){
				lineWidth += g.width;
			} else {
				Glyph unknowChar = glyphs.get((char)0x0080);
				lineWidth += unknowChar.width;
			}
        }
        width = Math.max(width, lineWidth);
        return width;
    }

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

	private boolean isCJK(char ch) {
		return
		((ch >= 0x4E00 && ch <= 0x9FFF) || // Chinese characters
		(ch >= 0x3040 && ch <= 0x309F) || // Hiragana
		(ch >= 0x30A0 && ch <= 0x30FF) || // Katakana
		(ch >= 0xAC00 && ch <= 0xD7AF) || // Hangul Syllables
		(ch >= 0x3000 && ch <= 0x303F)); // CJK Symbols and Punctuation
	}

    public void drawWord(Shader shader, float ox, float oy, float width, float height, int rotation, String text, int globalColor) {
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);
		int start = boundary.first();

		Vector<String> words = new Vector<>();
		Vector<Integer> pixelWidth = new Vector<>();

		final int x = (int)-width/2;
		final int y = (int)-height/2;

		int wordStartX = x;
		int wordDrawX = wordStartX;
		int wordDrawY = -y;

		for (
				int end = boundary.next();
				end != BreakIterator.DONE;
				start = end, end = boundary.next()
		){
			String wordString = text.substring(start,end);
			if(wordString.equals(" ")) {
				continue;
			}

			words.add(wordString);
			pixelWidth.add(getWidth(wordString));
		}

		int spaceWidth = glyphs.get(' ').width;
		int carriage = 0;
		boolean prevCJKWord = false;
		boolean currCJKWord = false;
		for(int k=0; k<words.size(); k++) {
			String wordString = words.get(k);
			if (wordString.equals("\n")) {
				wordDrawY -= fontHeight;
				wordDrawX = wordStartX;
				continue;
			}

			int wordWidth = 0;
			currCJKWord = isCJK(words.get(k).charAt(0));
			if(k != 0) {
				prevCJKWord = isCJK(words.get(k-1).charAt(0));
				if(!words.get(k-1).equals("\n") && !(prevCJKWord&&currCJKWord)) wordDrawX += spaceWidth;
			}
			wordWidth += pixelWidth.get(k);
			if(wordDrawX + wordWidth > wordStartX + width) {
				if(carriage == 0) wordDrawY -= fontHeight;
				wordDrawX = wordStartX + carriage;
				if(carriage > 0) if(wordDrawX + wordWidth > wordStartX + width) {
					wordDrawY -= fontHeight;
					wordDrawX = wordStartX;
				}
				carriage = 0;
			}
			if(wordDrawY < y - height) break;
			if(k == 0) wordDrawY -= fontHeight;

			int currentColor = globalColor;

			int startX = wordDrawX;
			int drawX = startX;
			int drawY = wordDrawY;

			for (int i = 0; i < wordString.length(); i++) {
				char ch = wordString.charAt(i);
				if (ch == '\r') {
					continue;
				}

				Glyph g = glyphs.get(ch);
				int gx=0, gy=0, gw=0, gh=0;
				if(g != null){
					gx=g.x; gy=g.y; gw=g.width; gh=g.height;
				} else {
					Glyph unknowChar = glyphs.get((char)0x0080);
					gx=unknowChar.x; gy=unknowChar.y; gw=unknowChar.width; gh=unknowChar.height;
				}
				if(drawX + gw > startX + width) {
					wordDrawY -= fontHeight;
					wordDrawX = wordStartX;
					carriage += gw;
					if(!(prevCJKWord&&currCJKWord)) carriage += spaceWidth;
					startX = wordDrawX;
					drawY -= fontHeight;
					drawX = startX;
				} else if(carriage > 0) carriage += gw;
				if(startX + carriage > startX + width) carriage = 0;
				if(drawY < -y - height) break;

				// referensi render text dengan linebreak CJK support https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/text/BreakIterator.html
				renderer.render(shader, texture, UIRenderer.getScreenSize(), (int)width, (int)height, (int)ox, (int)oy, rotation, textureWidth, textureHeight, drawX, drawY, gx, gy, gw, gh, currentColor);
				drawX += gw;
			}
			wordDrawX += wordWidth;
		}
    }

	public void drawText(Shader shader, float x, float y, float width, float height, int rotation, CharSequence text, int globalColor) {
		int currentColor = globalColor;
//        int textHeight = getHeight(text);

		int startX = (int)-width/2;
		int startY = (int)height/2;
		int drawX = startX;
		int drawY = startY;
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
			if(drawX + gw > startX + width) {
				drawY -= fontHeight;
				drawX = startX;
			}
			if(drawY < startY - height) break;
			// referensi render text dengan linebreak CJK support https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/text/BreakIterator.html
			renderer.render(shader, texture, UIRenderer.getScreenSize(), (int)width, (int)height, (int)x, (int)y, rotation, textureWidth, textureHeight, drawX, drawY, gx, gy, gw, gh, currentColor);
//			renderer.drawTextureRegion(texture, drawX, drawY, g.x, g.y, g.width, g.height, color);
			drawX += gw;
		}
//        renderer.end();
	}

    public void drawText(Shader shader, float x, float y, float w, float h, int rotation, String text) {
    	drawText(shader, x, y, w, h, rotation, text, 0xFFFFFFFF);
    }

    public void dispose() {
    	GL30.glDeleteTextures(texture);
    }

}