function asBytes(input) {
  if (input instanceof Uint8Array) return input
  if (input instanceof ArrayBuffer) return new Uint8Array(input)
  if (ArrayBuffer.isView(input)) return new Uint8Array(input.buffer, input.byteOffset, input.byteLength)
  return new Uint8Array(input || [])
}

function decodeUtf16(bytes, littleEndian) {
  let result = ''
  for (let index = 0; index + 1 < bytes.length; index += 2) {
    const code = littleEndian
      ? bytes[index] | (bytes[index + 1] << 8)
      : (bytes[index] << 8) | bytes[index + 1]
    result += String.fromCharCode(code)
  }
  return result
}

function decodeUtf8(bytes) {
  let result = ''
  for (let index = 0; index < bytes.length;) {
    const first = bytes[index++]
    if (first < 0x80) {
      result += String.fromCharCode(first)
    } else if ((first & 0xe0) === 0xc0 && index < bytes.length) {
      result += String.fromCharCode(((first & 0x1f) << 6) | (bytes[index++] & 0x3f))
    } else if ((first & 0xf0) === 0xe0 && index + 1 < bytes.length) {
      result += String.fromCharCode(
        ((first & 0x0f) << 12) | ((bytes[index++] & 0x3f) << 6) | (bytes[index++] & 0x3f)
      )
    } else if ((first & 0xf8) === 0xf0 && index + 2 < bytes.length) {
      let codePoint = ((first & 0x07) << 18) |
        ((bytes[index++] & 0x3f) << 12) |
        ((bytes[index++] & 0x3f) << 6) |
        (bytes[index++] & 0x3f)
      codePoint -= 0x10000
      result += String.fromCharCode(0xd800 + (codePoint >> 10), 0xdc00 + (codePoint & 0x3ff))
    } else {
      result += '\ufffd'
    }
  }
  return result
}

function decodeText(input) {
  const bytes = asBytes(input)
  if (bytes.length >= 2 && bytes[0] === 0xff && bytes[1] === 0xfe) {
    return decodeUtf16(bytes.subarray(2), true).replace(/^\uFEFF/, '')
  }
  if (bytes.length >= 2 && bytes[0] === 0xfe && bytes[1] === 0xff) {
    return decodeUtf16(bytes.subarray(2), false).replace(/^\uFEFF/, '')
  }
  const start = bytes.length >= 3 && bytes[0] === 0xef && bytes[1] === 0xbb && bytes[2] === 0xbf ? 3 : 0
  return decodeUtf8(bytes.subarray(start)).replace(/^\uFEFF/, '')
}

module.exports = { asBytes, decodeText, decodeUtf8, decodeUtf16 }
