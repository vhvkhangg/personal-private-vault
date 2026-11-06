package com.vhvkhangg.personalprivatevault.drive.internal.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;

public class DriveCredentialCrypto {

    public static final byte ENVELOPE_VERSION = 0x01;
    public static final int GCM_IV_LENGTH_BYTES = 12;
    public static final int GCM_TAG_LENGTH_BITS = 128;
    public static final int GCM_TAG_LENGTH_BYTES = 16;
    public static final int MINIMUM_ENVELOPE_LENGTH = 1 + GCM_IV_LENGTH_BYTES + GCM_TAG_LENGTH_BYTES;
    public static final String KEY_ID = "v1";

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public DriveCredentialCrypto(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length != 32) {
            throw new IllegalArgumentException("Drive token key must be exactly 32 bytes for AES-256");
        }
        this.secretKey = new SecretKeySpec(Arrays.copyOf(keyBytes, keyBytes.length), "AES");
        this.secureRandom = new SecureRandom();
    }

    public String getKeyId() {
        return KEY_ID;
    }

    public byte[] encrypt(short slot, String googleSub, UUID credentialEpoch, String plainRefreshToken) {
        if (plainRefreshToken == null || plainRefreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be null or blank");
        }
        if (googleSub == null || googleSub.isBlank() || credentialEpoch == null) {
            throw new IllegalArgumentException("googleSub and credentialEpoch are required for AAD computation");
        }

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        byte[] plainBytes = plainRefreshToken.getBytes(StandardCharsets.UTF_8);
        byte[] aad = computeAad(slot, googleSub, credentialEpoch);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            cipher.updateAAD(aad);
            byte[] ciphertextWithTag = cipher.doFinal(plainBytes);

            ByteBuffer buffer = ByteBuffer.allocate(1 + GCM_IV_LENGTH_BYTES + ciphertextWithTag.length);
            buffer.put(ENVELOPE_VERSION);
            buffer.put(iv);
            buffer.put(ciphertextWithTag);
            return buffer.array();
        } catch (Exception e) {
            throw new DriveCryptoException("Failed to encrypt token credential", e);
        }
    }

    public String decrypt(short slot, String googleSub, UUID credentialEpoch, byte[] encryptedPayload) {
        if (encryptedPayload == null || encryptedPayload.length < MINIMUM_ENVELOPE_LENGTH) {
            throw new DriveCryptoException("Invalid encrypted payload: payload too short or null");
        }
        if (googleSub == null || googleSub.isBlank() || credentialEpoch == null) {
            throw new IllegalArgumentException("googleSub and credentialEpoch are required for AAD computation");
        }

        byte version = encryptedPayload[0];
        if (version != ENVELOPE_VERSION) {
            throw new DriveCryptoException("Unsupported envelope version: " + version);
        }

        byte[] iv = Arrays.copyOfRange(encryptedPayload, 1, 1 + GCM_IV_LENGTH_BYTES);
        byte[] ciphertextWithTag = Arrays.copyOfRange(encryptedPayload, 1 + GCM_IV_LENGTH_BYTES, encryptedPayload.length);
        byte[] aad = computeAad(slot, googleSub, credentialEpoch);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            cipher.updateAAD(aad);
            byte[] plainBytes = cipher.doFinal(ciphertextWithTag);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DriveCryptoException("Decryption failed: authentication tag mismatch or corrupted ciphertext", e);
        }
    }

    private byte[] computeAad(short slot, String googleSub, UUID credentialEpoch) {
        String aadString = slot + ":" + googleSub + ":" + credentialEpoch;
        return aadString.getBytes(StandardCharsets.UTF_8);
    }
}
