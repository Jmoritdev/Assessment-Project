package eu.iunxi.apps.assessment.util;

import eu.iunxi.apps.assessment.model.QuestionImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;

/**
 *
 * @author joey
 */
public class Picture implements StreamResponse {

    private QuestionImage image;

    public Picture(QuestionImage image) {
        this.image = image;
    }
    
    @Override
    public String getContentType() {
        return image.getContentType();
    }

    @Override
    public InputStream getStream() throws IOException {
        return new ByteArrayInputStream(image.getImageBytes());
    }

    @Override
    public void prepareResponse(Response response) {
        response.setHeader("Cache-Control", "private");
        response.setHeader("Pragma", "private");
        response.setDateHeader("Expires", System.currentTimeMillis() + 604800000L); // one week
        response.setDateHeader("Max-Age", System.currentTimeMillis() + 604800000L);
    }
    


}
