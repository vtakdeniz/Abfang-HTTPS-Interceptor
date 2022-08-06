package github.com.vtakdeniz.CertUtil;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

class GeneratedCert implements Serializable{
    public final PrivateKey privateKey;
    public final X509Certificate certificate;

    public GeneratedCert(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

}
public class certGenerator {

    protected static X509Certificate readRootCert(String path){


        FileInputStream is = null;
        CertificateFactory certFactory=null;
        X509Certificate cer=null;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
            is = new FileInputStream(path);
            cer = (X509Certificate) certFactory.generateCertificate(is);
        } catch (FileNotFoundException | CertificateException e) {
            e.printStackTrace();
        }
        return cer;
    }



    protected static GeneratedCert createCertificate(String cnName, String domain, GeneratedCert issuer, boolean isCA) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair certKeyPair = keyGen.generateKeyPair();
        X500Name name = new X500Name("CN=" + cnName);
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Instant validFrom = Instant.now();
        Instant validUntil = validFrom.plus(10 * 360, ChronoUnit.DAYS);

        X500Name issuerName;
        PrivateKey issuerKey;
        if (issuer == null) {
            issuerName = name;
            issuerKey = certKeyPair.getPrivate();
        } else {
            issuerName = new X500Name(issuer.certificate.getSubjectDN().getName());
            issuerKey = issuer.privateKey;
        }

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                issuerName,
                serialNumber,
                Date.from(validFrom), Date.from(validUntil),
                name, certKeyPair.getPublic());
        if (isCA) {
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isCA));
        }
        if (domain != null) {
            builder.addExtension(Extension.subjectAlternativeName, false,
                    new GeneralNames(new GeneralName(GeneralName.dNSName, domain)));
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(issuerKey);
        X509CertificateHolder certHolder = builder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);
        GeneratedCert generatedCert = new GeneratedCert(certKeyPair.getPrivate(), cert);

        if(isCA){
            FileOutputStream fos = new FileOutputStream("rootCert.data");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(generatedCert);
        }
        return generatedCert;
    }

    public static void main(String[] args) {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            char[] pwdArray = "password".toCharArray();
            ks.load(null, pwdArray);
            try (FileOutputStream fos = new FileOutputStream("www.youtube.com"+".keystore")) {
                ks.store(fos, pwdArray);
            }
        } catch (IOException | CertificateException |
                NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        GeneratedCert rootCA = null;
        try {
            rootCA = createCertificate("interceptor",   /*domain=*/null,     /*issuer=*/null,  /*isCa=*/true);
            FileInputStream fos = new FileInputStream("rootCert.data");
            ObjectInputStream ois = new ObjectInputStream(fos);
            GeneratedCert rootCAFromfile=(GeneratedCert) ois.readObject();
            GeneratedCert domain = createCertificate("www.youtube.com",              "www.youtube.com", rootCAFromfile,           /*isCa=*/false);
            writeCertToFileBase64Encoded(rootCA.certificate,"root.crt");
            writeCertToFileBase64Encoded(domain.certificate,"www.youtube.com.crt");
            KeyPair cert = new KeyPair(domain.certificate.getPublicKey(),domain.privateKey);
            exportKeyPairToKeystoreFile(cert,rootCA.certificate ,domain.certificate, "testxx","password.","www.youtube.com");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected static void exportKeyPairToKeystoreFile(KeyPair keyPair,Certificate caCert, Certificate certificate, String alias, String storePass,String keyStoreName) throws Exception {
        char[] pwdArray = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyStoreName+".keystore"), pwdArray);
        Certificate[] chain = new Certificate[2];
        chain[0] = certificate;
        chain[1] = caCert;
        ks.setKeyEntry(alias, keyPair.getPrivate(),pwdArray, chain);
        OutputStream writeStream = new FileOutputStream(keyStoreName+".keystore");
        ks.store(writeStream, "password".toCharArray());
        writeStream.close();
        KeyStore ks2 = KeyStore.getInstance("JKS");
        InputStream readStream = new FileInputStream(keyStoreName+".keystore");
        ks2.load(readStream, "password".toCharArray());
        Key key = ks2.getKey("testxx", "password".toCharArray());
        readStream.close();
    }

    protected static void writeCertToFileBase64Encoded(Certificate certificate, String fileName) throws Exception {
        FileOutputStream certificateOut = new FileOutputStream(fileName);
        certificateOut.write("-----BEGIN CERTIFICATE-----\n".getBytes());
        certificateOut.write(Base64.encode(certificate.getEncoded()));
        certificateOut.write("\n-----END CERTIFICATE-----\n".getBytes());
        certificateOut.close();
    }
}

