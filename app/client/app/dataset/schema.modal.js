angular.module('gasp.dataset.schema', ['gasp.core', 'ngTable'])
.controller('DatasetSchemaCtrl',
  function($scope, $modalInstance, $log, ngTableParams, Api, selection) {
    $log.log(selection);
    $scope.ok = function() {
    };
    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    $scope.columns = [];
    $scope.tableOpts = new ngTableParams({
        page: 1,
        count: 100,
    }, {
      counts: [],
      total: $scope.columns.length,
      getData: function($defer, params) {
        var p = params.page();
        var n = params.count();
        $defer.resolve($scope.columns.slice((p-1) * n, p * n));
      }
    });

    $scope.tableSelected = function(table) {
      Api.table.get(table.name, table.schema).then(function(result) {
        $scope.columns = result.data.columns;
        $scope.tableOpts.reload();
      });
    };
    $scope.groupTable = function(table) {
      return table.schema;
    };
    $scope.selectColumn = function(col) {
      $modalInstance.close(col);
    };

    Api.table.list().then(function(result) {
      $scope.tables = result.data;
      angular.element('.ui-select-container')
        .controller('uiSelect').focusser[0].focus();

      if (selection) {
        $scope.table = {name: selection};
        $scope.tableSelected($scope.table);
      }
    });
  });