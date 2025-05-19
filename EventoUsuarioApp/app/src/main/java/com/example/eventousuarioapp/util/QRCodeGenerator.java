package com.example.eventousuarioapp.util;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Utilitário para geração de QR Codes a partir de texto.
 */
public class QRCodeGenerator {

    /**
     * Gera um Bitmap contendo um QR Code para o texto fornecido.
     *
     * @param text   Texto que será codificado no QR Code.
     * @param width  Largura do bitmap em pixels.
     * @param height Altura do bitmap em pixels.
     * @return Bitmap com o QR Code gerado, ou null em caso de erro.
     */
    public static Bitmap generateQRCode(String text, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            com.google.zxing.common.BitMatrix bitMatrix =
                qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] =
                        bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(
                width, height, Bitmap.Config.ARGB_8888
            );
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
