angular.module('gasp.dataset.new', ['gasp.core'])
.controller('DatasetNewCtrl', function($scope, $modalInstance, $log, Api) {
  $scope.dataset = {
    name: 'Untitled',
    query: 'SELECT * FROM <table>'
  };

  $scope.ok = function() {
    Api.dataset.create($scope.dataset).then(
      function(result) {
        $log.log(result.data);
        $modalInstance.close(result.data.id);
      },
      function(result) {
        $scope.error = result.data;
      });
  };
  $scope.cancel = function() {
    $modalInstance.dismiss('cancel');
  };
});