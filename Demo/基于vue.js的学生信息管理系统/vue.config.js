const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 7000,
    hot: true
  },
  publicPath: process.env.NODE_ENV === 'production'
    ? '/production-sub-path/'
    : '/'
})
