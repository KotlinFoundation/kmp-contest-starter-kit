package com.kotlinfoundation.koko.util.file

import kotlinx.coroutines.await
import kotlin.js.Promise

// Kotlin/JS `js(...)` does not support async/await, so these use plain Promise chains (the wasmJs actual
// can use async/await via @JsFun).

private fun opfsWriteFileJs(dirName: String, name: String, b64: String): Promise<Unit> = js(
    """
    navigator.storage.getDirectory()
      .then(function (root) { return root.getDirectoryHandle(dirName, { create: true }); })
      .then(function (dir) { return dir.getFileHandle(name, { create: true }); })
      .then(function (fh) { return fh.createWritable(); })
      .then(function (w) {
        var bin = atob(b64);
        var arr = new Uint8Array(bin.length);
        for (var i = 0; i < bin.length; i++) arr[i] = bin.charCodeAt(i);
        return w.write(arr).then(function () { return w.close(); });
      })
    """,
)

internal actual suspend fun opfsWriteFile(dirName: String, name: String, base64: String) {
    opfsWriteFileJs(dirName, name, base64).await()
}

private fun opfsReadAllFilesJs(dirName: String): Promise<String> = js(
    """
    navigator.storage.getDirectory()
      .then(function (root) { return root.getDirectoryHandle(dirName, { create: true }); })
      .then(function (dir) {
        var it = dir.entries();
        var out = [];
        var step = function () {
          return it.next().then(function (res) {
            if (res.done) return JSON.stringify(out);
            var n = res.value[0], h = res.value[1];
            if (h.kind !== 'file') return step();
            return h.getFile()
              .then(function (f) { return f.arrayBuffer(); })
              .then(function (ab) {
                var buf = new Uint8Array(ab);
                var s = '';
                for (var i = 0; i < buf.length; i++) s += String.fromCharCode(buf[i]);
                out.push({ n: n, d: btoa(s) });
                return step();
              });
          });
        };
        return step();
      })
    """,
)

internal actual suspend fun opfsReadAllFiles(dirName: String): String = opfsReadAllFilesJs(dirName).await()
