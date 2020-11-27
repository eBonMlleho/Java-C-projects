//Author: Zhanghao Wen

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Server {
	public static void main(String[] args) throws Exception {
		// generate server/client RSA public and private keys
		AsymmetricKeyProducer serverKeyProducer = new AsymmetricKeyProducer("serverPublicKey", "serverPrivateKey");
		AsymmetricKeyProducer clientKeyProducer = new AsymmetricKeyProducer("clientPublicKey", "clientPrivateKey");

		// read public key of client
		PublicKey clientPublickey = clientKeyProducer.PublicKeyReader("clientPublicKey");
		PrivateKey severPrivateKey = serverKeyProducer.PrivateKeyReader("serverPrivateKey");
		// System.out.println(clientPublickey);

		// build TCP connection
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(6666); // provide MYSERVICE at port 6666
			// System.out.println(serverSocket);
		} catch (IOException e) {
			System.out.println("Could not listen on port: 6666");
			System.exit(-1);
		}

		// keep listenning our only 1 client
		while (true) {
			Socket clientSocket = null;
			DataInputStream inStream;
			DataOutputStream outStream;
			try {
				System.out.println("Waiting for connetion ......");
				// WAIT FOR CLIENT TO TRY TO CONNECT TO SERVER
				clientSocket = serverSocket.accept(); //
				System.out.println("Client is talking to server......");
				inStream = new DataInputStream(clientSocket.getInputStream());
				outStream = new DataOutputStream(clientSocket.getOutputStream());

				byte[] received = new byte[256];
				byte[] received2 = new byte[256];
				inStream.readFully(received);
				inStream.readFully(received2);
				// System.out.println("server got message:" + Arrays.toString(received));
				// decrypt by using own private key to get sym key
				byte symmetricKey[] = new byte[16];
				symmetricKey = decrypt(severPrivateKey, received);
				// System.out.println("this is key from client after decryption: " +
				// Arrays.toString(symmetricKey));

				// verify
				boolean verified = verify(clientPublickey, received2, symmetricKey);
				// System.out.println("digital signature: " + Arrays.toString(received2));

				if (verified) {
					System.out.println("Verify digital signature success! \nSymmetric Key in plaintext is (byte array):"
							+ Arrays.toString(symmetricKey));
				} else {
					System.out.println("error: digital signature does not match... break...");
					break;
				}
				// grab text file and encrypt
				File file = new File("text.txt");
				byte[] fileToByteArray = new byte[(int) file.length()];// init array with file length
				// System.out.println("file length is " + (int) file.length());
				FileInputStream fis = new FileInputStream(file);
				fis.read(fileToByteArray); // read file into bytes[]
				fis.close();

				// encrypt text //64bytes
				byte cipherText[] = encryptText(fileToByteArray, new SecretKeySpec(symmetricKey, "AES"));
				outStream.write(cipherText);
				System.out.println("Send ciphertext to the client successfully!");
				System.out.println("Ciphertext is: (byte array): " + Arrays.toString(cipherText));

				break;

			} catch (IOException e) {
				System.out.println("Accept failed: 6666");
				System.exit(-1);
			}
		}

	}

	public static byte[] decrypt(PrivateKey key, byte[] ciphertext) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(ciphertext);
	}

	public static boolean verify(PublicKey key, byte[] signature, byte[] data)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initVerify(key);
		sign.update(data);
		return sign.verify(signature);

	}

	public static byte[] encryptText(byte[] plaintext, SecretKeySpec key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		// Create IvParameterSpec
		byte[] IV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		IvParameterSpec ivSpec = new IvParameterSpec(IV);
		cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
		return cipher.doFinal(plaintext);
	}
}
