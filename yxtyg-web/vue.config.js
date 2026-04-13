const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 10011,
    proxy: {
      '/api': {
        target: 'http://localhost:10010',
        changeOrigin: true
      }
    }
  }
})
