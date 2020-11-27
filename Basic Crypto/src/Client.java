//Author: Zhanghao Wen

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Client {
	public static void main(String[] args) throws Exception {
		// generate client RSA public and private keys
		AsymmetricKeyProducer myKeyProducer = new AsymmetricKeyProducer("trash", "trash");

		// GENERATE 128 BITS KEY
		SecureRandom random = new SecureRandom();
		byte symmetricKey[] = new byte[16]; // 128 bits are converted to 16 bytes;
		random.nextBytes(symmetricKey);
		System.out.println("Symmetric key is: " + Arrays.toString(symmetricKey));

		// read public key of server
		PublicKey serverPublickey = myKeyProducer.PublicKeyReader("serverPublicKey");
		PrivateKey clientPrivateKey = myKeyProducer.PrivateKeyReader("clientPrivateKey");

		// buld connection to the server
		Socket serverSocket = new Socket("localhost", 6666);

		// Encrypt symmetric key with server's public key
		byte symAfterEncrypt[] = null;
		symAfterEncrypt = encrypt(serverPublickey, symmetricKey);
		System.out.println("Cipher text of symmetric key is (byte array): " + Arrays.toString(symAfterEncrypt));

//		// Hash the key    // DONT NEED TO DO BECAUSE DIGITAL SIGNATURE DOES IT FOR US
//		byte[] hashSymKey = new byte[20];
//		MessageDigest md = MessageDigest.getInstance("SHA-256");
//		hashSymKey = md.digest(symmetricKey);
//		System.out.println("Hash value: " + Arrays.toString(hashSymKey));

		// Create a digital signature
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initSign(clientPrivateKey);
		sign.update(symmetricKey);// Adding data to the signature
		byte[] signature = sign.sign(); // Calculating the signature
		System.out.println("Digital signature is (byte arrray): " + Arrays.toString(signature));

		DataInputStream in = new DataInputStream(serverSocket.getInputStream());
		DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
		// below put message send to the server

		// send {k}es
		out.write(symAfterEncrypt);
//		 send {H(k)}dc
		out.write(signature);

		// receive ciphertext from server
		byte[] received = new byte[64];
		in.readFully(received);

		byte plainText[] = decryptText(received, new SecretKeySpec(symmetricKey, "AES"));
		System.out.println("Plain text after decryption is: " + new String(plainText));
	}

	public static byte[] encrypt(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(plaintext);
	}

	public static byte[] decryptText(byte[] cipherText, SecretKeySpec key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		byte[] IV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivSpec = new IvParameterSpec(IV);
		cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		return cipher.doFinal(cipherText);
	}

}
