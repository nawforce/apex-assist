# Building Apex Assist

Before attempting to build make sure you have installed and working versions of:

    npm
    mvn
    sbt
    vsce

To build use:

    npm run package

If sucessful this will create an apex-assist-x.y.z.vsix file

By default I build on OS X, but the process should work on Linux. You should be able to build on Windows
by performing the same actions as the "package" script from package.json, but the scripts are setup to 
assume a shell is available.

The build process is rather involved due to the different stacks in use.

