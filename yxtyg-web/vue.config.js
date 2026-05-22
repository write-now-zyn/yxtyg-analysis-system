const { defineConfig } = require('@vue/cli-service')

const devServerPort = Number(process.env.VUE_DEV_SERVER_PORT) || 10011
const apiProxyTarget = process.env.VUE_APP_API_PROXY_TARGET || process.env.API_PROXY_TARGET || 'http://localhost:10010'

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    host: '0.0.0.0',
    port: devServerPort,
    allowedHosts: 'all',
    proxy: {
      '/api': {
        target: apiProxyTarget,
        changeOrigin: true
      }
    }
  }
})
