//@ts-check

'use strict';

const path = require('path');
const webpack = require('webpack');

/**@type {import('webpack').Configuration}*/
const config = {
  target: 'node',
  entry: './dist/boot-prod.js',
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'extension.js',
    libraryTarget: 'commonjs2',
    devtoolModuleFilenameTemplate: '../[resource-path]'
  },
  devtool: 'source-map',
  externals: {
    vscode: 'commonjs vscode' 
  },
  resolve: {
    mainFields: ['main'], 
    extensions: ['.js'],
  },
  ignoreWarnings: [
    {
      module: /dist\/boot-prod.js/,
    },
    {
      module: /@salesforce\/core\/lib\/messages.js/,
    },
  ]
};
module.exports = config;