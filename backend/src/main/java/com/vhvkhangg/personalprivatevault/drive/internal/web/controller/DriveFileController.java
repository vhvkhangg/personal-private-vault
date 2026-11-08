package com.vhvkhangg.personalprivatevault.drive.internal.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.web.DriveFileFilter;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveFileDtos;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.context.request.async.WebAsyncTask;

@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="ppv.drive.files",name="enabled",havingValue="true")
@RequestMapping("/api/v1/drive/connections/{slot}")
@Tag(name="Drive files",description="Direct original binary files and child folders inside the verified PPV root")
@SecurityRequirement(name="bearerAuth")
public class DriveFileController implements AutoCloseable {
    private final DriveFileService service;
    private final DriveChildFolderService childFolderService;
    private final DriveItemSearchService searchService;
    private final DriveThumbnailService thumbnailService;
    private final DriveExplorerService explorerService;
    private final ThreadPoolExecutor workers=new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2),r -> {Thread t=new Thread(r,"ppv-drive-file-request");t.setDaemon(true);return t;});
    private final AsyncTaskExecutor executor=new TaskExecutorAdapter(workers);
    private final ObjectMapper mapper=new ObjectMapper().findAndRegisterModules()
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public DriveFileController(DriveFileService service) {
        this(service, null, null, null, null);
    }

    public DriveFileController(DriveFileService service,
                               DriveChildFolderService childFolderService,
                               DriveItemSearchService searchService,
                               DriveThumbnailService thumbnailService,
                               DriveExplorerService explorerService) {
        this.service = service;
        this.childFolderService = childFolderService;
        this.searchService = searchService;
        this.thumbnailService = thumbnailService;
        this.explorerService = explorerService;
    }

    @PostMapping("/root-folder")
    @Operation(operationId="ensureDriveRootFolder",summary="Ensure the PPV-owned account root")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Verified PPV root",
            content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                    schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.Root.class))))
    public WebAsyncTask<Void> ensure(@PathVariable("slot") String slot,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            if(request.getContentLengthLong()>0 || request.getHeader("Transfer-Encoding")!=null || request.getQueryString()!=null) throw DriveFileException.input();
            json(response,service.ensure(jwt.getSubject(),selected,op),op);
        });
    }

    @GetMapping("/files")
    @Operation(operationId="listDriveRootFiles",summary="List one direct-child binary page")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Permitted direct binary file page",
            content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                    schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.Page.class))))
    public WebAsyncTask<Void> list(@PathVariable("slot") String slot,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            for(var p:request.getParameterMap().entrySet()) if(!Set.of("limit","cursor").contains(p.getKey()) || p.getValue().length!=1) throw DriveFileException.input();
            int limit=25;
            try {if(request.getParameter("limit")!=null)limit=Integer.parseInt(request.getParameter("limit"));}
            catch(NumberFormatException e){throw new DriveFileException("DRIVE_INVALID_LIMIT",400);}
            json(response,service.list(jwt.getSubject(),selected,limit,request.getParameter("cursor"),op),op);
        });
    }

    @PostMapping("/files")
    @Operation(operationId="uploadDriveRootFile",summary="Upload one original binary file with a stable action key",
            requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(required=true,content=@Content(mediaType="multipart/form-data",
                    schema=@Schema(implementation=DriveFileDtos.Upload.class))),
            parameters=@Parameter(name="Idempotency-Key",required=true,in=io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER,
                    description="UTC yyyyMMddTHHmmssZ.lowercase-UUIDv4; reuse for every retry of this action"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201",description="New original file verified",
                    content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                            schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.File.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Same action reconciled and verified",
                    content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                            schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.File.class))))})
    public WebAsyncTask<Void> upload(@PathVariable("slot") String slot,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            if(request.getQueryString()!=null)throw DriveFileException.input();
            var keys=request.getHeaders("Idempotency-Key");String key=keys.hasMoreElements()?keys.nextElement():null;
            if(keys.hasMoreElements())throw new DriveFileException("UPLOAD_KEY_INVALID",400);
            DriveFileInputs.keyTime(key);
            var spool=DriveMultipartUpload.read(request.getInputStream(),(String)request.getAttribute(DriveFileFilter.CONTENT_TYPE),request.getContentLengthLong(),op);
            json(response,service.upload(jwt.getSubject(),selected,key,spool,op),op);
        });
    }

    @GetMapping("/files/{fileId}/download")
    @Operation(operationId="downloadDriveRootFile",summary="Download an authorized original binary attachment")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Original binary attachment",
            content=@Content(mediaType="application/octet-stream",schema=@Schema(type="string",format="binary")))
    public WebAsyncTask<Void> download(@PathVariable("slot") String slot,@PathVariable("fileId") String fileId,@AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            if(request.getHeader("Range")!=null)throw new DriveFileException("DRIVE_RANGE_NOT_SUPPORTED",400);
            if(request.getQueryString()!=null)throw DriveFileException.input();
            var result=service.download(jwt.getSubject(),selected,fileId,op);
            var media=service.media(result,op);
            service.verify(result,op);
            response.setStatus(200);response.setContentType("application/octet-stream");
            response.setHeader("Cache-Control","no-store");response.setHeader("X-Content-Type-Options","nosniff");
            response.setHeader("Connection","close");
            response.setHeader("Content-Disposition","attachment; filename=\"download.bin\"; filename*=UTF-8''"
                    +URLEncoder.encode(result.data().name(),StandardCharsets.UTF_8).replace("+","%20").replace("*","%2A"));
            response.setContentLengthLong(result.data().sizeBytes());
            byte[] buffer=new byte[DriveFileResources.BUFFER];long remaining=result.data().sizeBytes();
            var input=media.input();var output=response.getOutputStream();
            while(remaining>0) {
                service.verify(result,op);
                int target=(int)Math.min(remaining,buffer.length),count=0;
                while(count<target) {
                    op.check();
                    int read=input.read(buffer,count,target-count);
                    if(read<0)throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
                    if(read>0){count+=read;op.progress();}
                }
                remaining-=count;
                if(remaining==0 && input.read()!=-1)throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
                service.verify(result,op);output.write(buffer,0,count);
            }
            if(result.data().sizeBytes()==0 && input.read()!=-1)throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
            service.verify(result,op);output.flush();op.endPhase();
        });
    }

    @GetMapping("/storage-quota")
    @Operation(operationId="getDriveStorageQuota",summary="Get account storage quota")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Account storage quota projection",
            content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                    schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.StorageQuota.class))))
    public WebAsyncTask<Void> getStorageQuota(@PathVariable("slot") String slot,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            if(request.getContentLengthLong()>0 || request.getHeader("Transfer-Encoding")!=null || request.getQueryString()!=null) throw DriveFileException.input();
            if(explorerService==null) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
            json(response,explorerService.storageQuota(jwt.getSubject(),selected,op),op);
        });
    }

    @PostMapping("/folders")
    @Operation(operationId="createDriveChildFolder",summary="Create an app-owned first-level child folder",
            requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(required=true,content=@Content(mediaType="application/json",
                    schema=@Schema(implementation=DriveFileDtos.FolderCreateRequest.class))),
            parameters=@Parameter(name="Idempotency-Key",required=true,in=io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER,
                    description="UTC yyyyMMddTHHmmssZ.lowercase-UUIDv4; reuse for every retry of this action"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201",description="New child folder created",
                    content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                            schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.Folder.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Same folder action reconciled and verified",
                    content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                            schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.Folder.class))))})
    public WebAsyncTask<Void> createFolder(@PathVariable("slot") String slot,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            if(request.getQueryString()!=null) throw DriveFileException.input();
            var keys=request.getHeaders("Idempotency-Key");String key=keys.hasMoreElements()?keys.nextElement():null;
            if(keys.hasMoreElements()) throw new DriveFileException("FOLDER_KEY_INVALID",400);
            DriveFileInputs.folderKeyTime(key);
            long contentLength=request.getContentLengthLong();
            if(contentLength<=0 || contentLength>4096) throw DriveFileException.input();
            byte[] bodyBytes=request.getInputStream().readNBytes(4097);
            if(bodyBytes.length>4096) throw DriveFileException.input();
            JsonNode tree;
            try (com.fasterxml.jackson.core.JsonParser parser = mapper.createParser(bodyBytes)) {
                parser.configure(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
                tree = mapper.readTree(parser);
                if (parser.nextToken() != null) {
                    throw DriveFileException.input();
                }
            } catch (DriveFileException e) {
                throw e;
            } catch (Exception e) {
                throw DriveFileException.input();
            }
            if(tree==null || !tree.isObject()) throw DriveFileException.input();
            Iterator<String> fieldNames=tree.fieldNames();
            if(!fieldNames.hasNext()) throw DriveFileException.input();
            String firstName=fieldNames.next();
            if(!firstName.equals("name") || fieldNames.hasNext()) throw DriveFileException.input();
            JsonNode nameNode=tree.get("name");
            if(nameNode==null || !nameNode.isTextual()) throw DriveFileException.input();
            if(childFolderService==null) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
            json(response,childFolderService.create(jwt.getSubject(),selected,key,nameNode.textValue(),op),op);
        });
    }

    @GetMapping("/items")
    @Operation(operationId="listDriveItems",summary="List or search items in PPV account subtree")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Bounded items page",
            content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                    schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.ItemPage.class))))
    public WebAsyncTask<Void> listItems(@PathVariable("slot") String slot,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            Set<String> allowedParams=Set.of("scope","parentId","limit","cursor","name","type","from","to","timeZone");
            for(var p:request.getParameterMap().entrySet()) {
                if(!allowedParams.contains(p.getKey()) || p.getValue().length!=1) throw DriveFileException.input();
            }
            int limit=50;
            if(request.getParameter("limit")!=null) {
                try { limit=Integer.parseInt(request.getParameter("limit")); }
                catch(NumberFormatException e) { throw new DriveFileException("DRIVE_INVALID_LIMIT",400); }
            }
            if(searchService==null) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
            json(response,searchService.listItems(
                    jwt.getSubject(),selected,
                    request.getParameter("scope"),
                    request.getParameter("parentId"),
                    limit,
                    request.getParameter("cursor"),
                    request.getParameter("name"),
                    request.getParameter("type"),
                    request.getParameter("from"),
                    request.getParameter("to"),
                    request.getParameter("timeZone"),
                    op
            ),op);
        });
    }

    @PostMapping("/folders/{childId}/files")
    @Operation(operationId="uploadDriveChildFile",summary="Upload one original binary file under an authorized child folder",
            requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(required=true,content=@Content(mediaType="multipart/form-data",
                    schema=@Schema(implementation=DriveFileDtos.Upload.class))),
            parameters=@Parameter(name="Idempotency-Key",required=true,in=io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER,
                    description="UTC yyyyMMddTHHmmssZ.lowercase-UUIDv4; reuse for every retry of this action"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201",description="New original child file verified",
                    content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                            schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.File.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Same child file action reconciled and verified",
                    content=@Content(mediaType="application/json",schema=@Schema(allOf=ApiResponse.class),
                            schemaProperties=@SchemaProperty(name="data",schema=@Schema(implementation=DriveFileDtos.File.class))))})
    public WebAsyncTask<Void> uploadChildFile(@PathVariable("slot") String slot,@PathVariable("childId") String childId,@AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            if(request.getQueryString()!=null) throw DriveFileException.input();
            DriveFileInputs.id(childId);
            var keys=request.getHeaders("Idempotency-Key");String key=keys.hasMoreElements()?keys.nextElement():null;
            if(keys.hasMoreElements()) throw new DriveFileException("UPLOAD_KEY_INVALID",400);
            DriveFileInputs.keyTime(key);
            var spool=DriveMultipartUpload.read(request.getInputStream(),(String)request.getAttribute(DriveFileFilter.CONTENT_TYPE),request.getContentLengthLong(),op);
            json(response,service.uploadChild(jwt.getSubject(),selected,childId,key,spool,op),op);
        });
    }

    @GetMapping("/files/{fileId}/thumbnail")
    @Operation(operationId="downloadDriveFileThumbnail",summary="Download verified thumbnail image bytes for an authorized file")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="Thumbnail image bytes",
            content=@Content(mediaType="image/*",schema=@Schema(type="string",format="binary")))
    public WebAsyncTask<Void> downloadThumbnail(@PathVariable("slot") String slot,@PathVariable("fileId") String fileId,@AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request,HttpServletResponse response) throws IOException {
        return runAsync(request,response,() -> {
            var op=operation(request);DriveFileAuthority.requireOwner(jwt.getSubject());short selected=DriveFileInputs.slot(slot);
            if(request.getQueryString()!=null) throw DriveFileException.input();
            if(thumbnailService==null) throw new DriveFileException("THUMBNAIL_UNAVAILABLE",404);
            var result=thumbnailService.thumbnail(jwt.getSubject(),selected,fileId,op);
            var image=result.data();
            service.verify(result,op);
            response.setStatus(200);
            response.setContentType(image.contentType());
            response.setHeader("Cache-Control","private, no-store");
            response.setHeader("X-Content-Type-Options","nosniff");
            response.setHeader("Content-Security-Policy","default-src 'none'; style-src 'unsafe-inline'");
            response.setContentLength(image.bytes().length);
            response.getOutputStream().write(image.bytes());
            response.getOutputStream().flush();
            op.endPhase();
        });
    }

    private void json(HttpServletResponse response,DriveFileService.Result<?> result,DriveFileResources.Operation op) throws IOException {
        service.verify(result,op);response.setStatus(result.created()?201:200);response.setContentType("application/json");response.setHeader("Cache-Control","no-store");
        Object data=result.data();
        if(data instanceof DriveFileService.RootStatus root)data=new DriveFileDtos.Root(root.slot(),root.status());
        else if(data instanceof DriveFileService.FilePage page)data=new DriveFileDtos.Page(page.files().stream().map(this::fileDto).toList(),page.nextCursor(),page.hasMore());
        else if(data instanceof DriveFileProof.FileItem file)data=fileDto(file);
        else if(data instanceof DriveFileProof.FolderItem folder)data=new DriveFileDtos.Folder(folder.id(),folder.name(),folder.parentId(),"folder",folder.modifiedAt());
        else if(data instanceof DriveFileDtos.StorageQuota quota)data=quota;
        else if(data instanceof DriveFileDtos.ItemPage itemPage)data=itemPage;
        mapper.writeValue(response.getOutputStream(),ApiResponses.success(data));
    }

    private DriveFileDtos.File fileDto(DriveFileProof.FileItem file){return new DriveFileDtos.File(file.id(),file.name(),file.mimeType(),file.sizeBytes(),file.modifiedAt());}
    private DriveFileResources.Operation operation(HttpServletRequest request) {
        if(!(request.getAttribute(DriveFileFilter.OPERATION) instanceof DriveFileResources.Operation op))throw DriveFileException.capacity();
        return op;
    }
    private interface Work {void run() throws IOException;}
    private WebAsyncTask<Void> runAsync(HttpServletRequest request,HttpServletResponse response,Work work) {
        var op=operation(request);
        var task=new WebAsyncTask<Void>(op.remaining().toMillis(),executor,() -> {
            try{run(response,op,work);return null;}
            finally{response.getOutputStream().flush();}
        });
        task.onTimeout(() -> {op.cancel();run(response,op,() -> {throw DriveFileException.timeout();});return null;});
        return task;
    }
    @Override public void close(){workers.shutdownNow();}
    private void run(HttpServletResponse response,DriveFileResources.Operation op,Work work) throws IOException {
        try {work.run();}
        catch(Exception error) {
            if(response.isCommitted()){DriveFileFilter.abortTransfer(response);return;}
            var safe=op.isCancelled()?DriveFileException.timeout():error instanceof DriveException drive ? drive : new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
            response.reset();response.setStatus(safe.getHttpStatus());response.setContentType("application/json");response.setHeader("Cache-Control","no-store");
            mapper.writeValue(response.getOutputStream(),ApiResponses.error(safe.getErrorCode(),safe.getMessage()));
        }
    }
}
