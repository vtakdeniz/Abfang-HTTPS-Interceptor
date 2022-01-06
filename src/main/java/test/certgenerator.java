package main.java.test;

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
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

class GeneratedCert {
    public final PrivateKey privateKey;
    public final X509Certificate certificate;

    public GeneratedCert(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }
}

public class certgenerator{

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

        return new GeneratedCert(certKeyPair.getPrivate(), cert);
    }

    public static void main(String[] args) {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            char[] pwdArray = "password".toCharArray();
            ks.load(null, pwdArray);
            try (FileOutputStream fos = new FileOutputStream("newKeyStoreFileName.keystore")) {
                ks.store(fos, pwdArray);
            }
        } catch (IOException |CertificateException |NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        GeneratedCert rootCA = null;
        try {
            rootCA = createCertificate("interceptor",   /*domain=*/null,     /*issuer=*/null,  /*isCa=*/true);
            //GeneratedCert issuer = createCertificate("www.youtube.com", /*domain=*/null,     rootCA,           /*isCa=*/true);
            GeneratedCert domain = createCertificate("www.youtube.com",              "www.youtube.com", rootCA,           /*isCa=*/false);
           // GeneratedCert otherD = createCertificate("other.gamlor.info",              "other.gamlor.info", issuer,           /*isCa=*/false);

            writeCertToFileBase64Encoded(rootCA.certificate,"root.crt");
            //writePrivateKey(rootCA.privateKey,"root.key");

            writeCertToFileBase64Encoded(domain.certificate,"www.youtube.com.crt");
            //writePrivateKey(issuer.privateKey,"www.youtube.com.key");
            KeyPair cert = new KeyPair(domain.certificate.getPublicKey(),domain.privateKey);

            exportKeyPairToKeystoreFile(cert,rootCA.certificate ,domain.certificate, "testxx","password.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected static void exportKeyPairToKeystoreFile(KeyPair keyPair,Certificate caCert, Certificate certificate, String alias, String storePass) throws Exception {
        char[] pwdArray = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("newKeyStoreFileName.keystore"), pwdArray);
        Certificate[] chain = new Certificate[2];
        chain[0] = certificate;
        chain[1] = caCert;
        //KeyStore sslKeyStore = KeyStore.getInstance("JKS");
        //sslKeyStore.load(null, null);
        ks.setKeyEntry(alias, keyPair.getPrivate(),pwdArray, chain);
        OutputStream writeStream = new FileOutputStream("newKeyStoreFileName.keystore");
        ks.store(writeStream, "password".toCharArray());
        writeStream.close();
        //FileOutputStream keyStoreOs = new FileOutputStream(fileName);
        //ks.store(keyStoreOs, storePass.toCharArray());

        KeyStore ks2 = KeyStore.getInstance("JKS");
        InputStream readStream = new FileInputStream("newKeyStoreFileName.keystore");
        ks2.load(readStream, "password".toCharArray());
        Key key = ks2.getKey("testxx", "password".toCharArray());
        readStream.close();
        System.out.println(Base64.encode(key.getEncoded()));
    }

    protected static void writeCertToFileBase64Encoded(Certificate certificate, String fileName) throws Exception {
        FileOutputStream certificateOut = new FileOutputStream(fileName);
        certificateOut.write("-----BEGIN CERTIFICATE-----\n".getBytes());
        certificateOut.write(Base64.encode(certificate.getEncoded()));
        certificateOut.write("\n-----END CERTIFICATE-----\n".getBytes());
        certificateOut.close();
    }

    /*protected static void writePrivateKey(PrivateKey privateKey, String fileName) throws Exception {
        FileOutputStream certificateOut = new FileOutputStream(fileName);

        byte[] key = Base64.encode(privateKey.getEncoded());
        System.out.println(key.length);
        certificateOut.write("-----BEGIN PRIVATE KEY-----\n".getBytes());

        for(int i=0;i<25;i++){
            System.out.println(i*64+" : "+(i*64+64));
            certificateOut.write(Arrays.copyOfRange(key,i*64,i*64+64));
            certificateOut.write("\n".getBytes());
        }
        certificateOut.write(Arrays.copyOfRange(key,1600,1624));
        certificateOut.write("\n".getBytes());
        certificateOut.write("-----END PRIVATE KEY-----\n".getBytes());
        certificateOut.close();
    }*/


}

