package br.com.boilerplate.services;

import br.com.boilerplate.errors.ExceptionCode;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.errors.exceptions.InternalUnexpectedException;
import br.com.boilerplate.utils.FilenameHelper;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlobUploadService {
    private final BlobContainerClient containerClient;

    private BlobClient getExistingBlobByName(String blobName) {
        var blob = this.getBlobReferenceByName(blobName);

        if (!blob.exists())
            throw new EntityNotFoundException(ExceptionCode.FILE_NOT_FOUND);

        return blob;
    }

    private BlobClient getBlobReferenceByName(String blobName) {
        blobName = this.removeRootPathSlash(blobName);
        return containerClient.getBlobClient(blobName);
    }

    private PagedIterable<BlobItem> getBlobsFromDirectory(String directory) {
        if (!directory.endsWith("/"))
            directory += "/";

        return containerClient.listBlobsByHierarchy(directory);
    }

    private String appendIndexToBlobName(String blobName) {
        var file = FilenameHelper.getFilenameData(blobName);
        var index = 1;

        while (this.getBlobReferenceByName(blobName).exists()) {
            var root = Objects.isNull(file.getParent()) ? "/" : file.getParent() + "/" ;
            blobName = root + file.getName() + "(" + index + ")" + file.getExtension();
            index++;
        }

        return blobName;
    }

    private String removeRootPathSlash(String filename) {
        filename = filename.startsWith("/")
                ? filename.substring(1)
                : filename;

        return filename;
    }

    public String upload(MultipartFile file) {
        return this.upload(file.getOriginalFilename(), file);
    }

    public String upload(String filename, MultipartFile file) {
        try {
            var originalExtension = FilenameHelper.getFilenameData(file.getOriginalFilename()).getExtension();

            if (Objects.isNull(filename) || filename.isBlank())
                filename = UUID.randomUUID() + originalExtension;

            var inputHasExtension = !FilenameHelper.getFilenameData(filename).getExtension().isBlank();
            var filenameWithIndex = this.appendIndexToBlobName(filename);

            filename = inputHasExtension ? filenameWithIndex : filenameWithIndex + originalExtension;
            var blob = this.getBlobReferenceByName(filename);

            final var overwriteExistingData = true;
            blob.upload(file.getInputStream(), file.getSize(), overwriteExistingData);

            return blob.getBlobName();
        } catch (IOException e) {
            throw new InternalUnexpectedException(ExceptionCode.FILE_PROCESSING_ERROR);
        }
    }

    public String update(String filename, MultipartFile file) {
        try {
            var blob = this.getExistingBlobByName(filename);

            final var overwriteExistingData = true;
            blob.upload(file.getInputStream(), file.getSize(), overwriteExistingData);

            return blob.getBlobName();
        } catch (IOException e) {
            throw new InternalUnexpectedException(ExceptionCode.FILE_PROCESSING_ERROR);
        }
    }

    public void delete(String filename) {
        this.getExistingBlobByName(filename).deleteIfExists();
    }

    public void deleteDirectory(String directory) {
        var blobs = this.getBlobsFromDirectory(directory);
        blobs.stream().map(BlobItem::getName).forEach(this::delete);
    }

    public String getBlobUrl(String filename) {
        return this.getExistingBlobByName(filename).getBlobUrl();
    }

    public InputStreamResource get(String blobName) {
        var blob = this.getExistingBlobByName(blobName);
        var filename = FilenameHelper.getFilenameData(blob.getBlobName()).getNameWithExtension();
        return new InputStreamResource(blob.openInputStream()) {

            @Override
            public String getFilename() {
                return filename;
            }

            @Override
            public long contentLength() throws IOException {
                return blob.getProperties().getBlobSize();
            }
        };
    }
}
