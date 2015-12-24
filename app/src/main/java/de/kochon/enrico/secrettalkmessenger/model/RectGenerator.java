package de.kochon.enrico.secrettalkmessenger.model;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Rect;



public class RectGenerator {

	private RectGenerator() {
		widthX = 10;
		heightY = 10;
	}
	
	private static RectGenerator instance;
	
	public static RectGenerator createRectGenerator() {
		if (null == instance)
			instance = new RectGenerator();
		return instance;
	}

	private double widthX;
	private double heightY;
	private double offsetX;
	private double offsetY;
	
	public void setWidthX(double w) {
		widthX = w;
	}
	
	public void setHeightY(double h) {
		heightY = h;
	}
	
	public void setOffsetX(double ox) {
		offsetX = (int)ox;
	}
	
	public void setOffsetY(double oy) {
		offsetY = (int)oy;
	}
	
	public List<Rect> createRects(String representation, int targetBit) {
		List<Rect> result = new ArrayList<Rect>();
		String lines[] = representation.split("\n");
		double currentX1 = this.offsetX;
		double currentX2 = this.offsetX + this.widthX;
		double currentY1 = this.offsetY;
		double currentY2 = this.offsetY + this.heightY;
		for(String line: lines) {
			String bits[] = line.split(" ");
			for(String bit: bits) {
				if (bit.isEmpty() || ! (bit.equals("1") || bit.equals("0")) )
					throw new IllegalArgumentException("Expected bitstring consisting of 0 or 1.");
				
				if ( ((0 == targetBit) && bit.equals("0")) ||((1 == targetBit) && bit.equals("1")) ) {
					result.add(new Rect((int)(currentX1+0.5), (int)(currentY1+0.5), (int)(currentX2+0.5), (int)(currentY2+0.5)));
				}
				
				currentX1 += widthX;
				currentX2 += widthX;
			}
			currentX1 = this.offsetX;
			currentX2 = this.offsetX + this.widthX;
			currentY1 += heightY;
			currentY2 += heightY;
		}
		return result;
	}
	
	
	public static List<Rect> generatePatternRects(
			int maxXCharCount, int maxYCharCount, int halfCharRes,
			int totalWidth, int totalHeight, double x_block, double y_block, int bit) {
			List<Rect> rects = new ArrayList<Rect>();
			
			RectGenerator gen = RectGenerator.createRectGenerator();
			gen.setWidthX(x_block);
			gen.setHeightY(y_block);
			
			// upper left
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block);
			gen.setOffsetY(totalHeight/2-(maxYCharCount*halfCharRes)*y_block);
			rects.addAll(gen.createRects(Dotmatrix._marker, bit));
			gen.setOffsetX(totalWidth/2-((maxXCharCount-2)*halfCharRes)*x_block); // move two half chars rightwards
			rects.addAll(gen.createRects(Dotmatrix._0x00, bit));
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block);
			gen.setOffsetY(totalHeight/2-((maxYCharCount-2)*halfCharRes)*y_block); // move two half chars downwards
			rects.addAll(gen.createRects(Dotmatrix._0x00, bit));
			
			// upper right
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block + (maxXCharCount-1)*2*halfCharRes*x_block);
			gen.setOffsetY(totalHeight/2-(maxYCharCount*halfCharRes)*y_block);
			rects.addAll(gen.createRects(Dotmatrix._marker, bit));
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block + (maxXCharCount-2)*2*halfCharRes*x_block);
			rects.addAll(gen.createRects(Dotmatrix._0x00, bit));
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block + (maxXCharCount-1)*2*halfCharRes*x_block);
			gen.setOffsetY(totalHeight/2-((maxYCharCount-2)*halfCharRes)*y_block); // move two half chars downwards
			rects.addAll(gen.createRects(Dotmatrix._0x00, bit));
			
			// lower left
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block);
			gen.setOffsetY(totalHeight/2-(maxYCharCount*halfCharRes)*y_block + (maxYCharCount-1)*2*halfCharRes*y_block);
			rects.addAll(gen.createRects(Dotmatrix._marker, bit));
			gen.setOffsetX(totalWidth/2-((maxXCharCount-2)*halfCharRes)*x_block); // move two half chars rightwards
			rects.addAll(gen.createRects(Dotmatrix._0x00, bit));
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block);
			gen.setOffsetY(totalHeight/2-(maxYCharCount*halfCharRes)*y_block + (maxYCharCount-2)*2*halfCharRes*y_block);
			rects.addAll(gen.createRects(Dotmatrix._0x00, bit));
			
			// lower right
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block + (maxXCharCount-1)*2*halfCharRes*x_block);
			gen.setOffsetY(totalHeight/2-(maxYCharCount*halfCharRes)*y_block + (maxYCharCount-1)*2*halfCharRes*y_block);
			rects.addAll(gen.createRects(Dotmatrix._marker, bit));
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block + (maxXCharCount-2)*2*halfCharRes*x_block);
			rects.addAll(gen.createRects(Dotmatrix._0x00, bit));
			gen.setOffsetX(totalWidth/2-(maxXCharCount*halfCharRes)*x_block + (maxXCharCount-1)*2*halfCharRes*x_block);
			gen.setOffsetY(totalHeight/2-(maxYCharCount*halfCharRes)*y_block + (maxYCharCount-2)*2*halfCharRes*y_block);
			rects.addAll(gen.createRects(Dotmatrix._0x00, bit));
			
			return rects;
	    }

}
