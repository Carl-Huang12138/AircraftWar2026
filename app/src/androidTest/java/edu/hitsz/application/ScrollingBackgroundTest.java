package edu.hitsz.application;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/** 离屏像素检查，无需重现用户的实时抖动。 */
@RunWith(AndroidJUnit4.class)
public class ScrollingBackgroundTest {
    private Bitmap source() {
        Bitmap bitmap = Bitmap.createBitmap(3, 4, Bitmap.Config.ARGB_8888);
        int[] rows = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
        for (int y = 0; y < 4; y++) for (int x = 0; x < 3; x++) bitmap.setPixel(x, y, rows[y]);
        bitmap.setDensity(420);
        return bitmap;
    }

    private Bitmap render(ScrollingBackground background, int offset, int density) {
        Bitmap target = Bitmap.createBitmap(3, 4, Bitmap.Config.ARGB_8888);
        target.setDensity(density);
        background.draw(new Canvas(target), offset);
        return target;
    }

    @Test public void oppositeEdgesMeetWithoutAColorDiscontinuity() {
        ScrollingBackground background = new ScrollingBackground(source(), 3, 4);
        // 位移1时原图顶边经过第0/1行；位移5时底边经过同一位置。
        for (int offset : new int[]{1, 5}) {
            Bitmap frame = render(background, offset, 160);
            assertEquals("The two pixels beside the join must be identical",
                    frame.getPixel(1, 0), frame.getPixel(1, 1));
        }
    }

    @Test public void mirrorCycleWrapsAfterTwoHeightsAndKeepsRemainder() {
        ScrollingBackground background = new ScrollingBackground(source(), 3, 4);
        assertTrue(render(background, 0, 160).sameAs(render(background, 8, 160)));
        assertFalse("One height is the mirrored image, not the start frame",
                render(background, 0, 160).sameAs(render(background, 4, 160)));
        assertEquals(1, background.advance(7, 2));
        assertEquals(0, background.advance(6, 2));
    }

    @Test public void backgroundCoversEveryPixelRegardlessOfCanvasDensity() {
        ScrollingBackground background = new ScrollingBackground(source(), 3, 4);
        for (int offset = 0; offset < 8; offset++) {
            Bitmap low = render(background, offset, 160);
            Bitmap high = render(background, offset, 640);
            assertTrue("Screen pixel coordinates must ignore resource density", low.sameAs(high));
            for (int y = 0; y < 4; y++) for (int x = 0; x < 3; x++) {
                assertEquals(255, Color.alpha(low.getPixel(x, y)));
            }
        }
    }
}
