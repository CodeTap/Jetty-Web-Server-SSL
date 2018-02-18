package jetty_demo.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

@SuppressWarnings("restriction")
public class JettyServerSecurity 
{
	private static String WEBSERVER_CERT_NAME = "SERVER_CERT";
	
	public JettyServerSecurity()
	{}
	
	public KeyStore generateCertAndServerKeyStore()
	{
		File file = new File(WEBSERVER_CERT_NAME);
		FileOutputStream fos = null;
		KeyStore keyStore = null;
		KeyPair pair = null;
		
		try 
		{
			// For the sake of speed here lets see if we already have a file.
			if (file.exists())
			{
				System.out.println("Webserver key pair already exists. Loading key pair.");
				pair = LoadKeyPair("","RSA");			
			}
			// If we don't already have the file then we need to create it
			else
			{
				System.out.println("Webserver key pair does not exists. Generating key pair.");
				fos = new FileOutputStream(WEBSERVER_CERT_NAME);
		        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		        keyPairGenerator.initialize(4096, SecureRandom.getInstance("SHA1PRNG"));
		        pair = keyPairGenerator.generateKeyPair();
		        
			}
			
	        Certificate[] chain = {generateCertificate("cn=Unknown", pair, 365, "SHA256withRSA")};
	        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        keyStore.load(null, null);
	        keyStore.setKeyEntry("main", pair.getPrivate(), "654321".toCharArray(), chain);
	        
	        // If the file didn't exist we have to store it
	        File key = new File("public.key");
	        if (!key.exists())
	        {
	        	keyStore.store(fos, "123456".toCharArray());
	        	saveKeyPair("",pair);
	        }
	        
	        return keyStore;
    	} 
    	catch (FileNotFoundException ex)
    	{
    		System.out.println("Error generating web server key.");
    		System.out.println(ex);
		} 
    	catch (IOException ex) 
    	{
    		System.out.println("Error generating web server key.");
    		System.out.println(ex);
		} 
    	catch (InvalidKeyException ex) 
    	{
    		System.out.println("Error generating web server key. Invalid key");
    		System.out.println(ex);
		} 
    	catch (CertificateException ex) 
    	{
    		System.out.println("Error generating web server key. Certificate Error");
    		System.out.println(ex);
		}
    	catch (NoSuchAlgorithmException ex) 
    	{
    		System.out.println("Error generating web server key. Algorithm Error");
    		System.out.println(ex);
		} 
    	catch (NoSuchProviderException ex) 
    	{
    		System.out.println("Error generating web server key.");
    		System.out.println(ex);
		} 
    	catch (SignatureException ex) 
    	{
    		System.out.println("Error generating web server key. Signature Error");
    		System.out.println(ex);
		} 
    	catch (KeyStoreException ex) 
    	{
    		System.out.println("Error generating web server key. Keystore Error");
    		System.out.println(ex);
		} 
		catch (InvalidKeySpecException ex) 
		{
			System.out.println("Error generating web server key. Invalid Key Spec Error");
			System.out.println(ex);
		}
		return keyStore;
	}
	
	private X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm) throws CertificateException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException
	{
		PrivateKey privkey = pair.getPrivate();
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000l);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(dn);

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, owner);
        info.set(X509CertInfo.ISSUER, owner);
        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);

        // Update the algorith, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);
        return cert;
	}
	
	public void saveKeyPair(String path, KeyPair keyPair) throws IOException 
	{
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();
 
		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				publicKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(path + "public.key");
		fos.write(x509EncodedKeySpec.getEncoded());
		fos.close();
 
		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
		fos = new FileOutputStream(path + "private.key");
		fos.write(pkcs8EncodedKeySpec.getEncoded());
		fos.close();
	}
 
	public KeyPair LoadKeyPair(String path, String algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException 
	{
		// Read Public Key.
		File filePublicKey = new File(path + "public.key");
		FileInputStream fis = new FileInputStream(path + "public.key");
		byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
		fis.read(encodedPublicKey);
		fis.close();
 
		// Read Private Key.
		File filePrivateKey = new File(path + "private.key");
		fis = new FileInputStream(path + "private.key");
		byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
		fis.read(encodedPrivateKey);
		fis.close();
 
		// Generate KeyPair.
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
 
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
 
		return new KeyPair(publicKey, privateKey);
	}
}
