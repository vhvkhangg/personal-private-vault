package com.vhvkhangg.personalprivatevault.vault.internal.application.entry;

import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Owner-local codec for strict bounded versioned vault trash pagination cursors.
 * Canonical representation: unpadded Base64url UTF-8 of "v1|<positive-id>".
 */
final class VaultTrashCursor {

    private static final String VERSION_PREFIX = "v1|";
    private static final int MAX_ENCODED_LENGTH = 128;

    private VaultTrashCursor() {}

    static String encode(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid cursor id");
        }
        byte[] bytes = (VERSION_PREFIX + id).getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static Long decode(String rawCursor) {
        if (rawCursor == null) {
            return null;
        }
        if (rawCursor.isBlank() || rawCursor.length() > MAX_ENCODED_LENGTH || rawCursor.contains("=")) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getUrlDecoder().decode(rawCursor);
        } catch (IllegalArgumentException e) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        String decodedStr;
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(decodedBytes));
            decodedStr = charBuffer.toString();
        } catch (CharacterCodingException e) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        int pipeIndex = decodedStr.indexOf('|');
        if (pipeIndex <= 0 || pipeIndex != decodedStr.lastIndexOf('|') || pipeIndex == decodedStr.length() - 1) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        String version = decodedStr.substring(0, pipeIndex);
        if (!"v1".equals(version)) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        String idStr = decodedStr.substring(pipeIndex + 1);
        if (!idStr.matches("^[1-9][0-9]{0,18}$")) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        if (id <= 0) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        String canonical = encode(id);
        if (!canonical.equals(rawCursor)) {
            throw new VaultEntryOperations.InvalidTrashInputException("Invalid cursor");
        }

        return id;
    }
}
