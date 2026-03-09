package folder_java;

import java.awt.image.BufferedImage;

public class SpriteUtils {
    public static BufferedImage makeTransparent(BufferedImage img) {
        if (img == null)
            return null;
        int bgColor = img.getRGB(0, 0); // top-left pixel as background
        BufferedImage transparentImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getRGB(x, y);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;

                int bgR = (bgColor >> 16) & 0xff;
                int bgG = (bgColor >> 8) & 0xff;
                int bgB = bgColor & 0xff;

                int rDiff = Math.abs(r - bgR);
                int gDiff = Math.abs(g - bgG);
                int bDiff = Math.abs(b - bgB);

                boolean isWhiteBg = (r > 180 && g > 180 && b > 180);
                boolean isTopLeftBg = (rDiff < 50 && gDiff < 50 && bDiff < 50);

                if (isWhiteBg || isTopLeftBg) {
                    transparentImg.setRGB(x, y, 0x00FFFFFF);
                } else {
                    transparentImg.setRGB(x, y, color);
                }
            }
        }
        return transparentImg;
    }
}
