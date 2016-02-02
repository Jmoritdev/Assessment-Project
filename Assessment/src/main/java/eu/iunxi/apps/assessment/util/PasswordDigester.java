package eu.iunxi.apps.assessment.util;

import java.security.MessageDigest;
import org.apache.commons.codec.binary.Hex;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author joey
 */
public class PasswordDigester {
    
    public static String getDigest(String plaintext) throws NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(plaintext.getBytes(Charset.forName("utf8")));
        
        return Hex.encodeHexString(crypt.digest());
    }
    
}
