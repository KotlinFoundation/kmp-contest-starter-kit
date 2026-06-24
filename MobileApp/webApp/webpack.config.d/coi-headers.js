// Cross-origin isolation for the dev server.
//
// The local Room database on web is backed by SQLite-Wasm's OPFS VFS
// (`sqlite3.oo1.OpfsDb` in shared/sqlite-wasm-worker/worker.js). That VFS only works when the
// page is *cross-origin isolated* (`crossOriginIsolated === true`), which requires these two
// response headers. Without them the worker can't open the database and all local data
// (e.g. the credit transaction list) silently comes back empty.
//
// COEP is `credentialless` (not `require-corp`) so cross-origin resources like remote images
// (Coil) still load without each needing a CORP header.
//
// NOTE: this only covers the dev server. In production the web host must send the same two
// headers for OPFS to work (e.g. a `headers` block in your Firebase Hosting `firebase.json`).
config.devServer = config.devServer || {};
config.devServer.headers = Object.assign({}, config.devServer.headers, {
    "Cross-Origin-Opener-Policy": "same-origin",
    "Cross-Origin-Embedder-Policy": "credentialless",
});
