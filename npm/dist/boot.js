// Fix for __dirname not being in node global on Windows
global.__dirname = __dirname;
module.exports = require('./apex-assist-opt.js');
