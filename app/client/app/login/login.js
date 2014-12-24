
angular.module('jasp.login', [])
.config(
  function($stateProvider) {
    $stateProvider.state('login', {
      url: '/login',
      templateUrl: 'login/login.tpl.html',
      controller: function($scope, $rootScope, $http, $state, $log, Auth) {
        $scope.login = function(username, password) {
          Auth.login(username, password).then(
            function() {
              $state.go('home');
            },
            function() {
              $scope.error = true;
            });
        };
      }
    });
  });
