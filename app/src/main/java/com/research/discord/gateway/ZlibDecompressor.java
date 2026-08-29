package com.research.discord.gateway;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Inflater;

import okio.ByteString;

public class ZlibDecompressor {
    private Inflater inflater;
    private ByteArrayOutputStream buffer;
    
    // Z_SYNC_FLUSH suffix indicates the end of a compressed message
    private static final byte[] ZLIB_SUFFIX = {0x00, 0x00, (byte) 0xff, (byte) 0xff};

    public ZlibDecompressor() {
        inflater = new Inflater();
        buffer = new ByteArrayOutputStream();
    }

    public String decompress(ByteString data) throws Exception {
        byte[] bytes = data.toByteArray();
        buffer.write(bytes);

        if (!endsWithSuffix(bytes, ZLIB_SUFFIX)) {
            // Wait for more chunks
            return null;
        }

        byte[] compressedData = buffer.toByteArray();
        buffer.reset(); // Reset for next message

        inflater.setInput(compressedData);
        ByteArrayOutputStream decompressedBuffer = new ByteArrayOutputStream();
        byte[] decompressedChunk = new byte[8192];
        
        while (!inflater.needsInput()) {
            int count = inflater.inflate(decompressedChunk);
            if (count > 0) {
                decompressedBuffer.write(decompressedChunk, 0, count);
            } else if (count == 0 && inflater.finished()) {
                break;
            }
        }
        
        return decompressedBuffer.toString(StandardCharsets.UTF_8.name());
    }
    
    private boolean endsWithSuffix(byte[] array, byte[] suffix) {
        if (array.length < suffix.length) return false;
        for (int i = 0; i < suffix.length; i++) {
            if (array[array.length - suffix.length + i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }
}
