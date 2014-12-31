angular.module('gasp.dataset.settings', ['gasp.core'])
.controller('DatasetSettingsCtrl',
  function($scope, $state, $modalInstance, Api, dataset) {
    $scope.dataset = dataset;
    $scope.save = function() {
      Api.dataset.update($scope.dataset).then(
        function(result) {
          $modalInstance.close();
        },
        function(result) {
          $scope.error = result.data;
        });
    };
    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    $scope.confirmDelete = false;
    $scope.cancelDelete = function() {
      $scope.confirmDelete = false;
    };

    $scope.delete = function() {
      if (!$scope.confirmDelete) {
        $scope.confirmDelete = true;
      }
      else {
        Api.dataset.remove($scope.dataset).then(
          function() {
            $modalInstance.close();
            $state.go('home');
          },
          function(result) {
            $scope.error = result.data;
          });
      }
    };
  });