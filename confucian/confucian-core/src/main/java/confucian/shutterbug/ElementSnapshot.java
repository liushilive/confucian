package confucian.shutterbug;

import confucian.exception.FrameworkException;
import confucian.shutterbug.utils.Coordinates;
import confucian.shutterbug.utils.ImageProcessor;

import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

public class ElementSnapshot extends Snapshot {

    ElementSnapshot() {
    }

    protected void setImage(BufferedImage image, Coordinates coords) {
        try {
            self().image = ImageProcessor.getElement(image, coords);
        } catch (RasterFormatException rfe) {
            throw new FrameworkException(ELEMENT_OUT_OF_VIEWPORT_EX_MESSAGE, rfe);
        }
    }

    @Override
    protected ElementSnapshot self() {
        return this;
    }
}
