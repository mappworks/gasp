angular.module('gasp.erroralert', ['ui.bootstrap'])
.directive('errorAlert', function() {
  return {
    restrict: 'E',
    scope: {
      error: '='
    },
    templateUrl: 'components/erroralert/erroralert.tpl.html',
    controller: function($scope, $modal) {
      $scope.showDetails = function() {
        $modal.open({
          templateUrl: 'erroralert.modal.tpl.html',
          size: 'lg',
          resolve: {
            error: function() {
              return $scope.error;
            }
          },
          controller: function($scope, $modalInstance, error) {
            $scope.error = error;
            $scope.close = function() {
              $modalInstance.close();
            };
          }
        });
      };
    }
  };
});