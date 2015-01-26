 /*global require, module, __dirname */
var fs = require('fs');
var path = require('path');
var url = require('url');
var rw = require('http-rewrite-middleware');

String.prototype.startsWith = function(s) {
  return this.indexOf(s) == 0;
};

String.prototype.endsWith = function(s) {
  return this.substr(-s.length) == s;
};

var app = {
  src: {
    js: ['app/**/*.js', '!**/*.tests.js'],
    less: ['app/**/*.less'],
    tpl: ['app/**/*.tpl.html']
  },
  lib: {
    js: [
      'jquery/dist/jquery.js',
      'codemirror/lib/codemirror.js',
      'codemirror/mode/sql/sql.js',
      'codemirror/addon/hint/show-hint.js',
      'codemirror/addon/hint/sql-hint.js',
      'angular/angular.js',
      'angular-sanitize/angular-sanitize.js',
      'angular-ui-router/release/angular-ui-router.js',
      'angular-bootstrap/ui-bootstrap.js',
      'angular-bootstrap/ui-bootstrap-tpls.js',
      'angular-ui-codemirror/ui-codemirror.js',
      'angular-smart-table/dist/smart-table.debug.js',
      'angular-ui-select/dist/select.js',
      'angular-ui-utils/ui-utils.js',
      'angular-xeditable/dist/js/xeditable.js',
      'ng-lodash/build/ng-lodash.js',
      'leaflet/dist/leaflet-src.js',
      'angular-leaflet-directive/dist/angular-leaflet-directive.js',
      'angular-gettext/dist/angular-gettext.js',
      'ng-table/dist/ng-table.js'
    ].map(function(dep) {
      return 'bower_components/' + dep;
    })
  }

};

module.exports = function(grunt) {
  grunt.initConfig({
    pkg: grunt.file.readJSON('./package.json'),

    clean: {
      build: {
        src: 'build/'
      }
    },

    concat: {
      libs: {
        src: app.lib.js,
        dest: 'build/libs.js'
      }
    },

    connect: {
      server: {
        options: {
          port: 8001,
          base: [
            path.join(__dirname, 'build'),
            path.join(__dirname, 'app'),
            __dirname
          ],
          livereload: true,
          middleware: function(connect, options) {
            var middle = [];

            middle.push(require('grunt-connect-proxy/lib/utils').proxyRequest);

            middle.push(rw.getMiddleware([{
              from: '^/gasp/app(.*)$', to: '/$1'
            }]));

            // debug script loader
            middle.push(function(req, res, next) {
              var parts = url.parse(req.url);
              if (parts.pathname.endsWith('/app.min.js')) {
                var template = path.join(__dirname, 'loader.js');
                fs.readFile(template, 'utf8', function(err, string) {
                  if (err) {
                    return next(err);
                  }
                  var scripts = grunt.file.expand(app.lib.js.concat(app.src.js))
                    .map(function(script) {
                      return '/' + script.split(path.sep).join('/');
                    });

                  res.setHeader('Content-Type', 'application/javascript');
                  var body =
                    string.replace('{{{ paths }}}', JSON.stringify(scripts));
                  res.end(body, 'utf8');
                });
              }
              else {
                next();
              }
            });

            // static files
            options.base.forEach(function(base) {
              middle.push(connect.static(base));
            });

            return middle;
          }
        },
        proxies: ['/api', '/auth'].map(function(path) {
          return {
            context: '/gasp' + path,
            host: 'localhost',
            port: 8000
          };
        })
      }
    },

    copy: {
      index: {
        files: [{
          expand: true,
          cwd: 'app',
          src: ['**/index.html'],
          dest: 'build'
        }]
      },

      assets: {
        files: [{
          expand: true,
          cwd: 'bower_components/codemirror',
          src: ['**/*.css'],
          dest: 'build/assets/codemirror'
        }, {
          expand: true,
          cwd: 'bower_components/icomoon/dist',
          src: ['**/*'],
          dest: 'build/assets/icomoon'
        }, {
          expand: true,
          cwd: 'bower_components/font-awesome',
          src: ['css/**/*', 'fonts/**/*'],
          dest: 'build/assets/font-awesome'
        }, {
          expand: true,
          cwd: 'bower_components/leaflet/dist',
          src: ['*.css', 'images/*'],
          dest: 'build/assets/leaflet'
        }]
      }
    },

    cssmin: {
      app: {
        files: [{
          expand: true,
          cwd: 'build',
          src: 'app.css',
          dest: 'build',
          ext: '.min.css'
        }]
      }
    },

    html2js: {
      app: {
        options: {
          base: 'app',
          module: 'gasp.templates',
          fileFooterString:
              'angular.module("gasp").requires.push("gasp.templates");',
          rename: function(name) {
            // return '/' + name;
            return name;
          }
        },
        src: app.src.tpl,
        dest: 'build/app.tpl.js'
      }
    },

    jshint: {
      options: {
        jshintrc: true
      },
      js: app.src.js
    },

    less: {
      build: {
        options: {
          paths: ['build/css']
        },
        cleancss: true,
        files: {
          'build/app.css': [app.src.less]
        }
      }
    },

    karma: {
      unit: {
        configFile: 'karma.conf.js',
        options: {
          files: app.lib.js
            .concat(['bower_components/angular-mocks/angular-mocks.js'])
            .concat(app.src.js)
        }
      }
    },

    ngAnnotate: {
      options: {
        singleQuotes: true
      },
      app: {
        files: {
          'build/app.js': app.src.js
        }
      }
    },

    nggettext_compile: {
      all: {
        options: {
          module: 'gasp'
        },
        files: {
          'build/i18n.js': ['po/*.po']
        }
      }
    },

    nggettext_extract: {
      pot: {
        files: {
          'po/template.pot': app.src.tpl
        }
      },
    },

    uglify: {
      app: {
        files: {
          'build/app.min.js': [
            'build/libs.js', 'build/app.js', 'build/app.tpl.js', 'build/i18n.js'
          ]
        }
      }
    },

    watch: {
      index: {
        files: ['app/**/index.html'],
        tasks: ['copy'],
        options: {
          livereload: true
        }
      },
      less: {
        files: app.src.less,
        tasks: ['less'],
        options: {
          livereload: true
        }
      },

      js: {
        files: app.src.js,
        tasks: ['jshint:js', 'karma'],
        options: {
          livereload: true
        }
      },
      tpl: {
        files: app.src.tpl,
        tasks: ['nggettext_extract']
      }
    }
  });

  // plugins
  grunt.loadNpmTasks('grunt-angular-gettext');
  grunt.loadNpmTasks('grunt-connect-proxy');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-connect');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-html2js');
  grunt.loadNpmTasks('grunt-karma');
  grunt.loadNpmTasks('grunt-ng-annotate');

  // tasks
  grunt.registerTask('build', ['less', 'copy', 'nggettext_extract']);
  grunt.registerTask('start',
    ['build', 'configureProxies:server', 'connect', 'watch']);
  grunt.registerTask('dist', ['build', 'html2js', 'nggettext_compile', 'concat',
    'ngAnnotate', 'uglify', 'cssmin']);
};
