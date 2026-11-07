package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.time.*;
import java.time.format.*;
import java.util.*;

public final class DriveFileInputs {
    private DriveFileInputs() {}
    public static short slot(String value) {
        if(!"1".equals(value) && !"2".equals(value)) throw new DriveFileException("DRIVE_INVALID_SLOT",400);
        return Short.parseShort(value);
    }
    public static String id(String value) {
        if(value==null || !value.matches("[A-Za-z0-9_-]{1,255}")) throw new DriveFileException("DRIVE_INVALID_FILE_ID",400);
        return value;
    }
    public static String generatedId(String value) {
        if(value==null || !value.matches("[A-Za-z0-9_-]{1,112}")) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
        return value;
    }
    public static String name(String value) {
        if(value==null) throw DriveFileException.input();
        String safe=Normalizer.normalize(value,Normalizer.Form.NFC).strip();
        if(safe.isEmpty() || safe.equals(".") || safe.equals("..") || safe.contains("/") || safe.contains("\\")
                || safe.codePoints().anyMatch(Character::isISOControl) || safe.getBytes(StandardCharsets.UTF_8).length>255) throw DriveFileException.input();
        return safe;
    }
    public static String mime(String value) {
        if(value!=null && value.toLowerCase(Locale.ROOT).startsWith("application/vnd.google-apps.")) throw new DriveFileException("DRIVE_FILE_NOT_BINARY",415);
        return value!=null && value.length()<=127 && value.matches("[a-z0-9!#$&^_.+-]+/[a-z0-9!#$&^_.+-]+") ? value : "application/octet-stream";
    }
    public static Instant keyTime(String key) {
        if(key==null || key.isBlank()) throw new DriveFileException("UPLOAD_KEY_REQUIRED",400);
        if(!key.matches("[0-9]{8}T[0-9]{6}Z\\.[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) throw new DriveFileException("UPLOAD_KEY_INVALID",400);
        try {
            return LocalDateTime.parse(key.substring(0,16),DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").withResolverStyle(ResolverStyle.STRICT)).toInstant(ZoneOffset.UTC);
        } catch(DateTimeException e) { throw new DriveFileException("UPLOAD_KEY_INVALID",400); }
    }
    public static void freshKey(String key, Instant now) {
        Instant time=keyTime(key);
        if(time.isBefore(now.minusSeconds(600)) || time.isAfter(now.plusSeconds(120))) throw new DriveFileException("UPLOAD_KEY_EXPIRED",409);
    }
    public static MessageDigest digest(String algorithm) {
        try { return MessageDigest.getInstance(algorithm); } catch(java.security.NoSuchAlgorithmException e) { throw new IllegalStateException("Required digest unavailable"); }
    }
    public static String action(String key) { return HexFormat.of().formatHex(digest("SHA-256").digest(key.getBytes(StandardCharsets.US_ASCII))); }
    public static String fingerprint(String name,String mime,long length,byte[] contentSha) {
        var hash=digest("SHA-256"); hash.update("GDI26-S2-UPLOAD-V1\0".getBytes(StandardCharsets.US_ASCII));
        for(String text:List.of(name,mime)) { byte[] bytes=text.getBytes(StandardCharsets.UTF_8); hash.update(ByteBuffer.allocate(4).putInt(bytes.length).array()); hash.update(bytes); }
        hash.update(ByteBuffer.allocate(8).putLong(length).array()); hash.update(contentSha);
        return HexFormat.of().formatHex(hash.digest());
    }
}
