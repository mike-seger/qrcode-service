package com.github.spring.example;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@CrossOrigin(methods = {GET, RequestMethod.DELETE})
public class QRCodeServiceApplication {

	static final String qrcodeENDPOINT = "/qrcode";
	
	@Autowired
	private ImageService imageService;

	@Value("${qrcode.cache.maxAgeMinutes}")
	private static final int maxCachedAgeMinutes = 30;

	@Value("${qrcode.service.dimension}")
	private int size;

	public static void main(String[] args) {
		SpringApplication.run(QRCodeServiceApplication.class, args);
	}

	@GetMapping(value = qrcodeENDPOINT, produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> qrCode(@RequestParam(value = "text") String text) {
		try {
			return ResponseEntity.ok().cacheControl(CacheControl.maxAge(maxCachedAgeMinutes, TimeUnit.MINUTES))
					.body(imageService.generateQRCodeAsync(text, size, size).get());
		} catch (Exception ex) {
			throw new InternalServerError("Error while generating QR code image.", ex);
		}
	}

	@PostMapping(value = qrcodeENDPOINT, produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> qrCode(@RequestParam(value = "file") MultipartFile file) {
		try {
			String text=new Scanner(file.getInputStream()).useDelimiter("\\A").next();
			return ResponseEntity.ok().cacheControl(CacheControl.maxAge(maxCachedAgeMinutes, TimeUnit.MINUTES))
					.body(imageService.generateQRCodeAsync(text, size, size).get());
		} catch (Exception ex) {
			throw new InternalServerError("Error while generating QR code image.", ex);
		}
	}

	@Scheduled(fixedRate = maxCachedAgeMinutes*1000*60)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping(value = qrcodeENDPOINT)
	public void deleteAllCachedImages() {
		imageService.purgeCache();
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public class InternalServerError extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public InternalServerError(final String message, final Throwable cause) {
			super(message);
		}

	}
}
