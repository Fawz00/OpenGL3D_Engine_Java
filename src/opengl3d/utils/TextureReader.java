package opengl3d.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class TextureReader {
	private boolean PRINT_LOG = false;

	private boolean blur = false;
	private int filter = GL30.GL_REPEAT;
	private int[] textures = new int[5];
	private boolean hasColor = true;
	private boolean hasNormal = false;
	private boolean hasParallax = false;
	private boolean hasMER = false;
	private boolean hasAO = false;

	public TextureReader(boolean col, boolean nor, boolean par, boolean mer, boolean ao, boolean inBlur, int inFilter){
		blur = inBlur;
		filter = inFilter;
		hasColor=col;hasNormal=nor;hasParallax=par;hasMER=mer;hasAO=ao;

		GL30.glGenTextures(textures);
	}
	public TextureReader(){
		GL30.glGenTextures(textures);
	}
	public void setTextureColor(String dir){
		if(hasColor == true){
			textures[0] = loadTexture(0, dir, blur, filter);
			if(PRINT_LOG) System.out.println("INFO: " + "TEXTURE READER: "+dir+"\nSuccess set the color texture");
		} else if(PRINT_LOG) System.out.println("ERROR: " + "TEXTURE READER: "+dir+"\nError set the color texture");
	}
	public void setTextureNor(String dir){
		if(hasNormal == true && dir != null){
			textures[1] = loadTexture(1, dir, blur, filter);
			if(PRINT_LOG) System.out.println("INFO: " + "TEXTURE READER: "+dir+"\nSuccess set the normal texture");
		} else {
			hasNormal= false;
			if(PRINT_LOG) System.out.println("ERROR: " + "TEXTURE READER: "+dir+"\nError set the normal texture");
		}
	}
	public void setTexturePar(String dir){
		if(hasParallax == true && dir != null){
			textures[2] = loadTexture(2, dir, blur, filter);
			if(PRINT_LOG) System.out.println("INFO: " + "TEXTURE READER: "+dir+"\nSuccess set the parallax texture");
		} else {
			hasParallax= false;
			if(PRINT_LOG) System.out.println("ERROR: " + "TEXTURE READER: "+dir+"\nError set the parallax texture");
		}
	}
	public void setTextureMer(String dir){
		if(hasMER == true && dir != null){
			textures[3] = loadTexture(3, dir, blur, filter);
			if(PRINT_LOG) System.out.println("INFO: " + "TEXTURE READER: "+dir+"\nSuccess set the MER texture");
		} else {
			hasMER= false;
			if(PRINT_LOG) System.out.println("ERROR: " + "TEXTURE READER: "+dir+"\nError set the MER texture");
		}
	}
	public void setTextureAo(String dir){
		if(hasAO == true && dir != null){
			textures[4] = loadTexture(4, dir, blur, filter);
			if(PRINT_LOG) System.out.println("INFO: " + "TEXTURE READER: "+dir+"\nSuccess set the Ambient Occlusion texture");
		} else {
			hasAO= false;
			if(PRINT_LOG) System.out.println("ERROR: " + "TEXTURE READER: "+dir+"\nError set the Ambient Occlusion texture");
		}
	}
	public boolean hasNormal(){
		return hasNormal;
	}
	public boolean hasParallax(){
		return hasParallax;
	}
	public boolean hasMer(){
		return hasMER;
	}
	public boolean hasAo(){
		return hasAO;
	}
	public int getTextureColor(){
		if(textures[0] != 0) return textures[0];
		return 0;
	}
	public int getTextureNor(){
		if(textures[1] != 0) return textures[1];
		return 0;
	}
	public int getTexturePar(){
		if(textures[2] != 0) return textures[2];
		return 0;
	}
	public int getTextureMer(){
		if(textures[3] != 0) return textures[3];
		return 0;
	}
	public int getTextureAo(){
		if(textures[4] != 0) return textures[4];
		return 0;
	}
	public void deleteTextures(){
		GL30.glDeleteTextures(textures);
	}



	public int loadTexture(int pos, String filePath, boolean blur, int filter) {
		int width, height;
		ByteBuffer buffer;
		if(textures[pos] != 0) try {
			BufferedImage image = ImageIO.read(new File(filePath));

			width = image.getWidth();
			height = image.getHeight();
	
			int[] pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
	
			buffer = MemoryUtil.memAlloc(width * height * 4);
	
			for (int y = 0; y < height; y++){
				for (int x = 0; x < width; x++){
					int pixel = pixels[y * width + x];
	
					buffer.put((byte) ((pixel >> 16) & 0xFF));  // red
					buffer.put((byte) ((pixel >> 8) & 0xFF));   // green
					buffer.put((byte) (pixel & 0xFF));          // blue
					buffer.put((byte) ((pixel >> 24) & 0xFF));  // alpha
				}
			}
			buffer.flip();

			GL30.glBindTexture(GL30.GL_TEXTURE_2D, textures[pos]);
			GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);

			if(blur){
				GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
			}else{
				GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
			}
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, filter);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, filter);

			if (buffer != null) {
				GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
			}
			GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);
			MemoryUtil.memFree(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		};
		if (textures[pos] != 0) {
			return textures[pos];
		}
		throw new RuntimeException("Error loading texture.");
	}

}
