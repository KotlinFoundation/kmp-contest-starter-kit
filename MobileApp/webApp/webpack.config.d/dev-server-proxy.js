// Local-dev CORS bypass for DIRECT-mode AI: the browser can't call api.replicate.com /
// api.openai.com (no Access-Control-Allow-Origin), so the app (on web) calls same-origin
// /replicate & /openai and the dev server proxies them to the provider. See baseUrl in
// ReplicateApiService / OpenAiApiService. Deployed web still needs the Cloud Functions proxy.
//
// This lives here rather than in commonWebpackConfig {} because that block is a Gradle script
// object reference, which the configuration cache cannot serialize.
if (config.devServer) {
    config.devServer.proxy = [
        {
            context: ["/replicate"],
            target: "https://api.replicate.com",
            pathRewrite: { "^/replicate": "" },
            changeOrigin: true,
            secure: false,
        },
        {
            context: ["/openai"],
            target: "https://api.openai.com",
            pathRewrite: { "^/openai": "" },
            changeOrigin: true,
            secure: false,
        },
        // Replicate's output-image CDN also sends no CORS headers, so the generated image is
        // fetched through the proxy too (see FileManager.web.kt). Non-nested prefix:
        // "/replicate" would greedily match this path.
        {
            context: ["/rdelivery"],
            target: "https://replicate.delivery",
            pathRewrite: { "^/rdelivery": "" },
            changeOrigin: true,
            secure: false,
        },
    ];
}
