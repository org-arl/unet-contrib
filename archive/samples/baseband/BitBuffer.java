package org.arl.unet.bb;

import org.arl.unet.UnetException;

/**
 * A bit buffer allowing reading/writing of data a bit at a time.
 */
public class BitBuffer {

  private byte[] buf;
  private int bytePos, bitPos;

  /**
   * Creates a bit buffer of a specfied size. The buffer size cannot be
   * changed after allocation.
   *
   * @param bytes number of bytes in the buffer
   */
  public BitBuffer(int bytes) {
    buf = new byte[bytes];
  }

  /**
   * Gets the size of the buffer in bytes.
   *
   * @return size in bytes
   */
  public int getSizeInBytes() {
    return buf.length;
  }

  /**
   * Gets the size of the buffer in bits.
   *
   * @return size in bits
   */
  public int getSizeInBits() {
    return buf.length*8;
  }

  /**
   * Resets the read/write position of the buffer to the start of the buffer.
   */
  public void reset() {
    bytePos = 0;
    bitPos = 0;
  }

  /**
   * Checks if the read/write position is at the end of the buffer.
   *
   * @return true if position at end, false otherwise
   */
  public boolean eos() {
    return bytePos >= buf.length;
  }

  /**
   * Writes one bit into the buffer.
   *
   * @param bit bit to write (0 or 1)
   */
  public void write(int bit) {
    if (bit < 0 || bit > 1) throw new UnetException("Invalid bit (must be 0 or 1)");
    if (bytePos >= buf.length) throw new UnetException("Buffer overflow");
    if (bit == 0) buf[bytePos] &= 0xff ^ (0x01 << bitPos);
    else buf[bytePos] |= 0x01 << bitPos;
    if (++bitPos > 7) {
      bitPos = 0;
      bytePos++;
    }
  }

  /**
   * Writes an array of bytes into the buffer. This method should not be called
   * when an incompletely written byte exists in the buffer.
   *
   * @param data array of bytes to write
   */
  public void write(byte[] data) {
    if (bitPos > 0) throw new UnetException("Buffer contains incomplete byte");
    if (bytePos + data.length > buf.length) throw new UnetException("Buffer overflow");
    System.arraycopy(data, 0, buf, bytePos, data.length);
    bytePos += data.length;
    bitPos = 0;
  }

  /**
   * Reads one bit of data from the buffer.
   *
   * @return bit (0 or 1) or -1 if end of stream
   */
  public int read() {
    if (bytePos >= buf.length) return -1;
    int b = buf[bytePos] & (0x01 << bitPos);
    if (++bitPos > 7) {
      bitPos = 0;
      bytePos++;
    }
    return b > 0 ? 1 : 0;
  }

  /**
   * Gets an array of bytes in the buffer. Any modifications to the array are
   * reflected as changes in the buffer.
   *
   * @return byte array of buffered data
   */
  public byte[] getBytes() {
    return buf;
  }

}
