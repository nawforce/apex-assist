// Expose some globals we need
global.__dirname = __dirname;
global.require = require;

// Run check function
let apex_assist = require('./apex-assist-fastopt.js');
apex_assist.check(process.argv);
