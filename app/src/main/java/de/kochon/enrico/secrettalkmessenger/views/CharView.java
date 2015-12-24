package de.kochon.enrico.secrettalkmessenger.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

import de.kochon.enrico.secrettalkmessenger.model.Dotmatrix;
import de.kochon.enrico.secrettalkmessenger.model.RectGenerator;

import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

public class CharView extends View {
	protected Paint ink;
	protected Paint antiink;
	protected Paint paper;
	protected Paint indicator;
	protected Rect r;
	
	protected byte[] id;
	protected byte[] body;
	
	public static List<Rect> screenPatternRects = new ArrayList<Rect>();	
	
 
	public CharView(Context context) {
		super(context);
		initialize();
	}
	
	public CharView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    initialize();
	}

	public CharView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    initialize();
	}
	
	public void initialize() {
		body = new byte[0];
		id = new byte[0];
		r = new Rect();
		ink = new Paint();
        ink.setStyle(Paint.Style.FILL); 
        ink.setColor(Color.BLACK); 
		antiink = new Paint();
        antiink.setStyle(Paint.Style.FILL); 
        antiink.setColor(Color.WHITE); 
        paper = new Paint();
        paper.setStyle(Paint.Style.FILL);
		paper.setColor(Color.argb(255, 255, 255, 255));	
        indicator = new Paint();
        indicator.setStyle(Paint.Style.FILL);
		indicator.setColor(Color.RED);	
	}
	
	
	public void setKey(byte[] id, byte[] body) {
		this.id = id;
		this.body = body;
		invalidate();
	}
	
	protected void drawIndicators(Canvas canvas, int w, int h) {
		r.left=0;
		r.top=0;
		r.right=4;
		r.bottom=4;
		canvas.drawRect(r, indicator);
		r.left=4;
		r.top=0;
		r.right=8;
		r.bottom=4;
		canvas.drawRect(r, indicator);
		r.left=0;
		r.top=4;
		r.right=4;
		r.bottom=8;
		canvas.drawRect(r, indicator);
		r.left=w/2-2;
		r.top=0;
		r.right=w/2+2;
		r.bottom=4;
		canvas.drawRect(r, indicator);
		r.left=w-4;
		r.top=0;
		r.right=w;
		r.bottom=4;
		canvas.drawRect(r, indicator);
		r.left=0;
		r.top=h/2-2;
		r.right=4;
		r.bottom=h/2+2;
		canvas.drawRect(r, indicator);
		r.left=0;
		r.top=h-4;
		r.right=4;
		r.bottom=h;
		canvas.drawRect(r, indicator);
		r.left=w-4;
		r.top=h/2-2;
		r.right=w;
		r.bottom=h/2+2;
		canvas.drawRect(r, indicator);
		r.left=w/2-2;
		r.top=h-4;
		r.right=w/2+2;
		r.bottom=h;
		canvas.drawRect(r, indicator);
		r.left=w-4;
		r.top=h-4;
		r.right=w;
		r.bottom=h;
		canvas.drawRect(r, indicator);
	}
	
	
	protected String getBitsForNibble(int nibble) {
		if ( (nibble < 0) || (nibble > 15) ) return "";
		switch (nibble) {
                case 1: return Dotmatrix._0x01;
                case 2: return Dotmatrix._0x02;
                case 3: return Dotmatrix._0x03;
                case 4: return Dotmatrix._0x04;
                case 5: return Dotmatrix._0x05;
                case 6: return Dotmatrix._0x06;
                case 7: return Dotmatrix._0x07;
                case 8: return Dotmatrix._0x08;
                case 9: return Dotmatrix._0x09;
                case 10: return Dotmatrix._0x0A;
                case 11: return Dotmatrix._0x0B;
                case 12: return Dotmatrix._0x0C;
                case 13: return Dotmatrix._0x0D;
                case 14: return Dotmatrix._0x0E;
                case 15: return Dotmatrix._0x0F;
                default: return Dotmatrix._0x00;
		}
	}


   protected int getNibble(int pos, byte[] binarydata) {
      if ((null == binarydata) || ( pos < 0) || (pos/2 >= binarydata.length)) {
         return 0;
      }
      byte relevantByte = binarydata[pos/2];
      int nibble = 0;
      if (0==pos%2) {
         nibble = 0x0f & (relevantByte >> 4);
      } else {
         nibble = 0x0f & relevantByte;
      }
      return nibble;
   }

	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
      // static assertions relevant for the representation
      if ( (16!=Messagekey.HEADERID_LENGTH) || (Messagekey.HEADERID_LENGTH != id.length) ||
           (64!=Messagekey.KEYBODY_LENGTH  ) || (Messagekey.KEYBODY_LENGTH != body.length)) 
      {
         return;
      }
      int horizontalCount = 18;
      int verticalCount = 10;
		
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		r.left=0;
		r.top=0;
		r.right=w;
		r.bottom=h;
		canvas.drawRect(r, paper);
		
		drawIndicators(canvas, w, h);
			
		int pixel_per_block = w/((horizontalCount)*8); // spare place around code for marker plus buffer
		// whole bytes are inside body-array, these are assumed to be horizontally aligned
		// 
		if (pixel_per_block >=1)
		{
			// draw outside marker
			screenPatternRects = RectGenerator.generatePatternRects(horizontalCount, verticalCount, 4,
					w, h, pixel_per_block, pixel_per_block, 1);
			for( Rect patternrect: screenPatternRects) {
				canvas.drawRect(patternrect, ink);
			}
			screenPatternRects = RectGenerator.generatePatternRects(horizontalCount, verticalCount, 4,
					w, h, pixel_per_block, pixel_per_block, 0);
			for( Rect patternrect: screenPatternRects) {
				canvas.drawRect(patternrect, antiink);
			}
			
			RectGenerator idGen = RectGenerator.createRectGenerator();
			idGen.setWidthX(pixel_per_block);
			idGen.setHeightY(pixel_per_block);
			
			List<Rect> pixelizedChar;

         // draw key id = 32 halfbytes aka nibbles
         // top outer row
			idGen.setOffsetY(h/2 - (pixel_per_block*(verticalCount)*8)/2);
			for (int i=0;i<14;i++) {
             // Marker Space ID: M_ID
			    idGen.setOffsetX(w/2 - (pixel_per_block*(horizontalCount-4)*8)/2 + i*pixel_per_block*8); 
			    pixelizedChar = idGen.createRects(getBitsForNibble(getNibble(i, id)), 1);
			    for( Rect charpixel: pixelizedChar) { canvas.drawRect(charpixel, ink); }
			    idGen.setOffsetX(w/2 - (pixel_per_block*(horizontalCount-4)*8)/2 + i*pixel_per_block*8); 
			    pixelizedChar = idGen.createRects(getBitsForNibble(getNibble(i, id)), 0);
			    for( Rect charpixel: pixelizedChar) { canvas.drawRect(charpixel, indicator); }
			}

         // right outer column
			idGen.setOffsetX(w/2 + (pixel_per_block*(horizontalCount)*8)/2 - pixel_per_block*8); 
			for (int i=0;i<6;i++) {
			   idGen.setOffsetY(h/2 - (pixel_per_block*(verticalCount-4)*8)/2 + i*pixel_per_block*8);
			   pixelizedChar = idGen.createRects(getBitsForNibble(getNibble(14 + i, id)), 1);
			   for( Rect charpixel: pixelizedChar) { canvas.drawRect(charpixel, ink); }
				idGen.setOffsetY(h/2 - (pixel_per_block*(verticalCount-4)*8)/2 + i*pixel_per_block*8);
			   pixelizedChar = idGen.createRects(getBitsForNibble(getNibble(14 + i, id)), 0);
			   for( Rect charpixel: pixelizedChar) { canvas.drawRect(charpixel, indicator); }
			}

         // bottom outer row
			idGen.setOffsetY(h/2 + (pixel_per_block*(verticalCount)*8)/2 - pixel_per_block*8);
			for (int i=0;i<12;i++) {
             // Marker Space ID: M_ID
			    idGen.setOffsetX(w/2 + (pixel_per_block*(horizontalCount-4)*8)/2 - i*pixel_per_block*8 - pixel_per_block*8); 
			    pixelizedChar = idGen.createRects(getBitsForNibble(getNibble(20 + i, id)), 1);
			    for( Rect charpixel: pixelizedChar) { canvas.drawRect(charpixel, ink); }
			    idGen.setOffsetX(w/2 + (pixel_per_block*(horizontalCount-4)*8)/2 - i*pixel_per_block*8 - pixel_per_block*8); 
			    pixelizedChar = idGen.createRects(getBitsForNibble(getNibble(20 + i, id)), 0);
			    for( Rect charpixel: pixelizedChar) { canvas.drawRect(charpixel, indicator); }
			}

         // left outer column simple fill up the empty space on left border
			idGen.setOffsetX(w/2 - (pixel_per_block*(horizontalCount)*8)/2); 
			for (int i=0;i<6;i++) {
				idGen.setOffsetY(h/2 - (pixel_per_block*(verticalCount-4)*8)/2 + i*pixel_per_block*8);
			    pixelizedChar = idGen.createRects(getBitsForNibble(i+1), 1);
			    for( Rect charpixel: pixelizedChar) { canvas.drawRect(charpixel, ink); }
				idGen.setOffsetY(h/2 - (pixel_per_block*(verticalCount-4)*8)/2 + i*pixel_per_block*8);
			    pixelizedChar = idGen.createRects(getBitsForNibble(i+1), 0);
			    for( Rect charpixel: pixelizedChar) { canvas.drawRect(charpixel, indicator); }
			}

         // draw key body = 128 halfbytes aka nibbles
			// count from 0 to (16*8)%16
			int X_leftUpper = w/2 - (pixel_per_block*(horizontalCount-2)*8)/2;
			int Y_leftUpper = h/2 - (pixel_per_block*(verticalCount-2)*8)/2;
			for (int i=0; i<128; i++) {
				idGen.setOffsetX(X_leftUpper + (i%16)*pixel_per_block*8); 
            idGen.setOffsetY(Y_leftUpper + (i/16)*pixel_per_block*8);
			   pixelizedChar = idGen.createRects(getBitsForNibble(getNibble(i, body)), 1);
			   for( Rect charpixel: pixelizedChar) { 
               canvas.drawRect(charpixel, ink); 
            }
			}
			
			
			// for (int line=0; line<verticalCount*8; line++) {
			// 	for (int lineElement=0; lineElement<horizontalCount*8; lineElement++) {
			// 		int bitpos = lineElement % 8;
			// 		int currentoffset = lineElement / 8;
			// 		byte test = (byte)(1<<bitpos);
			// 		byte currentBit = (byte) (body[horizontalCount*line+currentoffset] & test); 
			// 		if ( currentBit == test ) {
			// 			
			// 			r.left = offset_x + lineElement*pixel_per_block;
			// 			r.top = offset_y + line*pixel_per_block;
			// 			r.right = offset_x + lineElement*pixel_per_block + pixel_per_block;
			// 			r.bottom = offset_y + line*pixel_per_block + pixel_per_block;
			// 			canvas.drawRect(r, ink);			
			// 		}
			// 		
			// 	}
			// }
						
			
		} else {
			Log.i("CharView", "data initialization error");
		}
		

	}
}
