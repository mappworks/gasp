angular.module('jasp.home', [
  'jasp.topnav', 'jasp.core', 'jasp.login', 'jasp.dataset'
])
.config(
  function($stateProvider) {
    $stateProvider
      .state('home', {
        resolve: {
          session: function(Auth) {
            return Auth.sessionOrLogin();
          }
        },
        url: '/',
        templateUrl: 'home/home.tpl.html',
        controller: 'HomeCtrl'
      });
  })
.controller('HomeCtrl',
  function($scope, $state, $log, Api, session) {
    $scope.session = session;

    Api.dataset.list({
      sort: 'modified-', limit: 3
    }).then(function(result) {
      $scope.datasets = result.data;
    });
  });
