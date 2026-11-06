package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCredentialCrypto;
import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriveCredentialCryptoTest {

    private final byte[] keyBytes = new byte[32];
    private final DriveCredentialCrypto crypto;

    DriveCredentialCryptoTest() {
        new SecureRandom().nextBytes(keyBytes);
        this.crypto = new DriveCredentialCrypto(keyBytes);
    }

    @Test
    @DisplayName("Encrypt and decrypt roundtrip restores original refresh token plaintext")
    void encryptAndDecryptRoundtripSuccess() {
        short slot = 1;
        String googleSub = "sub-12345";
        UUID epoch = UUID.randomUUID();
        String plainToken = "1//04AbCdEfGhIjKlMnOpQrStUvWxYz-secret-token";

        byte[] encrypted = crypto.encrypt(slot, googleSub, epoch, plainToken);

        assertThat(encrypted).isNotNull();
        assertThat(encrypted.length).isGreaterThanOrEqualTo(DriveCredentialCrypto.MINIMUM_ENVELOPE_LENGTH);
        assertThat(encrypted[0]).isEqualTo(DriveCredentialCrypto.ENVELOPE_VERSION);

        String decrypted = crypto.decrypt(slot, googleSub, epoch, encrypted);
        assertThat(decrypted).isEqualTo(plainToken);
    }

    @Test
    @DisplayName("Decryption fails when AAD parameters (slot, sub, epoch) do not match")
    void decryptionFailsWhenAadMismatched() {
        short slot = 1;
        String googleSub = "sub-12345";
        UUID epoch = UUID.randomUUID();
        String plainToken = "my-secret-token";

        byte[] encrypted = crypto.encrypt(slot, googleSub, epoch, plainToken);

        // Wrong slot
        assertThatThrownBy(() -> crypto.decrypt((short) 2, googleSub, epoch, encrypted))
                .isInstanceOf(DriveCryptoException.class)
                .hasMessageContaining("Decryption failed");

        // Wrong subject
        assertThatThrownBy(() -> crypto.decrypt(slot, "sub-other", epoch, encrypted))
                .isInstanceOf(DriveCryptoException.class)
                .hasMessageContaining("Decryption failed");

        // Wrong epoch
        assertThatThrownBy(() -> crypto.decrypt(slot, googleSub, UUID.randomUUID(), encrypted))
                .isInstanceOf(DriveCryptoException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    @DisplayName("Decryption fails when ciphertext, IV, or tag is tampered")
    void decryptionFailsWhenCiphertextTampered() {
        short slot = 1;
        String googleSub = "sub-12345";
        UUID epoch = UUID.randomUUID();
        String plainToken = "my-secret-token";

        byte[] encrypted = crypto.encrypt(slot, googleSub, epoch, plainToken);

        // Tamper ciphertext
        byte[] tamperedCiphertext = encrypted.clone();
        tamperedCiphertext[tamperedCiphertext.length - 1] ^= 0x01;
        assertThatThrownBy(() -> crypto.decrypt(slot, googleSub, epoch, tamperedCiphertext))
                .isInstanceOf(DriveCryptoException.class);

        // Tamper IV
        byte[] tamperedIv = encrypted.clone();
        tamperedIv[2] ^= 0x01;
        assertThatThrownBy(() -> crypto.decrypt(slot, googleSub, epoch, tamperedIv))
                .isInstanceOf(DriveCryptoException.class);

        // Unsupported version
        byte[] wrongVersion = encrypted.clone();
        wrongVersion[0] = 0x02;
        assertThatThrownBy(() -> crypto.decrypt(slot, googleSub, epoch, wrongVersion))
                .isInstanceOf(DriveCryptoException.class)
                .hasMessageContaining("Unsupported envelope version");
    }

    @Test
    @DisplayName("Decryption fails when using a different AES-256 key")
    void decryptionFailsWithDifferentKey() {
        byte[] otherKey = new byte[32];
        new SecureRandom().nextBytes(otherKey);
        DriveCredentialCrypto otherCrypto = new DriveCredentialCrypto(otherKey);

        short slot = 1;
        String googleSub = "sub-12345";
        UUID epoch = UUID.randomUUID();
        String plainToken = "my-secret-token";

        byte[] encrypted = crypto.encrypt(slot, googleSub, epoch, plainToken);

        assertThatThrownBy(() -> otherCrypto.decrypt(slot, googleSub, epoch, encrypted))
                .isInstanceOf(DriveCryptoException.class);
    }

    @Test
    @DisplayName("Constructor rejects key that is not exactly 32 bytes")
    void rejectsInvalidKeyLength() {
        assertThatThrownBy(() -> new DriveCredentialCrypto(new byte[16]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DriveCredentialCrypto(new byte[64]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DriveCredentialCrypto(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
