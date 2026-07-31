package com.kotlinfoundation.koko.util.file

import kotlinx.coroutines.await
import kotlin.js.Promise

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (dirName, name, b64) => (async () => {
      const root = await navigator.storage.getDirectory();
      const dir = await root.getDirectoryHandle(dirName, { create: true });
      const fh = await dir.getFileHandle(name, { create: true });
      const w = await fh.createWritable();
      const bin = atob(b64);
      const arr = new Uint8Array(bin.length);
      for (let i = 0; i < bin.length; i++) arr[i] = bin.charCodeAt(i);
      await w.write(arr);
      await w.close();
    })()
    """,
)
private external fun opfsWriteFileJs(dirName: String, name: String, b64: String): Promise<JsAny?>

internal actual suspend fun opfsWriteFile(dirName: String, name: String, base64: String) {
    opfsWriteFileJs(dirName, name, base64).await()
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (dirName) => (async () => {
      const root = await navigator.storage.getDirectory();
      const dir = await root.getDirectoryHandle(dirName, { create: true });
      const out = [];
      for await (const [n, h] of dir.entries()) {
        if (h.kind !== 'file') continue;
        const f = await h.getFile();
        const buf = new Uint8Array(await f.arrayBuffer());
        let s = '';
        for (let i = 0; i < buf.length; i++) s += String.fromCharCode(buf[i]);
        out.push({ n: n, d: btoa(s) });
      }
      return JSON.stringify(out);
    })()
    """,
)
private external fun opfsReadAllFilesJs(dirName: String): Promise<JsString>

internal actual suspend fun opfsReadAllFiles(dirName: String): String = opfsReadAllFilesJs(dirName).await().toString()
