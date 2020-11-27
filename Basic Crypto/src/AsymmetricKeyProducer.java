//Author: Zhanghao Wen

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class AsymmetricKeyProducer {
	PrivateKey prv;
	PublicKey pub;
	PrivateKey prv_recovered;
	PublicKey pub_recovered;

	public AsymmetricKeyProducer(String publicKeyPath, String privateKeyPath)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		// Generate KeyPair
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair keyPair = kpg.generateKeyPair();
		pub = keyPair.getPublic();
		prv = keyPair.getPrivate();

		byte[] pubBytes = pub.getEncoded();
		byte[] prvBytes = prv.getEncoded();

		// now save pubBytes or prvBytes
		// to recover the key
		KeyFactory kf = KeyFactory.getInstance("RSA");
		prv_recovered = kf.generatePrivate(new PKCS8EncodedKeySpec(prvBytes));
		pub_recovered = kf.generatePublic(new X509EncodedKeySpec(pubBytes));

		// Store Public Key to file based on parameters
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pubBytes);
		FileOutputStream fos = new FileOutputStream(publicKeyPath);
		fos.write(x509EncodedKeySpec.getEncoded());
		fos.close();

		// Store Private Key to file based on parameters
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(prvBytes);
		fos = new FileOutputStream(privateKeyPath);
		fos.write(pkcs8EncodedKeySpec.getEncoded());
		fos.close();
	}

	public PrivateKey PrivateKeyReader(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);

	}

	public PublicKey PublicKeyReader(String publicKeyPath)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		// Read Public Key from file
		byte[] keyBytes = Files.readAllBytes(Paths.get(publicKeyPath));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");

		return kf.generatePublic(spec);
	}

}
