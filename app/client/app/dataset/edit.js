/* global L*/
angular.module('gasp.dataset.edit', [
  'gasp.core', 'ngSanitize', 'ui.codemirror', 'ui.select', 'ui.validate',
  'smart-table', 'xeditable', 'leaflet-directive'
])
.controller('DatasetEditCtrl',
  function($scope, $state, $stateParams, $timeout, $modal, $log, _, leafletData,
    Api) {
    $scope.id = $stateParams.id;

    // initialize the editor
    $scope.editorOpts = {
      lineWrapping : true,
      lineNumbers: true,
      styleActiveLine: true,
      mode: 'text/x-sql',
      foldGutter: true,
      gutters: [''],
      extraKeys: {
        'Ctrl-Space': 'autocomplete',
        // 'Ctrl-F': function(cm) {
        //   var pos = cm.getCursor();
        //   while(pos.line > 0 && cm.isFolded(pos)) {
        //     pos = {line: pos.line-1, ch:0};
        //   }
        //   cm.foldCode(pos, {
        //     rangeFinder: CodeMirror.fold.indent,
        //     scanUp: true
        //   });
        // },
        // tab remapping taken from:
        //   https://gist.github.com/danieleds/326903084a196055a7c3
        'Tab': function (cm) {
          if (cm.somethingSelected()) {
            var sel = cm.getSelection('\n');
            var cur = cm.getCursor();

            // Indent only if there are multiple lines selected,
            // or if the selection spans a full line
            if (sel.length > 0 && (sel.indexOf('\n') > -1 ||
              sel.length === cm.getLine(cur.line).length)) {
              cm.indentSelection('add');
              return;
            }
          }

          if (cm.options.indentWithTabs) {
            cm.execCommand('insertTab');
          }
          else {
            cm.execCommand('insertSoftTab');
          }
        },
        'Shift-Tab': function (cm) {
          cm.indentSelection('subtract');
        }
      },
      hintOptions: {
        tables: {
          //TODO
        }
      },
        tabMode: 'spaces'
    };
    $scope.onEditorLoad = function(editor) {
      $scope.editor = editor;
    };
    $scope.refreshEditor = function() {
      $timeout(function() {
        $scope.editor.refresh();
      }, 100);
    };

    // initialze the map
    $scope.mapOpts = {
      // tileLayer: 'http://{s}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png'
    };
    leafletData.getMap().then(function(map) {
      var osm =
        L.tileLayer(
          'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution:
            '&copy; <a href="http://www.openstreetmap.org/copyright">'+
              'OpenStreetMap</a>'
        }).addTo(map);

      var baseMaps = {
        'OSM Classic': osm
      };

      L.control.layers(baseMaps).addTo(map);
      $scope.map = map;
    });

    // functions for handling query parameters
    $scope.addParam = function() {
    };
    $scope.removeParam = function(p) {
      var params = $scope.dataset.params;
      params.splice(params.indexOf(p), 1);
    };

    $scope.isParamValid = function(val, param) {
      param.error = null;
      if (param.type == 'Number') {
        if (!(!isNaN(parseFloat(val)) && isFinite(val))) {
          param.error = 'Value "' + val + '" is not number';
          return false;
        }
      }
      else if (param.type == 'Date') {
        //TODO
      }
      return true;
    };

    $scope.autoDetectParams = true;
    $scope.updateParamsFromQuery = function(q) {
      // find all matches of ${...}
      var regex = /\$\{(\w*)\}/g;
      var match = null;
      var matches = [];

      while ((match = regex.exec(q)) != null) {
        matches.push(match[1]);
      }

      $scope.params = matches;
      // // add paramters
      // _.difference(matches, paramNames).forEach(function(paramName) {
      //   $scope.dataset.params.push({
      //     name: paramName,
      //     type: 'String',
      //     defaultValue: ''
      //   });
      // });

      // // mark parameters unused
      // var params = _.indexBy($scope.dataset.params, 'name');
      // $scope.dataset.params.forEach(function(param) {
      //   param.warning = null;
      // });

      // _.difference(paramNames, matches).forEach(function(paramName) {
      //   params[paramName].warning =
      //     'Parameter "' + paramName + '" not used in query';
      // });

    };

    $scope.save = function() {
      Api.dataset.update($scope.dataset)
        .then(function(result) {
          if (result.success) {

          }
          else {

          }
        });
    };

    $scope.openSettings = function() {
      $modal.open({
        templateUrl: 'dataset/settings.modal.tpl.html',
        controller: 'DatasetSettingsCtrl',
        backdrop: 'static',
        resolve: {
          dataset: function() {
            return $scope.dataset;
          }
        }
      }).result.then(function() {
        // TODO: alert of settings updated
      });
    };

    $scope.openSchemaBrowser = function() {
      $modal.open({
        templateUrl: 'dataset/schema.modal.tpl.html',
        controller: 'DatasetSchemaCtrl',
        backdrop: 'static',
        resolve: {
          selection: function() {
            return $scope.editor.getSelection();
          }
        }
      }).result.then(function(obj) {
        if (!$scope.editor.somethingSelected()) {
          var ed = $scope.editor;
          ed.replaceRange(obj.name + ' ', ed.getCursor());
        }
      });
    };

    var task = null;
    $scope.$watch('dataset.query', function(newVal) {
      if (newVal != null && $scope.autoDetectParams) {
        // parse the query, looking for new parameters
        if (task != null) {
          $timeout.cancel(task);
        }
        task = $timeout(function() {
          // parse query for params
          $scope.updateParamsFromQuery(newVal);
        }, 1000);
      }
    });

    // load the dataset
    Api.dataset.get($stateParams.id)
      .then(function(result) {
        $scope.dataset = result.data;
      });
  })
.directive('paramType', function($log) {
  return {
    restrict: 'EA',
    require: '^ngModel',
    scope: {
      ngModel: '='
    },
    link: function(scope, el, attrs, ngModel) {
      scope.types = ['String', 'Number', 'Date'];
      scope.changed = function(val) {
        scope.ngModel = val;
      };
    },
    template:
      '<ui-select ng-model="ngModel" on-select="changed($model)">' +
        '<ui-select-match>{{$select.selected}}</ui-select-match>'+
        '<ui-select-choices repeat="t in types | filter: $select.search">' +
          '<div ng-bind-html="t | highlight: $select.search"></div>' +
        '</ui-select-choices>'+
      '</ui-select>'
  };
});
