package com.andervalla.msevidencias.utils;

public class MagicBytesValidator {

    // Firmas hexadecimales
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47};

    public static boolean esFormatoValido(byte[] fileBytes, String mimeTypeEsperado) {
        if (fileBytes == null || fileBytes.length < 4) return false;

        return switch (mimeTypeEsperado) {
            case "image/jpeg", "image/jpg" -> startsWith(fileBytes, JPEG_MAGIC);
            case "image/png" -> startsWith(fileBytes, PNG_MAGIC);
            default -> false;
        };
    }

    private static boolean startsWith(byte[] source, byte[] match) {
        if (source.length < match.length) return false;
        for (int i = 0; i < match.length; i++) {
            if (source[i] != match[i]) return false;
        }
        return true;
    }

}
