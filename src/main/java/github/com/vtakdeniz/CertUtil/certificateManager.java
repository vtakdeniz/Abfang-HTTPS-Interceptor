package github.com.vtakdeniz.CertUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

//org.bouncycastle:bcmail-jdk14:1.51
public class certificateManager {
    private static certGenerator generator = new certGenerator();

    public static void generateCertificate(){
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            char[] pwdArray = "password".toCharArray();
            ks.load(null, pwdArray);
            try (FileOutputStream fos = new FileOutputStream("newKeyStoreFileName.keystore")) {
                ks.store(fos, pwdArray);
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        GeneratedCert rootCA = null;
        try {
            rootCA = generator.createCertificate("interceptor",   /*domain=*/null,     /*issuer=*/null,  /*isCa=*/true);

            GeneratedCert domain = generator.createCertificate("www.youtube.com","www.youtube.com", rootCA,/*isCa=*/false);

            generator.writeCertToFileBase64Encoded(rootCA.certificate,"root.crt");
            //writePrivateKey(rootCA.privateKey,"root.key");

            generator.writeCertToFileBase64Encoded(domain.certificate,"www.youtube.com.crt");
            //writePrivateKey(issuer.privateKey,"www.youtube.com.key");
            KeyPair cert = new KeyPair(domain.certificate.getPublicKey(),domain.privateKey);

            generator.exportKeyPairToKeystoreFile(cert,rootCA.certificate ,domain.certificate, "testxx","password.","www.youtube.com");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
