package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/** Conjunctive provider identity, root parent and app-private creation provenance. */
public final class DriveFileProof {
    private DriveFileProof() {}
    public record FileItem(String id,String name,String mimeType,long sizeBytes,Instant modifiedAt) {
        @Override public String toString(){return "DriveFileItem[REDACTED]";}
    }
    public static void root(JsonNode node,DriveFolderRoot root,String accountRoot) {
        try {
            common(node,root.getRootFileId(),accountRoot,root.getRootBindingId(),"root");
            if(!"application/vnd.google-apps.folder".equals(text(node,"mimeType",127))) throw unavailable();
        } catch(DriveFileException e) { throw new DriveFileException("DRIVE_ROOT_UNAVAILABLE",409); }
    }
    public static FileItem file(JsonNode node,DriveFolderRoot root,String requested,DriveUploadIntent intent) {
        common(node,requested,root.getRootFileId(),root.getRootBindingId(),"file");
        String id=text(node,"id",255),name=DriveFileInputs.name(text(node,"name",1024)),mime=text(node,"mimeType",127);
        if(mime.startsWith("application/vnd.google-apps.")) throw new DriveFileException("DRIVE_FILE_NOT_BINARY",415);
        if(!DriveFileInputs.mime(mime).equals(mime)) throw unavailable();
        String rawSize=text(node,"size",20);
        if(!rawSize.matches("0|[1-9][0-9]{0,18}")) throw unavailable();
        long size;
        Instant modified;
        try { size=Long.parseLong(rawSize); modified=Instant.parse(text(node,"modifiedTime",40)); }
        catch(RuntimeException e) { throw unavailable(); }
        if(size>DriveFileResources.MAX_FILE) throw unavailable();
        String action=text(node.path("appProperties"),"ppvUploadAction",64);
        if(!action.matches("[0-9a-f]{64}")) throw unavailable();
        if(intent!=null && (!intent.getProviderFileId().equals(id) || !intent.getSafeName().equals(name)
                || !intent.getMimeType().equals(mime) || intent.getSizeBytes()!=size
                || !DriveFileInputs.action(intent.getId().getIdempotencyKey()).equals(action)
                || !intent.getContentMd5().equals(text(node,"md5Checksum",32)))) throw unavailable();
        return new FileItem(id,name,mime,size,modified);
    }
    private static void common(JsonNode node,String requested,String parent,UUID binding,String kind) {
        if(node==null || !node.isObject() || !Objects.equals(requested,text(node,"id",255))) throw unavailable();
        DriveFileInputs.id(requested);
        var trashed=node.get("trashed"); var parents=node.get("parents"); var props=node.get("appProperties");
        if(trashed==null || !trashed.isBoolean() || trashed.booleanValue() || parents==null || !parents.isArray()
                || parents.size()!=1 || !parents.get(0).isTextual() || !parent.equals(parents.get(0).textValue())
                || props==null || !props.isObject() || props.size()>16) throw unavailable();
        props.properties().forEach(p -> {
            if(!p.getValue().isTextual() || (p.getKey()+p.getValue().textValue()).getBytes(StandardCharsets.UTF_8).length>124) throw unavailable();
        });
        if(!kind.equals(text(props,"ppvKind",4)) || !binding.toString().equals(text(props,"ppvRootBinding",36))
                || !requested.equals(text(props,"ppvCreatedId",112))) throw unavailable();
    }
    public static String text(JsonNode node,String key,int max) {
        var value=node.get(key);
        if(value==null || !value.isTextual() || value.textValue().isBlank() || value.textValue().length()>max) throw unavailable();
        return value.textValue();
    }
    public static DriveFileException unavailable() { return new DriveFileException("DRIVE_FILE_UNAVAILABLE",404); }
}
