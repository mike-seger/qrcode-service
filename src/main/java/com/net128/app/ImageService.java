package com.net128.app;

import java.io.*;

import org.slf4j.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.common.BitMatrix;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Service
@Cacheable(cacheNames = "qr-code-cache", sync = true)
@Validated
public class ImageService {

	private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

	public byte[] generateQRCode(@NotNull @Size(min=1) String text, @Min(1) int width, @Min(1) int height) throws WriterException, IOException {
		logger.info("Will generate image  text=[{}], width=[{}], height=[{}]", text, width, height);

		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
		MatrixToImageWriter.writeToStream(matrix, MediaType.IMAGE_PNG.getSubtype(), baos, new MatrixToImageConfig());
		return baos.toByteArray();
	}

	@Async
	public ListenableFuture<byte[]> generateQRCodeAsync(@NotNull @Size(min=1) String text, @Min(1) int width, @Min(1) int height) throws Exception {
		return new AsyncResult<byte[]>(generateQRCode(text, width, height));
	}
	
	@CacheEvict(cacheNames = "qrcode-cache", allEntries = true)
	public void purgeCache() {
		logger.info("Purging cache");
	}

}