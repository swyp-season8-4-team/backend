package org.swyp.dessertbee.store.coupon.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.store.coupon.util.QRCodeGenerator;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    private final QRCodeGenerator qrCodeGenerator;

    public QRCodeController(QRCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateQRCode(@RequestParam String text) throws Exception {
        byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return ResponseEntity.ok().headers(headers).body(qrCodeImage);
    }
}
