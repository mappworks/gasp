/* global module */
module.exports = function(config) {
  config.set({
    // base path, that will be used to resolve files and exclude
    // frameworks to use
    frameworks: ['jasmine'],

    // // list of files / patterns to load in the browser
    // files: [
    //     'bower_components/angular/angular.js',
    //     'bower_components/angular-mocks/angular-mocks.js',
    //     'bower_components/ng-lodash/build/ng-lodash.js',
    //     'app/**/*.js'
    // ],

    // list of files to exclude
    exclude: [
    ],

    // test results reporter to use
    reporters: ['progress'],

    // web server port
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever file changes
    autoWatch: true,

    // Start these browsers
    browsers: ['PhantomJS'],

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: true
  });
};
