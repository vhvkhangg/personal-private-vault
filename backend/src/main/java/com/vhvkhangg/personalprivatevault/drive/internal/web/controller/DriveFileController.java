package com.vhvkhangg.personalprivatevault.drive.internal.web.controller;

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
@Tag(name="Drive files",description="Direct original binary files inside the verified PPV root")
@SecurityRequirement(name="bearerAuth")
public class DriveFileController implements AutoCloseable {
    private final DriveFileService service;
    private final ThreadPoolExecutor workers=new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2),r -> {Thread t=new Thread(r,"ppv-drive-file-request");t.setDaemon(true);return t;});
    private final AsyncTaskExecutor executor=new TaskExecutorAdapter(workers);
    private final ObjectMapper mapper=new ObjectMapper().findAndRegisterModules()
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    public DriveFileController(DriveFileService service){this.service=service;}

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
            // The approved localhost HTTP/1.1 server closes this attachment response at completion.
            // Underlength failures therefore terminate promptly instead of waiting on keep-alive.
            response.setHeader("Connection","close");
            response.setHeader("Content-Disposition","attachment; filename=\"download.bin\"; filename*=UTF-8''"
                    +URLEncoder.encode(result.data().name(),StandardCharsets.UTF_8).replace("+","%20").replace("*","%2A"));
            // The verified expected length makes committed failures observably incomplete, never a normal chunk terminator.
            response.setContentLengthLong(result.data().sizeBytes());
            byte[] buffer=new byte[DriveFileResources.BUFFER];long remaining=result.data().sizeBytes();
            var input=media.input();var output=response.getOutputStream();
            while(remaining>0) {
                service.verify(result,op);
                int target=(int)Math.min(remaining,buffer.length),count=0;
                // Account for each actual read while retaining the bounded block/final-EOF proof.
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
    private void json(HttpServletResponse response,DriveFileService.Result<?> result,DriveFileResources.Operation op) throws IOException {
        service.verify(result,op);response.setStatus(result.created()?201:200);response.setContentType("application/json");response.setHeader("Cache-Control","no-store");
        Object data=result.data();
        if(data instanceof DriveFileService.RootStatus root)data=new DriveFileDtos.Root(root.slot(),root.status());
        else if(data instanceof DriveFileService.FilePage page)data=new DriveFileDtos.Page(page.files().stream().map(this::fileDto).toList(),page.nextCursor(),page.hasMore());
        else if(data instanceof DriveFileProof.FileItem file)data=fileDto(file);
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
